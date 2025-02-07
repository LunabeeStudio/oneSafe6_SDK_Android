package studio.lunabee.onesafe.usecase.item

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.onesafe.test.OSHiltUnitTest
import studio.lunabee.onesafe.domain.model.safeitem.ItemOrder
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.domain.usecase.item.CreateItemUseCase
import studio.lunabee.onesafe.domain.usecase.item.MoveToBinItemUseCase
import studio.lunabee.onesafe.domain.usecase.item.RemoveDeletedItemUseCase
import studio.lunabee.onesafe.domain.usecase.item.RestoreItemUseCase
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.firstSafeId
import studio.lunabee.onesafe.test.test
import javax.inject.Inject
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@HiltAndroidTest
class RestoreItemUseCaseTest : OSHiltUnitTest() {
    @get:Rule override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.Home()

    @Inject lateinit var restoreItemUseCase: RestoreItemUseCase

    @Inject lateinit var safeItemRepository: SafeItemRepository

    @Inject lateinit var createItemUseCase: CreateItemUseCase

    @Inject lateinit var removeDeletedItemUseCase: RemoveDeletedItemUseCase

    @Inject lateinit var moveToBinItemUseCase: MoveToBinItemUseCase

    @Test
    fun restoreItemUseCase_without_child_test(): TestResult = runTest {
        val expectedItem = createItemUseCase.test()
        assertNull(expectedItem.deletedAt)
        moveToBinItemUseCase(expectedItem)
        val deletedItem = safeItemRepository.getSafeItem(expectedItem.id)
        assertNotNull(deletedItem.deletedAt)
        restoreItemUseCase(expectedItem)
        val actualItem = safeItemRepository.getSafeItem(expectedItem.id)
        assertNull(actualItem.deletedAt)
        assertEquals(expectedItem, actualItem)

        val actualChildItem = safeItemRepository.getChildren(expectedItem.id, ItemOrder.Position, firstSafeId).firstOrNull()
        assertNull(actualChildItem)
    }

    @Test
    fun restoreItemUseCase_with_children_test(): TestResult = runTest {
        val expectedItem = createItemUseCase.test()
        val expectedChildItem = createItemUseCase.test(
            parentId = expectedItem.id,
        )
        assertNull(expectedItem.deletedAt)
        moveToBinItemUseCase(expectedItem)
        val deletedItem = safeItemRepository.getSafeItem(expectedItem.id)
        assertNotNull(deletedItem.deletedAt)
        restoreItemUseCase(expectedItem)
        val actualItem = safeItemRepository.getSafeItem(expectedItem.id)
        val actualChildItem = safeItemRepository.getChildren(expectedItem.id, ItemOrder.Position, firstSafeId).firstOrNull()
        assertNull(actualItem.deletedAt)
        assertEquals(expectedItem, actualItem)
        assertEquals(expectedChildItem, actualChildItem)
    }

    @Test
    fun restoreItemUseCase_to_ancestor_removed_parent_test(): TestResult = runTest {
        // Setup
        val itemA = createItemUseCase.test()
        val itemB = createItemUseCase.test(
            parentId = itemA.id,
        )
        val itemC = createItemUseCase.test(
            parentId = itemB.id,
        )

        moveToBinItemUseCase(itemC)
        moveToBinItemUseCase(itemB)
        removeDeletedItemUseCase(itemB)

        // Test
        restoreItemUseCase(safeItemRepository.getSafeItem(itemC.id))

        val expectedC = itemC.copy(
            parentId = itemA.id,
            deletedParentId = null,
            deletedAt = null,
        )
        val actualC = safeItemRepository.getSafeItem(itemC.id)

        assertEquals(expectedC, actualC)
    }

    @Test
    fun restoreItemUseCase_to_ancestor_deleted_parent_test(): TestResult = runTest {
        // Setup
        val itemA = createItemUseCase.test()
        val itemB = createItemUseCase.test(
            parentId = itemA.id,
        )
        val itemC = createItemUseCase.test(
            parentId = itemB.id,
        )

        moveToBinItemUseCase(itemB)

        // Test
        restoreItemUseCase(safeItemRepository.getSafeItem(itemC.id))

        val expectedC = itemC.copy(
            parentId = itemA.id,
            deletedParentId = null,
            deletedAt = null,
        )
        val actualC = safeItemRepository.getSafeItem(itemC.id)

        assertEquals(expectedC, actualC)
    }

    @Test
    fun restoreItemUseCase_all(): TestResult = runTest {
        // Setup
        val parent = createItemUseCase.test()

        // Child
        val itemA = createItemUseCase.test(
            parentId = parent.id,
        )
        // Grand child
        val itemB = createItemUseCase.test(
            parentId = itemA.id,
        )
        // No parent
        val itemC = createItemUseCase.test()

        moveToBinItemUseCase(itemA)
        moveToBinItemUseCase(itemC)

        // Test
        restoreItemUseCase(null)

        val expectedA = itemA.copy(
            parentId = parent.id,
            deletedParentId = null,
            deletedAt = null,
        )
        val expectedB = itemB.copy(
            parentId = itemA.id,
            deletedParentId = null,
            deletedAt = null,
        )
        val expectedC = itemC.copy(
            parentId = null,
            deletedParentId = null,
            deletedAt = null,
        )

        assertEquals(expectedA, safeItemRepository.getSafeItem(expectedA.id))
        assertEquals(expectedB, safeItemRepository.getSafeItem(expectedB.id))
        assertEquals(expectedC, safeItemRepository.getSafeItem(expectedC.id))
    }
}
