package studio.lunabee.onesafe.usecase.importexport

import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.datetime.toKotlinInstant
import studio.lunabee.bubbles.domain.repository.BubblesCryptoRepository
import studio.lunabee.bubbles.domain.repository.ContactRepository
import studio.lunabee.bubbles.domain.usecase.CreateContactUseCase
import studio.lunabee.doubleratchet.DoubleRatchetEngine
import studio.lunabee.doubleratchet.crypto.DoubleRatchetKeyRepository
import studio.lunabee.doubleratchet.model.DRSharedSecret
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.doubleratchet.model.createRandomUUID
import studio.lunabee.messaging.domain.model.SharedMessage
import studio.lunabee.messaging.domain.repository.ConversationRepository
import studio.lunabee.messaging.domain.repository.MessageRepository
import studio.lunabee.messaging.domain.usecase.SaveMessageUseCase
import studio.lunabee.onesafe.AppAndroidTestUtils
import studio.lunabee.onesafe.domain.common.FileIdProvider
import studio.lunabee.onesafe.domain.model.crypto.EncryptEntry
import studio.lunabee.onesafe.domain.model.importexport.ImportMode
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.qualifier.ArchiveCacheDir
import studio.lunabee.onesafe.domain.qualifier.BackupType
import studio.lunabee.onesafe.domain.repository.SafeItemKeyRepository
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.domain.usecase.SetIconUseCase
import studio.lunabee.onesafe.domain.usecase.item.AddFieldUseCase
import studio.lunabee.onesafe.domain.usecase.item.CreateItemUseCase
import studio.lunabee.onesafe.domain.usecase.item.ItemDecryptUseCase
import studio.lunabee.onesafe.domain.usecase.item.RemoveAllDeletedItemUseCase
import studio.lunabee.onesafe.extension.iconSample
import studio.lunabee.onesafe.importexport.engine.BackupExportEngine
import studio.lunabee.onesafe.importexport.engine.ImportEngine
import studio.lunabee.onesafe.importexport.repository.LocalBackupRepository
import studio.lunabee.onesafe.importexport.usecase.ArchiveUnzipUseCase
import studio.lunabee.onesafe.test
import studio.lunabee.onesafe.test.CommonTestUtils.createItemFieldData
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltUnitTest
import studio.lunabee.onesafe.test.OSTestConfig
import studio.lunabee.onesafe.test.assertSuccess
import studio.lunabee.onesafe.test.firstSafeId
import studio.lunabee.onesafe.test.test
import java.io.File
import java.time.Instant
import javax.inject.Inject
import kotlin.test.assertEquals

@HiltAndroidTest
abstract class AbstractExportUseCaseTest : OSHiltUnitTest() {
    @Inject lateinit var safeItemRepository: SafeItemRepository

    @Inject lateinit var safeItemKeyRepository: SafeItemKeyRepository

    @Inject lateinit var decryptUseCase: ItemDecryptUseCase

    @Inject lateinit var createItemUseCase: CreateItemUseCase

    @Inject lateinit var addFieldUseCase: AddFieldUseCase

    @Inject lateinit var setIconUseCase: SetIconUseCase

    @Inject lateinit var removeAllDeletedItemUseCase: RemoveAllDeletedItemUseCase

    @Inject
    @ArchiveCacheDir(type = ArchiveCacheDir.Type.Import)
    lateinit var archiveExtractedDirectory: File

    @Inject
    @ArchiveCacheDir(type = ArchiveCacheDir.Type.AutoBackup)
    lateinit var archiveCacheDir: File

    @Inject lateinit var archiveUnzipUseCase: ArchiveUnzipUseCase

    @Inject
    @BackupType(BackupType.Type.Auto)
    lateinit var exportEngine: BackupExportEngine

    @Inject lateinit var importEngine: ImportEngine

    @Inject lateinit var localBackupRepository: LocalBackupRepository

    @Inject lateinit var fileIdProvider: FileIdProvider

    @Inject lateinit var createContactUseCase: CreateContactUseCase

    @Inject lateinit var saveMessageUseCase: SaveMessageUseCase

    @Inject lateinit var contactRepository: ContactRepository

    @Inject lateinit var conversationRepository: ConversationRepository

    @Inject lateinit var messageRepository: MessageRepository

    override val initialTestState: InitialTestState = InitialTestState.SignedOut

    @Inject lateinit var doubleRatchetEngine: DoubleRatchetEngine

    @Inject lateinit var doubleRatchetKeyRepository: DoubleRatchetKeyRepository

    @Inject lateinit var bubblesCryptoRepository: BubblesCryptoRepository

    private fun itemName(idx: Int): String = "Item $idx"

    protected suspend fun setupItemsForTest(safeId: SafeId = firstSafeId) {
        repeat(ITEMS_NUMBER) { itemIdx ->
            val item = createItemUseCase.test(name = itemName(itemIdx), position = itemIdx.toDouble())
            repeat(FIELDS_NUMBER) { fieldIdx ->
                addFieldUseCase(
                    itemId = item.id,
                    itemFieldData = createItemFieldData(name = "Field $fieldIdx", position = fieldIdx.toDouble()),
                )
            }
            setIconUseCase(item, iconSample)
            addFieldUseCase(
                itemId = item.id,
                itemFieldData = AppAndroidTestUtils.createItemFieldPhotoData(item.id.toString()),
            )
            val key = safeItemKeyRepository.getSafeItemKey(item.id)
            fileRepository.addFile(
                fileId = fileIdProvider(),
                file = cryptoRepository.encrypt(key, EncryptEntry(iconSample)),
                safeId = safeId,
            )
        }
    }

    protected suspend fun setupBubblesForTest() {
        repeat(CONTACT_NUMBER) { itemIdx ->
            val sharedConversationId = createRandomUUID()
            val contactId: DoubleRatchetUUID = createContactUseCase.test(name = "Test $itemIdx")
            val sharedSalt = bubblesCryptoRepository.deriveUUIDToKey(
                sharedConversationId,
                doubleRatchetKeyRepository.rootKeyByteSize,
            )
            doubleRatchetEngine.createInvitation(
                sharedSalt = DRSharedSecret(sharedSalt),
                newConversationId = contactId,
            )
            repeat(MESSAGE_NUMBER) { messageId ->
                saveMessageUseCase(
                    plainMessage = SharedMessage(
                        "message $messageId",
                        contactId,
                        Instant.now(OSTestConfig.clock).toKotlinInstant(),
                    ),
                    contactId = contactId,
                    channel = "",
                    id = createRandomUUID(),
                    safeItemId = createRandomUUID(),
                )
            }
        }
    }

    protected suspend fun assertImport(archiveResult: File, importBubbles: Boolean, importItems: Boolean) {
        assertSuccess(
            archiveResult.inputStream().use { inputStream ->
                archiveUnzipUseCase(inputStream, archiveExtractedDirectory).last()
            },
        )

        assertSuccess(
            importEngine.authenticateAndExtractData(
                archiveExtractedDirectory,
                testPassword.toCharArray(),
            ).last(),
        )
        importEngine.setDataToImport(importBubbles = importBubbles, importItems = importItems)
        assertSuccess(importEngine.prepareDataForImport(archiveExtractedDirectory, ImportMode.Replace).last())
        assertSuccess(importEngine.saveImportData(mode = ImportMode.Replace).last())

        // Remove items deleted during import with ImportMode.Replace
        removeAllDeletedItemUseCase()

        // Check that all elements are readable.
        val allSafeItemsSaved = safeItemRepository.getAllSafeItems(firstSafeId)
        val allContactSaved = contactRepository.getAllContactsFlow(DoubleRatchetUUID(firstSafeId.id)).first()
        val allConversationSaved = allContactSaved.mapNotNull {
            conversationRepository.getConversation(it.id)
        }
        val allMessageSaved = allContactSaved.flatMap { contact -> messageRepository.getAllByContact(contact.id) }
        val allFileSaved = fileRepository.getFiles(
            safeId = firstSafeId,
        )
        if (importItems) {
            assertEquals(expected = ITEMS_NUMBER, actual = allSafeItemsSaved.size)
            assertEquals(expected = ITEMS_NUMBER, actual = allFileSaved.size)
        } else {
            assertEquals(expected = 0, actual = allSafeItemsSaved.size)
            assertEquals(expected = 0, actual = allFileSaved.size)
        }
        if (importBubbles) {
            assertEquals(expected = CONTACT_NUMBER, actual = allContactSaved.size)
            assertEquals(expected = CONTACT_NUMBER, actual = allConversationSaved.size)
            assertEquals(expected = MESSAGE_NUMBER * CONTACT_NUMBER, actual = allMessageSaved.size)
        } else {
            assertEquals(expected = 0, actual = allContactSaved.size)
            assertEquals(expected = 0, actual = allConversationSaved.size)
            assertEquals(expected = 0, actual = allMessageSaved.size)
        }
        allSafeItemsSaved.sortedBy { it.position }.forEachIndexed { index, safeItem ->
            safeItem.encName?.let {
                val nameResult = decryptUseCase(it, safeItem.id, String::class)
                assertSuccess(nameResult)
                assertEquals(expected = itemName(index), actual = nameResult.successData)
            }
        }
    }

    companion object {
        const val ITEMS_NUMBER: Int = 3
        const val FIELDS_NUMBER: Int = 3
        const val CONTACT_NUMBER: Int = 3
        const val MESSAGE_NUMBER: Int = 3
    }
}
