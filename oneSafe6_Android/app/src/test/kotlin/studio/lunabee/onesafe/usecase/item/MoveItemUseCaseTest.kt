package studio.lunabee.onesafe.usecase.item

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.onesafe.domain.model.safeitem.SafeItem
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.domain.usecase.item.CreateItemUseCase
import studio.lunabee.onesafe.domain.usecase.move.MoveItemUseCase
import studio.lunabee.onesafe.test.test
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltUnitTest
import javax.inject.Inject
import kotlin.test.assertEquals

@HiltAndroidTest
class MoveItemUseCaseTest : OSHiltUnitTest() {

    override val initialTestState: InitialTestState = InitialTestState.Home()

    @get:Rule
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @Inject
    lateinit var moveItemUseCase: MoveItemUseCase

    @Inject
    lateinit var createItemUseCase: CreateItemUseCase

    @Inject
    lateinit var safeItemRepository: SafeItemRepository

    @Test
    fun move_item_without_child_test(): TestResult = runTest {
        val itemToMove = createItemUseCase.test(name = "")
        val itemDestination = createItemUseCase.test(name = "")
        moveItemUseCase(itemToMove.id, itemDestination.id)

        // We need to "re-fetch" the item as the parent should have change
        assertEquals(itemDestination.id, safeItemRepository.getSafeItem(itemToMove.id).parentId)
    }

    @Test
    fun move_item_with_children_test(): TestResult = runTest {
        val itemToMove = createItemUseCase.test(name = "")
        val childrenList = mutableListOf<SafeItem>()
        repeat(10) {
            val child = createItemUseCase.test(name = "$it", parentId = itemToMove.id)
            childrenList.add(child)
        }
        val itemDestination = createItemUseCase.test(name = "")
        moveItemUseCase(itemToMove.id, itemDestination.id)
        assertEquals(itemDestination.id, safeItemRepository.getSafeItem(itemToMove.id).parentId)
        childrenList.forEach {
            assertEquals(
                itemToMove.id,
                safeItemRepository.getSafeItem(it.id).parentId,
            ) // Test that the children still have the same parentId
        }
    }
}
