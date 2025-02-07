package studio.lunabee.onesafe.usecase.item

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.onesafe.test.OSHiltUnitTest
import studio.lunabee.onesafe.domain.common.FieldIdProvider
import studio.lunabee.onesafe.domain.common.ItemIdProvider
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.model.safeitem.ItemFieldData
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.domain.repository.SafeItemFieldRepository
import studio.lunabee.onesafe.domain.repository.SafeItemKeyRepository
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.domain.usecase.item.AddFieldUseCase
import studio.lunabee.onesafe.domain.usecase.item.ComputeItemAlphaIndexUseCase
import studio.lunabee.onesafe.domain.usecase.item.CreateItemUseCase
import studio.lunabee.onesafe.domain.usecase.item.ItemDecryptUseCase
import studio.lunabee.onesafe.domain.usecase.search.CreateIndexWordEntriesFromItemUseCase
import studio.lunabee.onesafe.domain.utils.SafeItemBuilder
import studio.lunabee.onesafe.error.OSStorageError
import studio.lunabee.onesafe.test.CommonTestUtils.createItemFieldData
import studio.lunabee.onesafe.test.IncrementalIdProvider
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.assertFailure
import studio.lunabee.onesafe.test.testUUIDs
import java.util.UUID
import javax.inject.Inject
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

@HiltAndroidTest
class ItemUseCaseTest : OSHiltUnitTest() {

    @get:Rule
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.Home()

    @Inject lateinit var safeItemRepository: SafeItemRepository

    @Inject lateinit var safeItemFieldRepository: SafeItemFieldRepository

    @Inject lateinit var safeItemKeyRepository: SafeItemKeyRepository

    @Inject lateinit var createItemUseCase: CreateItemUseCase

    @Inject lateinit var createIndexWordEntriesUseCase: CreateIndexWordEntriesFromItemUseCase

    @Inject lateinit var addFieldUseCase: AddFieldUseCase

    @Inject lateinit var decryptUseCase: ItemDecryptUseCase

    @Inject lateinit var safeItemBuilder: SafeItemBuilder

    @Inject lateinit var fieldIdProvider: FieldIdProvider

    @Inject lateinit var computeItemAlphaIndexUseCase: ComputeItemAlphaIndexUseCase

    @Test
    fun create_get_decrypt_item(): TestResult = runTest {
        val expectedName = "item_name"
        val expectedColor = "color_hex"

        val newItem = createItemUseCase(
            name = expectedName,
            parentId = null,
            isFavorite = false,
            icon = null,
            color = expectedColor,
        ).data!!

        val retrievedItem = safeItemRepository.getSafeItem(newItem.id)

        assertEquals(newItem, retrievedItem)
        assertEquals(expectedName, retrievedItem.encName?.let { decryptUseCase(it, newItem.id, String::class) }?.data)
        assertNull(retrievedItem.iconId)
        assertEquals(expectedColor, retrievedItem.encColor?.let { decryptUseCase(it, newItem.id, String::class) }?.data)
    }

    @Test
    fun addFieldUseCase(): TestResult = runTest {
        val newItem = createItemUseCase(
            name = "item_name",
            parentId = null,
            isFavorite = false,
            icon = null,
            color = "color_hex",
        ).data!!

        val expectedName = "field_name"
        val expectedPlaceholder = "field_placeholder"
        val expectedValue = "field_value"
        val expectedKind = SafeItemFieldKind.Unknown("field_unknown_kind")
        val expectedIsIdentifier = true
        val expectedIsSecured = false

        val expectedFields = Array(10) {
            val itemFieldData = ItemFieldData(
                id = fieldIdProvider(),
                name = expectedName,
                position = it.toDouble(),
                placeholder = expectedPlaceholder,
                value = expectedValue,
                kind = expectedKind,
                showPrediction = false,
                isItemIdentifier = expectedIsIdentifier,
                formattingMask = null,
                secureDisplayMask = null,
                isSecured = expectedIsSecured,
            )
            addFieldUseCase(
                itemId = newItem.id,
                itemFieldData = itemFieldData,
            )
        }.toList()

        val retrievedFields = safeItemFieldRepository.getSafeItemFields(newItem.id)
        assertContentEquals(expectedFields.map { it.data }, retrievedFields)

        retrievedFields.forEach { field ->
            assertEquals(expectedName, field.encName?.let { decryptUseCase(it, newItem.id, String::class) }?.data)
            assertEquals(
                expectedPlaceholder,
                field.encPlaceholder?.let { decryptUseCase(it, newItem.id, String::class) }?.data,
            )
            assertEquals(expectedValue, field.encValue?.let { decryptUseCase(it, newItem.id, String::class) }?.data)
            assertEquals(
                expectedKind,
                field.encKind?.let { decryptUseCase(it, newItem.id, SafeItemFieldKind::class) }?.data,
            )
            assertEquals(expectedIsIdentifier, field.isItemIdentifier)
            assertEquals(expectedIsSecured, field.isSecured)
        }
    }

    @Test
    fun addFieldUseCase_no_key(): TestResult = runTest {
        val itemFieldData = createItemFieldData(
            name = "field_name",
            position = 0.0,
            placeholder = "field_placeholder",
            value = "field_value",
            kind = SafeItemFieldKind.Email,
        )
        val result = addFieldUseCase(
            itemId = testUUIDs[0],
            itemFieldData = itemFieldData,
        )

        assertFailure(result)
        assertEquals(OSStorageError.Code.ITEM_KEY_NOT_FOUND, (result.throwable as OSStorageError).code)
    }

    @Test
    fun getSafeItem_no_item(): TestResult = runTest {
        val error = assertFailsWith<OSStorageError> {
            safeItemRepository.getSafeItem(testUUIDs[0])
        }
        assertEquals(OSStorageError.Code.ITEM_NOT_FOUND, (error.code))
    }

    @Test
    fun createItem_position_integer(): TestResult = runTest {
        val newItem = createItemUseCase(
            name = null,
            parentId = null,
            isFavorite = false,
            icon = null,
            color = null,
        ).data!!

        val newItem2 = createItemUseCase(
            name = null,
            parentId = null,
            isFavorite = false,
            icon = null,
            color = null,
        ).data!!

        assertEquals(0.0, newItem.position)
        assertEquals(1.0, newItem2.position)
    }

    @Test
    fun createItem_position_float(): TestResult = runTest {
        val safeItemRepository = object : SafeItemRepository by this@ItemUseCaseTest.safeItemRepository {
            override suspend fun getHighestChildPosition(parentId: UUID?, safeId: SafeId): Double = 1.1
        }

        val createItemUseCase = CreateItemUseCase(
            safeItemBuilder = safeItemBuilder,
            safeItemRepository = safeItemRepository,
            createIndexWordEntriesFromItemUseCase = createIndexWordEntriesUseCase,
            itemIdProvider = ItemIdProvider(IncrementalIdProvider()),
            computeItemAlphaIndexUseCase = computeItemAlphaIndexUseCase,
            clock = testClock,
            safeRepository = safeRepository,
        )

        val newItem = createItemUseCase(
            name = null,
            parentId = null,
            isFavorite = false,
            icon = null,
            color = null,
        ).data!!

        assertEquals(2.0, newItem.position)
    }
}
