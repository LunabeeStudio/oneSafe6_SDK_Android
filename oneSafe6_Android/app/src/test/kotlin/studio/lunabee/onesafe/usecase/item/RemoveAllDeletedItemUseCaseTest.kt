package studio.lunabee.onesafe.usecase.item

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.onesafe.test.OSHiltUnitTest
import studio.lunabee.onesafe.domain.model.safeitem.ItemOrder
import studio.lunabee.onesafe.domain.repository.SafeItemDeletedRepository
import studio.lunabee.onesafe.domain.usecase.item.CreateItemUseCase
import studio.lunabee.onesafe.domain.usecase.item.MoveToBinItemUseCase
import studio.lunabee.onesafe.domain.usecase.item.RemoveAllDeletedItemUseCase
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.firstSafeId
import studio.lunabee.onesafe.test.test
import javax.inject.Inject
import kotlin.test.assertEquals

@HiltAndroidTest
class RemoveAllDeletedItemUseCaseTest : OSHiltUnitTest() {
    @get:Rule override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.Home()

    @Inject lateinit var createItemUseCase: CreateItemUseCase

    @Inject lateinit var moveToBinItemUseCase: MoveToBinItemUseCase

    @Inject lateinit var safeItemDeletedRepository: SafeItemDeletedRepository

    @Inject lateinit var removeAllDeletedItemUseCase: RemoveAllDeletedItemUseCase

    @Test
    fun remove_all_root_items_test(): TestResult = runTest {
        val itemCount = 10
        repeat(itemCount) {
            val item = createItemUseCase.test()
            moveToBinItemUseCase(item)
        }

        assertEquals(itemCount, safeItemDeletedRepository.getDeletedItemsByDeletedParent(null, ItemOrder.Position, firstSafeId).size)
        removeAllDeletedItemUseCase()
        assertEquals(0, safeItemDeletedRepository.getDeletedItemsByDeletedParent(null, ItemOrder.Position, firstSafeId).size)
    }

    @Test
    fun remove_all_items_test(): TestResult = runTest {
        val itemA = createItemUseCase.test()
        val itemB = createItemUseCase.test(parentId = itemA.id)
        createItemUseCase.test(parentId = itemB.id)

        moveToBinItemUseCase(itemA)

        assertEquals(3, safeItemDeletedRepository.findDeletedByIdWithDeletedDescendants(itemA.id).size)
        removeAllDeletedItemUseCase()
        assertEquals(0, safeItemDeletedRepository.getDeletedItemsByDeletedParent(null, ItemOrder.Position, firstSafeId).size)
        assertEquals(0, safeItemDeletedRepository.findDeletedByIdWithDeletedDescendants(itemA.id).size)
    }
}
