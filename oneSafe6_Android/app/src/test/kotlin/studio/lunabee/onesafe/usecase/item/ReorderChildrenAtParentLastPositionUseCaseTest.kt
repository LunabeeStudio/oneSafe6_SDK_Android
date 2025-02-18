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
import studio.lunabee.onesafe.domain.usecase.item.ReorderChildrenAtParentLastPositionUseCase
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.firstSafeId
import javax.inject.Inject
import kotlin.test.assertContentEquals

@HiltAndroidTest
class ReorderChildrenAtParentLastPositionUseCaseTest : OSHiltUnitTest() {
    @get:Rule override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.Home()

    @Inject lateinit var safeItemRepository: SafeItemRepository

    @Inject lateinit var createItemUseCase: CreateItemUseCase

    @Inject lateinit var reorderChildrenAtParentLastPositionUseCase: ReorderChildrenAtParentLastPositionUseCase

    @Test
    fun reorderChildAtParentLastPosition(): TestResult = runTest {
        val item = createItemUseCase(
            name = null,
            parentId = null,
            isFavorite = false,
            icon = null,
            color = null,
            position = 10.0,
        ).data!!

        val childItem = createItemUseCase(
            name = null,
            parentId = item.id,
            isFavorite = false,
            icon = null,
            color = null,
            position = 5.0,
        ).data!!

        val childItem2 = createItemUseCase(
            name = null,
            parentId = item.id,
            isFavorite = false,
            icon = null,
            color = null,
            position = 13.0,
        ).data!!

        reorderChildrenAtParentLastPositionUseCase(item)

        val actualItems = safeItemRepository.getChildren(item.id, ItemOrder.Position, firstSafeId)
        val expectedItems = listOf(childItem.copy(position = 11.0), childItem2.copy(position = 12.0))

        assertContentEquals(actualItems, expectedItems)
    }

    @Test
    fun reorderChildAtParentLastPosition_optim_no_change(): TestResult = runTest {
        val item = createItemUseCase(
            name = null,
            parentId = null,
            isFavorite = false,
            icon = null,
            color = null,
            position = 10.0,
        ).data!!

        val childItem = createItemUseCase(
            name = null,
            parentId = item.id,
            isFavorite = false,
            icon = null,
            color = null,
            position = 15.0,
        ).data!!

        val childItem2 = createItemUseCase(
            name = null,
            parentId = item.id,
            isFavorite = false,
            icon = null,
            color = null,
            position = 17.0,
        ).data!!

        reorderChildrenAtParentLastPositionUseCase(item)

        val actualItems = safeItemRepository.getChildren(item.id, ItemOrder.Position, firstSafeId)
        val expectedItems = listOf(childItem, childItem2)

        assertContentEquals(actualItems, expectedItems)
    }
}
