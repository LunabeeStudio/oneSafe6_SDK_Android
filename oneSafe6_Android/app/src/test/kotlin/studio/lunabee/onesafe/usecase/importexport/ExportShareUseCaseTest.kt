package studio.lunabee.onesafe.usecase.importexport

import com.lunabee.lbcore.model.LBResult
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.onesafe.AppAndroidTestUtils
import studio.lunabee.onesafe.domain.model.crypto.EncryptEntry
import studio.lunabee.onesafe.domain.model.importexport.ImportMode
import studio.lunabee.onesafe.domain.qualifier.ArchiveCacheDir
import studio.lunabee.onesafe.domain.repository.SafeItemFieldRepository
import studio.lunabee.onesafe.domain.repository.SafeItemKeyRepository
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.domain.usecase.item.AddFieldUseCase
import studio.lunabee.onesafe.domain.usecase.item.CreateItemUseCase
import studio.lunabee.onesafe.domain.usecase.item.ItemDecryptUseCase
import studio.lunabee.onesafe.extension.iconSample
import studio.lunabee.onesafe.importexport.ArchiveConstants
import studio.lunabee.onesafe.importexport.engine.ImportEngine
import studio.lunabee.onesafe.importexport.engine.ShareExportEngine
import studio.lunabee.onesafe.importexport.usecase.ArchiveUnzipUseCase
import studio.lunabee.onesafe.importexport.usecase.ExportShareUseCase
import studio.lunabee.onesafe.proto.OSExportProto
import studio.lunabee.onesafe.test.CommonTestUtils.createItemFieldData
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltUnitTest
import studio.lunabee.onesafe.test.assertSuccess
import studio.lunabee.onesafe.test.firstSafeId
import studio.lunabee.onesafe.test.test
import java.io.File
import javax.inject.Inject
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@HiltAndroidTest
class ExportShareUseCaseTest : OSHiltUnitTest() {
    @get:Rule
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @Inject lateinit var exportShareUseCase: ExportShareUseCase

    @Inject lateinit var safeItemRepository: SafeItemRepository

    @Inject lateinit var decryptUseCase: ItemDecryptUseCase

    @Inject lateinit var createItemUseCase: CreateItemUseCase

    @Inject lateinit var addFieldUseCase: AddFieldUseCase

    @Inject lateinit var safeItemFieldRepository: SafeItemFieldRepository

    @Inject
    @ArchiveCacheDir(type = ArchiveCacheDir.Type.Import)
    lateinit var archiveExtractedDirectory: File

    @Inject
    @ArchiveCacheDir(type = ArchiveCacheDir.Type.Export)
    lateinit var archiveDir: File

    @Inject lateinit var archiveUnzipUseCase: ArchiveUnzipUseCase

    @Inject lateinit var importEngine: ImportEngine

    @Inject
    lateinit var exportEngine: ShareExportEngine

    @Inject lateinit var safeItemKeyRepository: SafeItemKeyRepository

    companion object {
        private const val ITEMS_NUMBER: Int = 10
    }

    override val initialTestState: InitialTestState = InitialTestState.SignedOut

    private val exportPassword = "azerty123"

    @Test
    fun share_backup_data_test(): TestResult = runTest {
        signup()
        login()

        repeat(ITEMS_NUMBER) {
            val item = createItemUseCase.test(name = "$it", position = it.toDouble(), icon = ByteArray(1), isFavorite = true)
            addFieldUseCase(item.id, listOf(createItemFieldData(), createItemFieldData())) // All items will have 2 fields

            addFieldUseCase(
                itemId = item.id,
                itemFieldData = AppAndroidTestUtils.createItemFieldPhotoData(item.id.toString()),
            )
            val key = safeItemKeyRepository.getSafeItemKey(item.id)
            fileRepository.addFile(
                fileId = item.id,
                file = cryptoRepository.encrypt(key, EncryptEntry(iconSample)),
                safeId = firstSafeId,
            )
        }

        val itemToShare = safeItemRepository.getAllSafeItems(firstSafeId).random()
        val itemToShareName = (decryptUseCase(itemToShare.encName!!, itemToShare.id, String::class) as LBResult.Success).successData

        exportEngine.buildExportInfo(
            password = exportPassword.toCharArray(),
        ).last()

        val exportedArchiveResult = exportShareUseCase(
            exportEngine = exportEngine,
            itemToShare = itemToShare.id,
            includeChildren = false,
            archiveExtractedDirectory = archiveDir,
        ).last()
        assertSuccess(exportedArchiveResult)

        signOut()
        signup()
        login()

        // Try to re-import exported archive.
        exportedArchiveResult.successData.inputStream().use { inputStream ->
            archiveUnzipUseCase(inputStream, archiveExtractedDirectory).last()
        }

        importEngine.authenticateAndExtractData(
            archiveExtractedDirectory,
            exportPassword.toCharArray(),
        ).last()
        importEngine.prepareDataForImport(archiveExtractedDirectory, ImportMode.Replace).last()
        importEngine.saveImportData(mode = ImportMode.Replace).last()

        // Check that the element is readable.
        val allSafeItemsSaved = safeItemRepository.getAllSafeItems(firstSafeId)
        assertEquals(expected = 1, actual = allSafeItemsSaved.size)

        // Check that only the 2 fields of the items where imported
        val allFieldsSaved = safeItemFieldRepository.getAllSafeItemFields(firstSafeId)
        assertEquals(expected = 3, actual = allFieldsSaved.size)

        val allIconsSaved = iconRepository.getIcons(safeId = firstSafeId)
        assertEquals(expected = 1, actual = allIconsSaved.size)

        val allFilesSaved = fileRepository.getFiles(safeId = firstSafeId)
        assertEquals(expected = 1, actual = allFilesSaved.size)

        val safeItem = allSafeItemsSaved.first()
        safeItem.encName?.let {
            val nameResult = decryptUseCase(it, safeItem.id, String::class)
            assertSuccess(nameResult)
            assertEquals(
                expected = itemToShareName,
                actual = nameResult.successData,
            )
        }
        assertFalse(safeItem.isFavorite)
    }

    /**
     * Check the parent ID of the root item is nullified at export
     */
    @Test
    fun share_backup_data_from_child_nullify_parent_test(): TestResult = runTest {
        signup()
        login()

        val rootItem = createItemUseCase.test()
        val childItem = createItemUseCase.test(parentId = rootItem.id)

        exportEngine.buildExportInfo(
            password = exportPassword.toCharArray(),
        ).last()

        val exportedArchiveResult = exportShareUseCase(
            exportEngine = exportEngine,
            itemToShare = childItem.id,
            includeChildren = false,
            archiveExtractedDirectory = archiveDir,
        ).last()
        assertSuccess(exportedArchiveResult)

        exportedArchiveResult.successData.inputStream().use { inputStream ->
            archiveUnzipUseCase(inputStream, archiveExtractedDirectory).last()
        }

        val importedItems = File(archiveExtractedDirectory, ArchiveConstants.DataFile).readBytes().let(OSExportProto.Archive::parseFrom)!!
        val actualParentId = importedItems.itemsList.single().parentId

        assertEquals("", actualParentId)
    }
}
