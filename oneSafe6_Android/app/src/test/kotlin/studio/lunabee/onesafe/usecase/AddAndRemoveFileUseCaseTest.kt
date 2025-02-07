package studio.lunabee.onesafe.usecase

import android.webkit.MimeTypeMap
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.robolectric.shadow.api.Shadow
import org.robolectric.shadows.ShadowMimeTypeMap
import studio.lunabee.onesafe.domain.model.crypto.DecryptEntry
import studio.lunabee.onesafe.domain.model.crypto.EncryptEntry
import studio.lunabee.onesafe.domain.model.safeitem.FileSavingData
import studio.lunabee.onesafe.domain.model.safeitem.SafeItem
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.domain.repository.SafeItemFieldRepository
import studio.lunabee.onesafe.domain.repository.SafeItemKeyRepository
import studio.lunabee.onesafe.domain.usecase.item.AddAndRemoveFileUseCase
import studio.lunabee.onesafe.domain.usecase.item.AddFieldUseCase
import studio.lunabee.onesafe.domain.usecase.item.CreateItemUseCase
import studio.lunabee.onesafe.domain.usecase.item.ItemDecryptUseCase
import studio.lunabee.onesafe.extension.iconSample
import studio.lunabee.onesafe.test.CommonTestUtils.createItemFieldData
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltUnitTest
import studio.lunabee.onesafe.test.firstSafeId
import studio.lunabee.onesafe.test.testUUIDs
import java.util.UUID
import javax.inject.Inject
import kotlin.test.assertContentEquals
import kotlin.test.assertFalse

@HiltAndroidTest
class AddAndRemoveFileUseCaseTest : OSHiltUnitTest() {

    @get:Rule
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.Home()

    @Inject lateinit var addFieldUseCase: AddFieldUseCase

    @Inject lateinit var safeItemKeyRepository: SafeItemKeyRepository

    @Inject lateinit var createItemUseCase: CreateItemUseCase

    @Inject lateinit var addAndRemoveFileUseCase: AddAndRemoveFileUseCase

    @Inject lateinit var itemDecryptUseCase: ItemDecryptUseCase

    @Inject lateinit var getCachedThumbnailUseCase: AndroidGetCachedThumbnailUseCase

    @Inject lateinit var safeItemFieldRepository: SafeItemFieldRepository

    @Test
    fun add_file_test(): TestResult = runTest {
        val item = createItem()
        val newFileId = testUUIDs[1]
        val newField = createItemFieldData(
            kind = SafeItemFieldKind.Photo,
            name = "newFile.jpeg",
            value = "$newFileId|jpeg",
        )
        addFieldUseCase(itemId = item.id, itemFieldData = newField)

        val savingData = FileSavingData.ToSave(
            fileId = newFileId,
            getStream = { iconSample.inputStream() },
        )
        addAndRemoveFileUseCase(
            item = item,
            fileSavingData = listOf(savingData),
        )

        val encData = fileRepository.getFile(newFileId.toString()).readBytes()
        val plainData = itemDecryptUseCase.invoke(encData, item.id, ByteArray::class).data
        assertContentEquals(iconSample, plainData)
    }

    @Test
    fun remove_file_test(): TestResult = runTest {
        val shadowMimeTypeMap = Shadow.extract<ShadowMimeTypeMap>(MimeTypeMap.getSingleton())
        shadowMimeTypeMap.addExtensionMimeTypeMapping("jpeg", "image/jpeg")

        val itemCreated = createItem()
        val fileToRemoveId = testUUIDs[0]
        val existingField = createItemFieldData(
            kind = SafeItemFieldKind.Photo,
            name = "newFile.jpeg",
            value = "$fileToRemoveId|jpeg",
        )
        val key0 = safeItemKeyRepository.getSafeItemKey(itemCreated.id)
        val field = addFieldUseCase(itemId = itemCreated.id, itemFieldData = existingField).data
        fileRepository.addFile(fileId = testUUIDs[0], file = cryptoRepository.encrypt(key0, EncryptEntry(iconSample)), safeId = firstSafeId)

        // Generate thumbnail file
        getCachedThumbnailUseCase(field!!)
        val updatedField = safeItemFieldRepository.getSafeItemField(field.id)
        val thumbnailName = cryptoRepository.decrypt(key0, DecryptEntry(updatedField.encThumbnailFileName!!, UUID::class))
        val thumbnailFile = fileRepository.getThumbnailFile(thumbnailName.toString(), false)
        assert(thumbnailFile.exists())

        val savingData = FileSavingData.ToRemove(fileId = fileToRemoveId, updatedField.encThumbnailFileName)
        addAndRemoveFileUseCase(
            item = itemCreated,
            fileSavingData = listOf(savingData),
        )
        val file = fileRepository.getFile(fileToRemoveId.toString())
        assertFalse(file.exists())
        assertFalse(thumbnailFile.exists())
    }

    private suspend fun createItem(): SafeItem {
        return createItemUseCase(
            name = "ItemCreated",
            parentId = null,
            isFavorite = false,
            icon = null,
            color = "#FFFFFF",
        ).data!!
    }
}
