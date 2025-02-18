package studio.lunabee.onesafe.usecase.item

import com.lunabee.lbcore.model.LBResult
import com.lunabee.lbextensions.lazyFast
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.threeten.extra.MutableClock
import studio.lunabee.onesafe.AppAndroidTestUtils
import studio.lunabee.onesafe.domain.Constant
import studio.lunabee.onesafe.domain.common.FieldIdProvider
import studio.lunabee.onesafe.domain.common.FileIdProvider
import studio.lunabee.onesafe.domain.common.ItemIdProvider
import studio.lunabee.onesafe.domain.model.crypto.EncryptEntry
import studio.lunabee.onesafe.domain.model.safeitem.ItemOrder
import studio.lunabee.onesafe.domain.model.safeitem.SafeItem
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemField
import studio.lunabee.onesafe.domain.model.search.ItemFieldDataToIndex
import studio.lunabee.onesafe.domain.repository.IndexWordEntryRepository
import studio.lunabee.onesafe.domain.repository.SafeItemFieldRepository
import studio.lunabee.onesafe.domain.repository.SafeItemKeyRepository
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.domain.usecase.DeleteIconUseCase
import studio.lunabee.onesafe.domain.usecase.DuplicateItemUseCase
import studio.lunabee.onesafe.domain.usecase.EncryptFieldsUseCase
import studio.lunabee.onesafe.domain.usecase.GetIconUseCase
import studio.lunabee.onesafe.domain.usecase.item.AddFieldUseCase
import studio.lunabee.onesafe.domain.usecase.item.ComputeItemAlphaIndexUseCase
import studio.lunabee.onesafe.domain.usecase.item.CreateItemUseCase
import studio.lunabee.onesafe.domain.usecase.item.ItemDecryptUseCase
import studio.lunabee.onesafe.domain.usecase.search.CreateIndexWordEntriesFromItemFieldUseCase
import studio.lunabee.onesafe.domain.usecase.search.CreateIndexWordEntriesFromItemUseCase
import studio.lunabee.onesafe.domain.utils.SafeItemBuilder
import studio.lunabee.onesafe.extension.iconSample
import studio.lunabee.onesafe.repository.datasource.IconLocalDataSource
import studio.lunabee.onesafe.test.CommonTestUtils.createItemFieldData
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltUnitTest
import studio.lunabee.onesafe.test.assertContentNotEquals
import studio.lunabee.onesafe.test.assertFailure
import studio.lunabee.onesafe.test.assertSuccess
import studio.lunabee.onesafe.test.firstSafeId
import studio.lunabee.onesafe.test.test
import studio.lunabee.onesafe.test.testUUIDs
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject
import kotlin.random.Random
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.typeOf
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.toJavaDuration

class ExpectedDuplicatedData(
    val safeItem: SafeItem,
    val id: UUID,
    val parentId: UUID?,
    val position: Double,
    val fieldCount: Int = 0,
)

@HiltAndroidTest
class DuplicateItemUseCaseTest : OSHiltUnitTest() {
    @get:Rule override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)
    override val initialTestState: InitialTestState = InitialTestState.Home()

    @Inject lateinit var createItemUseCase: CreateItemUseCase

    @Inject lateinit var decryptUseCase: ItemDecryptUseCase

    @Inject lateinit var safeItemKeyRepository: SafeItemKeyRepository

    @Inject lateinit var safeItemRepository: SafeItemRepository

    @Inject lateinit var getIconUseCase: GetIconUseCase

    @Inject lateinit var addFieldUseCase: AddFieldUseCase

    @Inject lateinit var safeItemBuilder: SafeItemBuilder

    @Inject lateinit var createIndexWordEntriesUseCase: CreateIndexWordEntriesFromItemUseCase

    @Inject lateinit var computeItemAlphaIndexUseCase: ComputeItemAlphaIndexUseCase

    @Inject lateinit var safeItemFieldRepository: SafeItemFieldRepository

    @Inject lateinit var iconLocalDataSource: IconLocalDataSource

    @Inject lateinit var deleteIconUseCase: DeleteIconUseCase

    @Inject lateinit var itemIdProvider: ItemIdProvider

    @Inject lateinit var encryptFieldsUseCase: EncryptFieldsUseCase

    @Inject lateinit var createIndexWordEntriesFromItemUseCase: CreateIndexWordEntriesFromItemUseCase

    @Inject lateinit var createIndexWordEntriesFromItemFieldUseCase: CreateIndexWordEntriesFromItemFieldUseCase

    @Inject lateinit var indexWordEntryRepository: IndexWordEntryRepository

    @Inject lateinit var fileIdProvider: FileIdProvider

    @Inject lateinit var fieldIdProvider: FieldIdProvider

    private val duplicateClock = MutableClock.epochUTC().apply { add(1, ChronoUnit.DAYS) }

    private val duplicateItemUseCase: DuplicateItemUseCase by lazyFast {
        DuplicateItemUseCase(
            cryptoRepository = cryptoRepository,
            safeItemKeyRepository = safeItemKeyRepository,
            safeItemRepository = safeItemRepository,
            getIconUseCase = getIconUseCase,
            safeItemFieldRepository = safeItemFieldRepository,
            duplicateNameTransform = { it },
            itemIdProvider = itemIdProvider,
            safeItemBuilder = safeItemBuilder,
            deleteIconUseCase = deleteIconUseCase,
            encryptFieldsUseCase = encryptFieldsUseCase,
            createIndexWordEntriesFromItemUseCase = createIndexWordEntriesFromItemUseCase,
            createIndexWordEntriesFromItemFieldUseCase = createIndexWordEntriesFromItemFieldUseCase,
            fileIdProvider = fileIdProvider,
            fileRepository = fileRepository,
            fieldIdProvider = fieldIdProvider,
            computeItemAlphaIndexUseCase = computeItemAlphaIndexUseCase,
            clock = duplicateClock,
        )
    }

    @Test
    fun duplicate_empty_item(): TestResult = runTest {
        val originalItem = createItemUseCase(
            name = null,
            parentId = null,
            isFavorite = false,
            icon = null,
            color = null,
        ).data!!

        val duplicateResult = duplicateItemUseCase(originalItem.id)
        assertSuccess(duplicateResult)
        val duplicatedItem = duplicateResult.successData
        assertEquals(originalItem.indexAlpha, duplicateResult.successData.indexAlpha)
        testDuplicatedData(originalItem, duplicatedItem)
    }

    @Test
    fun duplicate_transform_name_item(): TestResult = runTest {
        val originalItem = createItemUseCase(
            name = "123",
            parentId = null,
            isFavorite = false,
            icon = null,
            color = null,
        ).data!!

        val useCase = DuplicateItemUseCase(
            cryptoRepository = cryptoRepository,
            safeItemKeyRepository = safeItemKeyRepository,
            safeItemRepository = safeItemRepository,
            getIconUseCase = getIconUseCase,
            safeItemFieldRepository = safeItemFieldRepository,
            duplicateNameTransform = { "abc $it def" },
            itemIdProvider = itemIdProvider,
            safeItemBuilder = safeItemBuilder,
            deleteIconUseCase = deleteIconUseCase,
            encryptFieldsUseCase = encryptFieldsUseCase,
            createIndexWordEntriesFromItemUseCase = createIndexWordEntriesFromItemUseCase,
            createIndexWordEntriesFromItemFieldUseCase = createIndexWordEntriesFromItemFieldUseCase,
            fileRepository = fileRepository,
            fileIdProvider = fileIdProvider,
            fieldIdProvider = fieldIdProvider,
            computeItemAlphaIndexUseCase = computeItemAlphaIndexUseCase,
            clock = testClock,
        )

        val duplicateResult = useCase(originalItem.id)
        assertSuccess(duplicateResult)
        val duplicatedItem = duplicateResult.successData
        val duplicatedName = decryptUseCase(duplicatedItem.encName!!, duplicatedItem.id, String::class).data!!

        assertEquals("abc 123 def", duplicatedName)
        assertEquals(1.0, duplicateResult.successData.indexAlpha)
    }

    @Test
    fun duplicate_file_fields_test(): TestResult = runTest {
        // Create item with file field
        val originalItem = createItemUseCase.test()
        addFieldUseCase(
            itemId = originalItem.id,
            itemFieldData = AppAndroidTestUtils.createItemFieldPhotoData(testUUIDs[0].toString()),
        )
        val key0 = safeItemKeyRepository.getSafeItemKey(originalItem.id)
        fileRepository.addFile(testUUIDs[0], cryptoRepository.encrypt(key0, EncryptEntry(iconSample)), safeId = firstSafeId)
        assert(fileRepository.getFile(testUUIDs[0].toString()).exists())

        // Duplicate the item
        val useCase = duplicateItemUseCase
        val duplicateResult = useCase(originalItem.id)
        assertSuccess(duplicateResult)

        // Get the duplicated field
        val duplicatedField = safeItemFieldRepository.getSafeItemFields(duplicateResult.successData.id).first()

        // Assert that the field is linked to a different file than the original field and the file exists
        val decryptedValue = duplicatedField.encValue?.let { decryptUseCase(it, duplicatedField.itemId, String::class) }?.data!!
        val duplicateFileId = decryptedValue.substringBefore(Constant.FileTypeExtSeparator)
        assertNotEquals(testUUIDs[0].toString(), duplicateFileId)
        assert(fileRepository.getFile(testUUIDs[0].toString()).exists())

        // Try to decrypt the file, assert that the plainData is the same than the original
        val encDecryptedFileData = fileRepository.getFile(duplicateFileId).readBytes()
        val plainData = decryptUseCase(encDecryptedFileData, duplicatedField.itemId, ByteArray::class).data
        assertContentEquals(iconSample, plainData)
    }

    @Test
    fun duplicate_filled_item(): TestResult = runTest {
        val parentItem = createItemUseCase(
            name = null,
            parentId = null,
            isFavorite = false,
            icon = null,
            color = null,
        ).data!!

        val originalItem = createItemUseCase(
            name = UUID.randomUUID().toString(),
            parentId = parentItem.id,
            isFavorite = Random.nextBoolean(),
            icon = iconSample,
            color = "#12345678",
        ).data!!

        val duplicateResult = duplicateItemUseCase(originalItem.id)
        assertSuccess(duplicateResult)
        val duplicatedItem = duplicateResult.successData

        testDuplicatedData(originalItem, duplicatedItem)
    }

    @Test
    fun duplicate_item_with_fields(): TestResult = runTest {
        val originalItem = createItemUseCase(
            name = UUID.randomUUID().toString(),
            parentId = null,
            isFavorite = Random.nextBoolean(),
            icon = iconSample,
            color = "#12345678",
        ).data!!

        val fields = (0 until 10).map { createItemFieldData(value = it.toString()) }
        addFieldUseCase(originalItem.id, fields)
        testClock.add(10.milliseconds.toJavaDuration()) // emulate some delay before duplicate
        val duplicateResult = duplicateItemUseCase(originalItem.id)
        assertSuccess(duplicateResult)
        val duplicatedItem = duplicateResult.successData

        testDuplicatedData(originalItem, duplicatedItem, 10)
    }

    @Test
    fun duplicate_item_with_empty_children(): TestResult = runTest {
        val createItemUseCase = createItemUseCaseWithIds(
            listOf(
                UUID.randomUUID(), // root
                UUID.randomUUID(), // child_to_duplicate
                UUID.randomUUID(), // child_2
                UUID.randomUUID(), // grandchild
                UUID.randomUUID(), // grandchild_2
                UUID.randomUUID(), // grand_grand_child
            ),
        )

        val rootItem = createItemUseCase(
            name = null,
            parentId = null,
            isFavorite = false,
            icon = null,
            color = null,
        ).data!!

        val originalItem = createItemUseCase(
            name = null,
            parentId = rootItem.id,
            isFavorite = false,
            icon = null,
            color = null,
        ).data!!

        createItemUseCase(
            name = null,
            parentId = rootItem.id,
            isFavorite = false,
            icon = null,
            color = null,
            position = 10.0,
        ).data!!

        val grandChild = createItemUseCase(
            name = null,
            parentId = originalItem.id,
            isFavorite = false,
            icon = null,
            color = null,
            position = 2.0,
        ).data!!

        val grandChild2 = createItemUseCase(
            name = null,
            parentId = originalItem.id,
            isFavorite = false,
            icon = null,
            color = null,
            position = 5.0,
        ).data!!

        val grandGrandChild = createItemUseCase(
            name = null,
            parentId = grandChild.id,
            isFavorite = false,
            icon = null,
            color = null,
            position = 3.0,
        ).data!!

        val duplicateResult = duplicateItemUseCase(originalItem.id)
        assertSuccess(duplicateResult)
        val duplicatedItem = duplicateResult.successData

        val childDataMap = mapOf(
            originalItem.id to ExpectedDuplicatedData(originalItem, testUUIDs[0], originalItem.parentId, 5.0),
            grandChild.id to ExpectedDuplicatedData(grandChild, testUUIDs[1], testUUIDs[0], 2.0),
            grandChild2.id to ExpectedDuplicatedData(grandChild2, testUUIDs[2], testUUIDs[0], 5.0),
            grandGrandChild.id to ExpectedDuplicatedData(grandGrandChild, testUUIDs[3], testUUIDs[1], 3.0),
        )

        testDuplicatedData(
            originalItem = originalItem,
            duplicatedItem = duplicatedItem,
            childDataMap = childDataMap,
        )
    }

    @Test
    fun duplicate_item_with_filled_children(): TestResult = runTest {
        val createItemUseCase = createItemUseCaseWithIds(
            listOf(
                UUID.randomUUID(), // originalItem
                UUID.randomUUID(), // childItem
            ),
        )

        val originalItem = createItemUseCase(
            name = "originalItem",
            parentId = null,
            isFavorite = false,
            icon = null,
            color = null,
        ).data!!

        val childItem = createItemUseCase(
            name = "childItem",
            parentId = originalItem.id,
            isFavorite = Random.nextBoolean(),
            icon = iconSample,
            color = "#12345678",
            position = Random.nextDouble(),
        ).data!!

        val duplicateResult = duplicateItemUseCase(originalItem.id)
        assertSuccess(duplicateResult)
        val duplicatedItem = duplicateResult.successData

        val childDataMap = mapOf(
            originalItem.id to ExpectedDuplicatedData(originalItem, testUUIDs[0], null, 1.0),
            childItem.id to ExpectedDuplicatedData(childItem, testUUIDs[1], testUUIDs[0], childItem.position),
        )

        testDuplicatedData(
            originalItem = originalItem,
            duplicatedItem = duplicatedItem,
            childDataMap = childDataMap,
        )
    }

    @Test
    fun duplicate_item_with_children_with_field(): TestResult = runTest {
        val createItemUseCase = createItemUseCaseWithIds(
            listOf(
                UUID.randomUUID(), // originalItem
                UUID.randomUUID(), // childItem
            ),
        )

        val originalItem = createItemUseCase(
            name = "originalItem",
            parentId = null,
            isFavorite = false,
            icon = null,
            color = null,
        ).data!!

        val childItem = createItemUseCase(
            name = "childItem",
            parentId = originalItem.id,
            isFavorite = false,
            icon = null,
            color = null,
            position = 0.0,
        ).data!!

        val childDataMap = mapOf(
            originalItem.id to ExpectedDuplicatedData(originalItem, id = testUUIDs[0], parentId = null, position = 1.0),
            childItem.id to ExpectedDuplicatedData(
                safeItem = childItem,
                id = testUUIDs[1],
                parentId = testUUIDs[0],
                fieldCount = 10,
                position = childItem.position,
            ),
        )

        val fields = (0 until 10).map { createItemFieldData(value = it.toString()) }
        addFieldUseCase(childItem.id, fields)

        testClock.add(10.milliseconds.toJavaDuration()) // emulate some delay before duplicate

        val duplicateResult = duplicateItemUseCase(originalItem.id)
        assertSuccess(duplicateResult)
        val duplicatedItem = duplicateResult.successData

        testDuplicatedData(
            originalItem = originalItem,
            duplicatedItem = duplicatedItem,
            childDataMap = childDataMap,
        )
    }

    @Test
    fun duplicate_item_storage_fail(): TestResult = runTest {
        val createItemUseCase = createItemUseCaseWithIds(
            listOf(
                UUID.randomUUID(),
                testUUIDs[0], // use expected duplicated item id
            ),
        )

        val originalItem = createItemUseCase(
            name = null,
            parentId = null,
            isFavorite = false,
            icon = null,
            color = null,
        ).data!!

        createItemUseCase(
            name = null,
            parentId = null,
            isFavorite = false,
            icon = null,
            color = null,
        ).data!!

        val duplicateResult = duplicateItemUseCase(originalItem.id)
        assertFailure(duplicateResult)
    }

    @Test
    fun duplicate_item_storage_fail_clear_icon(): TestResult = runTest {
        val createItemUseCase = createItemUseCaseWithIds(
            listOf(
                UUID.randomUUID(),
                testUUIDs[0], // use expected duplicated item id
            ),
        )

        val originalItem = createItemUseCase(
            name = null,
            parentId = null,
            isFavorite = false,
            icon = iconSample,
            color = null,
        ).data!!

        createItemUseCase(
            name = null,
            parentId = null,
            isFavorite = false,
            icon = null,
            color = null,
        )

        duplicateItemUseCase(originalItem.id)

        assertTrue(iconLocalDataSource.getIcon(testUUIDs[0].toString()).exists())
        assertFalse(iconLocalDataSource.getIcon(testUUIDs[1].toString()).exists())
        assertEquals(1, iconLocalDataSource.getIcon(testUUIDs[0].toString()).parentFile!!.listFiles()!!.size)
    }

    private suspend fun testDuplicatedData(
        originalItem: SafeItem,
        duplicatedItem: SafeItem,
        fieldsCount: Int? = null,
        childDataMap: Map<UUID, ExpectedDuplicatedData>? = null,
    ) {
        originalItem.encName?.let { decryptUseCase(it, originalItem.id, String::class) }?.let {
            println("Testing data of ${it.data}")
        }

        assertNotEquals(originalItem.id, duplicatedItem.id)
        val oriKey = safeItemKeyRepository.getSafeItemKey(originalItem.id)
        val dupKey = safeItemKeyRepository.getSafeItemKey(duplicatedItem.id)
        assertNotEquals(oriKey.id, dupKey.id)
        assertNotEquals(oriKey.encValue, dupKey.encValue)

        assertNotNull(duplicatedItem)
        assertNotEquals(originalItem.id, duplicatedItem.id)

        if (originalItem.iconId != null) {
            assertNotEquals(originalItem.iconId, duplicatedItem.iconId)
            val oriIcon = (originalItem.iconId?.let { getIconUseCase(it, originalItem.id) } as LBResult.Success).data!!
            val dupIcon = (duplicatedItem.iconId?.let { getIconUseCase(it, duplicatedItem.id) } as LBResult.Success).data!!
            assertContentEquals(oriIcon, dupIcon)
        } else {
            assertNull(duplicatedItem.iconId)
        }

        val expectedChildData = childDataMap?.get(originalItem.id)
        if (expectedChildData != null) {
            assertEquals(expectedChildData.parentId, duplicatedItem.parentId)
            assertEquals(expectedChildData.id, duplicatedItem.id)
            assertEquals(expectedChildData.position, duplicatedItem.position)
        } else {
            assertEquals(originalItem.parentId, duplicatedItem.parentId)
            assertEquals(originalItem.position + 1, duplicatedItem.position)
        }

        assertFalse(duplicatedItem.isFavorite)
        assertEquals(Instant.now(duplicateClock), duplicatedItem.createdAt)
        assertEquals(Instant.now(duplicateClock), duplicatedItem.updatedAt)

        // Remove non duplicated properties from test
        val itemProperties = SafeItem::class.memberProperties.filterNot { property ->
            property.name in listOf(
                "id",
                "position",
                "iconId",
                "parentId",
                "isFavorite",
                "indexAlpha",
                "createdAt",
                "updatedAt",
            )
        } // handled before
        println("Checking item ${originalItem.id} vs ${duplicatedItem.id}...")
        assertProperties(itemProperties, originalItem, duplicatedItem)

        val itemFieldCount = fieldsCount ?: childDataMap?.get(originalItem.id)?.fieldCount

        testDuplicatedFields(
            originalId = originalItem.id,
            duplicatedId = duplicatedItem.id,
            fieldsCount = itemFieldCount,
        )
        testDuplicatedChildren(
            originalId = originalItem.id,
            duplicatedId = duplicatedItem.id,
            childDataMap = childDataMap,
        )

        val children = childDataMap?.values?.map { it.safeItem } ?: listOf()
        testDuplicatedDataSearchIndex(
            duplicatedItems = children + duplicatedItem,
        )
    }

    private suspend fun testDuplicatedFields(originalId: UUID, duplicatedId: UUID, fieldsCount: Int?) {
        val originalFields = safeItemFieldRepository.getSafeItemFields(originalId)
        val duplicatedFields = safeItemFieldRepository.getSafeItemFields(duplicatedId)

        fieldsCount?.let {
            assertEquals(it, originalFields.size) // make sure we have added fields
        }
        assertEquals(originalFields.size, duplicatedFields.size)

        val fieldProperties = SafeItemField::class.memberProperties.filterNot {
            it.name in listOf(
                "id",
                "position",
                "itemId",
                "showPrediction",
                "updatedAt",
            )
        }
        originalFields.indices.forEach { idx ->
            val originalField = originalFields[idx]
            val duplicatedField = duplicatedFields[idx]

            assertNotEquals(originalField.id, duplicatedField.id)
            assertEquals(originalField.position, duplicatedField.position)
            assertNotEquals(originalField.itemId, duplicatedField.itemId)
            assertEquals(originalField.showPrediction, duplicatedField.showPrediction)
            assert(originalField.updatedAt < duplicatedField.updatedAt) {
                "originalField.updatedAt (${originalField.updatedAt}) is greater or equals to duplicatedField.updatedAt " +
                    "(${duplicatedField.updatedAt})"
            }

            println("Checking field ${originalField.id} vs ${duplicatedField.id}...")
            assertProperties(fieldProperties, originalField, duplicatedField, originalId, duplicatedId)
        }
    }

    private suspend fun testDuplicatedChildren(
        originalId: UUID,
        duplicatedId: UUID,
        childDataMap: Map<UUID, ExpectedDuplicatedData>?,
    ) {
        val childrenOriginal = safeItemRepository.getChildren(originalId, ItemOrder.Position, firstSafeId)
        val childrenDuplicated = safeItemRepository.getChildren(duplicatedId, ItemOrder.Position, firstSafeId)

        println("\tChecking children of $originalId vs $duplicatedId")

        assertEquals(childrenOriginal.size, childrenDuplicated.size)

        childrenOriginal.indices.forEach { idx ->
            val originalItem = childrenOriginal[idx]
            testDuplicatedData(
                originalItem = originalItem,
                duplicatedItem = childrenDuplicated[idx],
                childDataMap = childDataMap,
            )
        }
    }

    /**
     * Test if the search index created during the duplication correspond to what we expect
     * - Create index via UseCase for duplicatedItems and fields,
     * - Get all searchIndex, check if it contains all the expected index
     */
    private suspend fun testDuplicatedDataSearchIndex(
        duplicatedItems: List<SafeItem>,
    ) {
        val itemPlainNames = duplicatedItems.map { item ->
            item.id to item.encName?.let { encName -> decryptUseCase(encName, item.id, String::class) }?.data
        }
        val duplicatedFieldDataToIndex = duplicatedItems.flatMap { item ->
            safeItemFieldRepository.getSafeItemFields(item.id)
        }.mapNotNull { field ->
            val value = field.encValue?.let { value -> decryptUseCase(value, field.itemId, String::class) }?.data
            value?.let {
                ItemFieldDataToIndex(
                    value = value,
                    isSecured = field.isSecured,
                    itemId = field.itemId,
                    fieldId = field.id,
                )
            }
        }
        val itemIndexWordEntries = itemPlainNames.mapNotNull { (id, name) ->
            name?.let {
                createIndexWordEntriesFromItemUseCase(name, id)
            }
        }.flatten()
        val fieldIndexWordEntries = createIndexWordEntriesFromItemFieldUseCase(duplicatedFieldDataToIndex)
        val savedIndex = cryptoRepository.decryptIndexWord(indexWordEntryRepository.getAll(firstSafeId).first().map { it.encWord })
        val expectedIndex = cryptoRepository.decryptIndexWord(
            itemIndexWordEntries.map { it.encWord } + fieldIndexWordEntries.map { it.encWord },
        )
        assertTrue(savedIndex.containsAll(expectedIndex))
    }

    private suspend fun <T> assertProperties(
        properties: Collection<KProperty1<T, *>>,
        original: T,
        duplicated: T,
        originalId: UUID = (original as SafeItem).id,
        duplicatedId: UUID = (duplicated as SafeItem).id,
    ) {
        val byteArrayType = typeOf<ByteArray?>()
        properties.forEach { property ->
            if (property.returnType == byteArrayType) {
                val originalPropEnc = property.get(original) as ByteArray?
                val duplicatedPropEnc = property.get(duplicated) as ByteArray?

                println("\tChecking property ${property.name}")

                if (originalPropEnc != null) {
                    assertContentNotEquals(
                        originalPropEnc,
                        duplicatedPropEnc,
                        "Property ${property.name} not equal failed",
                    )

                    val originalProp = decryptUseCase(originalPropEnc, originalId, ByteArray::class).data
                    val duplicatedProp = decryptUseCase(duplicatedPropEnc as ByteArray, duplicatedId, ByteArray::class).data

                    assertContentEquals(originalProp, duplicatedProp, "Property ${property.name} equality failed")
                } else {
                    assertNull(duplicatedPropEnc, "Property ${property.name} nullity failed")
                }
            } else {
                assertEquals(
                    property.get(original),
                    property.get(duplicated),
                    "Property ${property.name} equality failed",
                )
            }
        }
    }

    private fun createItemUseCaseWithIds(ids: List<UUID>): CreateItemUseCase {
        val idDeck = ArrayDeque(ids)
        return CreateItemUseCase(
            safeItemBuilder = safeItemBuilder,
            safeItemRepository = safeItemRepository,
            createIndexWordEntriesFromItemUseCase = createIndexWordEntriesUseCase,
            itemIdProvider = ItemIdProvider {
                idDeck.removeFirst()
            },
            computeItemAlphaIndexUseCase = computeItemAlphaIndexUseCase,
            clock = testClock,
            safeRepository = safeRepository,
        )
    }
}
