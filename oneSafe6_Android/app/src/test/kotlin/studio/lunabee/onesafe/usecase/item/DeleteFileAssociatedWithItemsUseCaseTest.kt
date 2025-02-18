package studio.lunabee.onesafe.usecase.item

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.TestCase.assertFalse
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.onesafe.AppAndroidTestUtils
import studio.lunabee.onesafe.domain.model.crypto.EncryptEntry
import studio.lunabee.onesafe.domain.repository.SafeItemKeyRepository
import studio.lunabee.onesafe.domain.usecase.DeleteFileAssociatedWithItemsUseCase
import studio.lunabee.onesafe.domain.usecase.item.AddFieldUseCase
import studio.lunabee.onesafe.domain.usecase.item.CreateItemUseCase
import studio.lunabee.onesafe.extension.iconSample
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltUnitTest
import studio.lunabee.onesafe.test.firstSafeId
import studio.lunabee.onesafe.test.test
import studio.lunabee.onesafe.test.testUUIDs
import javax.inject.Inject

@HiltAndroidTest
class DeleteFileAssociatedWithItemsUseCaseTest : OSHiltUnitTest() {
    @get:Rule override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.Home()

    @Inject lateinit var deleteFileAssociatedWithItemsUseCase: DeleteFileAssociatedWithItemsUseCase

    @Inject lateinit var createItemUseCase: CreateItemUseCase

    @Inject lateinit var addFieldUseCase: AddFieldUseCase

    @Inject lateinit var safeItemKeyRepository: SafeItemKeyRepository

    @Test
    fun delete_files_from_item_list(): TestResult = runTest {
        val item1 = createItemUseCase.test()
        val item2 = createItemUseCase.test()
        val item3 = createItemUseCase.test()

        // Field item 1
        addFieldUseCase(itemId = item1.id, itemFieldData = AppAndroidTestUtils.createItemFieldPhotoData(testUUIDs[0].toString()))
        val key0 = safeItemKeyRepository.getSafeItemKey(item1.id)
        fileRepository.addFile(testUUIDs[0], cryptoRepository.encrypt(key0, EncryptEntry(iconSample)), safeId = firstSafeId)

        addFieldUseCase(itemId = item1.id, itemFieldData = AppAndroidTestUtils.createItemFieldPhotoData(testUUIDs[1].toString()))
        val key1 = safeItemKeyRepository.getSafeItemKey(item1.id)
        fileRepository.addFile(testUUIDs[1], cryptoRepository.encrypt(key1, EncryptEntry(iconSample)), safeId = firstSafeId)

        // Field item 2
        addFieldUseCase(itemId = item2.id, itemFieldData = AppAndroidTestUtils.createItemFieldPhotoData(testUUIDs[2].toString()))
        val key2 = safeItemKeyRepository.getSafeItemKey(item2.id)
        fileRepository.addFile(testUUIDs[2], cryptoRepository.encrypt(key2, EncryptEntry(iconSample)), safeId = firstSafeId)

        // Field item 3
        addFieldUseCase(itemId = item3.id, itemFieldData = AppAndroidTestUtils.createItemFieldPhotoData(testUUIDs[3].toString()))
        val key3 = safeItemKeyRepository.getSafeItemKey(item3.id)
        fileRepository.addFile(testUUIDs[3], cryptoRepository.encrypt(key3, EncryptEntry(iconSample)), safeId = firstSafeId)

        assert(fileRepository.getFile(testUUIDs[0].toString()).exists())
        assert(fileRepository.getFile(testUUIDs[1].toString()).exists())
        assert(fileRepository.getFile(testUUIDs[2].toString()).exists())
        assert(fileRepository.getFile(testUUIDs[3].toString()).exists())

        deleteFileAssociatedWithItemsUseCase(listOf(item1.id, item2.id))

        assertFalse(fileRepository.getFile(testUUIDs[0].toString()).exists())
        assertFalse(fileRepository.getFile(testUUIDs[1].toString()).exists())
        assertFalse(fileRepository.getFile(testUUIDs[2].toString()).exists())
        assert(fileRepository.getFile(testUUIDs[3].toString()).exists())
    }
}
