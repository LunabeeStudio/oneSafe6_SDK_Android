package studio.lunabee.onesafe.usecase.item

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.compose.androidtest.helper.LbcResourcesHelper
import studio.lunabee.onesafe.domain.model.common.UpdateState
import studio.lunabee.onesafe.domain.model.crypto.EncryptEntry
import studio.lunabee.onesafe.domain.model.safeitem.FileSavingData
import studio.lunabee.onesafe.domain.model.safeitem.SafeItem
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.domain.repository.SafeItemFieldRepository
import studio.lunabee.onesafe.domain.repository.SafeItemKeyRepository
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.domain.usecase.item.AddFieldUseCase
import studio.lunabee.onesafe.domain.usecase.item.CreateItemUseCase
import studio.lunabee.onesafe.domain.usecase.item.ItemDecryptUseCase
import studio.lunabee.onesafe.domain.usecase.item.UpdateItemUseCase
import studio.lunabee.onesafe.extension.iconSample
import studio.lunabee.onesafe.test.CommonTestUtils.createItemFieldData
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltUnitTest
import studio.lunabee.onesafe.test.firstSafeId
import studio.lunabee.onesafe.test.test
import studio.lunabee.onesafe.test.testUUIDs
import java.io.File
import javax.inject.Inject
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@HiltAndroidTest
class UpdateItemUseCaseTest : OSHiltUnitTest() {

    @get:Rule
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.Home()

    @Inject lateinit var updateItemUseCase: UpdateItemUseCase

    @Inject lateinit var createItemUseCase: CreateItemUseCase

    @Inject lateinit var safeItemRepository: SafeItemRepository

    @Inject lateinit var decryptUseCase: ItemDecryptUseCase

    @Inject lateinit var safeItemFieldRepository: SafeItemFieldRepository

    @Inject lateinit var addFieldUseCase: AddFieldUseCase

    @Inject lateinit var safeItemKeyRepository: SafeItemKeyRepository

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val iconDir = File(context.filesDir, "icons")

    @Test
    fun metadata_full_update_test(): TestResult = runTest {
        val icon = LbcResourcesHelper.readResourceAsBytes("icon_259_194.jpeg")

        val itemCreated = createItem()

        val itemNameModified = "ItemModified"
        val itemColorModified = "#000000"
        val data = UpdateItemUseCase.UpdateData(
            name = UpdateState.ModifiedTo(newValue = itemNameModified),
            icon = UpdateState.ModifiedTo(newValue = icon),
            color = UpdateState.ModifiedTo(newValue = itemColorModified),
        )

        val itemModified = updateItemUseCase.invoke(
            itemId = itemCreated.id,
            updateData = data,
            fileSavingData = listOf(),
        ).data!!

        val retrievedItem = safeItemRepository.getSafeItem(itemModified.id)
        assertEquals(expected = itemModified, actual = retrievedItem)
        assertNotEquals(actual = retrievedItem, illegal = itemCreated)
        assertEquals(
            expected = itemNameModified,
            actual = retrievedItem.encName?.let { decryptUseCase(it, retrievedItem.id, String::class) }?.data,
        )
        assertEquals(
            expected = itemColorModified,
            actual = retrievedItem.encColor?.let { decryptUseCase(it, retrievedItem.id, String::class) }?.data,
        )
        assertTrue(actual = iconDir.listFiles()!!.isNotEmpty())
        assertNull(actual = itemCreated.iconId)
        assertNotNull(actual = retrievedItem.iconId)
    }

    @Test
    fun update_fields_with_file_test(): TestResult = runTest {
        val itemCreated = createItem()
        val fileToRemoveId = testUUIDs[0]
        val existingField = createItemFieldData(
            name = "toRemove.jpeg",
            position = 0.0,
            value = "$fileToRemoveId|jpeg",
            kind = SafeItemFieldKind.Photo,
        )
        val key0 = safeItemKeyRepository.getSafeItemKey(itemCreated.id)
        addFieldUseCase(itemId = itemCreated.id, itemFieldData = existingField)
        fileRepository.addFile(testUUIDs[0], cryptoRepository.encrypt(key0, EncryptEntry(iconSample)), safeId = firstSafeId)
        assertEquals(expected = 1, actual = safeItemFieldRepository.getSafeItemFields(itemCreated.id).size)

        val newFileId = testUUIDs[1]
        val newField = createItemFieldData(
            kind = SafeItemFieldKind.Photo,
            name = "newFile.jpeg",
            value = "$newFileId|jpeg",
            position = 1.0,
        )

        // No update here.
        val data = UpdateItemUseCase.UpdateData(
            name = UpdateState.Unchanged(""),
            icon = UpdateState.Unchanged(null),
            color = UpdateState.Unchanged(null),
        )

        val itemModified = updateItemUseCase.invoke(
            itemId = itemCreated.id,
            updateData = data,
            fields = listOf(newField),
            fileSavingData = listOf(
                FileSavingData.ToRemove(fileToRemoveId, null),
                FileSavingData.ToSave(
                    fileId = newFileId,
                    getStream = { iconSample.inputStream() },
                ),
            ),
        ).data!!
        val fields = safeItemFieldRepository.getSafeItemFields(itemModified.id)
        assertEquals(expected = 1, actual = fields.size)
        val oldFile = fileRepository.getFile(fileToRemoveId.toString())
        assertFalse(oldFile.exists())

        val newFile = fileRepository.getFile(newFileId.toString())
        assert(newFile.exists())
    }

    @Test
    fun update_fields_item_test(): TestResult = runTest {
        val itemCreated = createItem()
        val itemFieldData = createItemFieldData(
            name = "Initial",
            position = 0.0,
            value = "Initial",
            kind = SafeItemFieldKind.Note,
        )
        addFieldUseCase(itemId = itemCreated.id, itemFieldData = itemFieldData)
        assertEquals(1, safeItemFieldRepository.getSafeItemFields(itemCreated.id).size)

        val newFieldsData = listOf(
            createItemFieldData(
                kind = SafeItemFieldKind.Password,
                name = "password",
                value = "1234",
                position = 0.0,
            ),
            createItemFieldData(
                kind = SafeItemFieldKind.Text,
                name = "name",
                value = "text",
                position = 0.0,
            ),
        )

        // No update here.
        val data = UpdateItemUseCase.UpdateData(
            name = UpdateState.Unchanged(""),
            icon = UpdateState.Unchanged(null),
            color = UpdateState.Unchanged(null),
        )

        val itemModified = updateItemUseCase.invoke(
            itemId = itemCreated.id,
            updateData = data,
            fields = newFieldsData,
            fileSavingData = listOf(),
        ).data!!

        val fields = safeItemFieldRepository.getSafeItemFields(itemModified.id)
        assertEquals(expected = newFieldsData.size, actual = fields.size)
        assertEquals(
            expected = newFieldsData.map { it.name },
            actual = fields.map {
                it.encName?.let { name -> decryptUseCase(name, itemModified.id, String::class) }?.data
            },
        )
        assertEquals(
            expected = newFieldsData.map { it.value },
            actual = fields.map {
                it.encValue?.let { name -> decryptUseCase(name, itemModified.id, String::class) }?.data
            },
        )

        assertEquals(
            newFieldsData.filter { it.isItemIdentifier }.size,
            fields.filter { it.isItemIdentifier }.size,
        )
    }

    @Test
    fun update_item_icon_test(): TestResult = runTest {
        val icon = LbcResourcesHelper.readResourceAsBytes("icon_259_194.jpeg")
        val item = createItemUseCase.test(icon = icon)

        val data = UpdateItemUseCase.UpdateData(
            name = UpdateState.Unchanged(""),
            icon = UpdateState.ModifiedTo(icon),
            color = UpdateState.Unchanged(null),
        )

        updateItemUseCase(
            itemId = item.id,
            updateData = data,
        )

        assertEquals(1, iconDir.list()!!.size)
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
