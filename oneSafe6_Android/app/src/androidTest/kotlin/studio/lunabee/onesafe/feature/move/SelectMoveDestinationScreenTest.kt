package studio.lunabee.onesafe.feature.move

import androidx.activity.ComponentActivity
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToKey
import androidx.compose.ui.test.runAndroidComposeUiTest
import androidx.paging.PagingData
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Test
import studio.lunabee.onesafe.AppAndroidTestUtils
import studio.lunabee.onesafe.animation.rememberOSTopBarVisibilityNestedScrollConnection
import studio.lunabee.onesafe.commonui.DefaultNameProvider
import studio.lunabee.onesafe.feature.move.selectdestination.SelectMoveDestinationRoute
import studio.lunabee.onesafe.feature.move.selectdestination.SelectMoveDestinationViewModel
import studio.lunabee.onesafe.test.OSTest
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.theme.OSTheme
import java.util.UUID

@OptIn(ExperimentalTestApi::class)
class SelectMoveDestinationScreenTest : OSTest() {
    private val navigateToDestination: (UUID) -> Unit = spyk({ })

    @Test
    fun cannot_navigate_to_self_test() {
        val itemSize = 100

        val safeItemPaginationList = AppAndroidTestUtils.createPlainItemData(
            size = itemSize,
            itemNameProvider = { DefaultNameProvider("item - $it") },
        )

        val firstItemId = safeItemPaginationList.first().id
        val lastItemId = safeItemPaginationList.last().id
        val firstItemIndex = "0"
        val firstItemName = "item - $firstItemIndex"
        val lastItemIndex = "${safeItemPaginationList.lastIndex}"
        val lastItemName = "item - $lastItemIndex"
        val vm = mockk<SelectMoveDestinationViewModel> {
            every { currentItem } returns MutableStateFlow(null)
            every { items } returns flowOf(
                PagingData.from(safeItemPaginationList, sourceLoadStates = AppAndroidTestUtils.loadedPagingStates()),
            )
            every { initialItemsCount } returns 100
        }
        setSelectDestinationScreen(vm, firstItemId, "0") {
            onNodeWithText(firstItemName).performClick()
            verify(exactly = 0) { navigateToDestination.invoke(firstItemId) }
            onNodeWithTag(UiConstants.TestTag.Item.SelectMoveDestinationItemGrid)
                .performScrollToKey(safeItemPaginationList.last()::class.simpleName + lastItemId)
            onNodeWithText(lastItemName).performClick()
        }
        verify(exactly = 1) { navigateToDestination.invoke(lastItemId) }
    }

    private fun setSelectDestinationScreen(
        viewModel: SelectMoveDestinationViewModel,
        itemToMove: UUID,
        itemToMoveName: String,
        block: ComposeUiTest.() -> Unit,
    ) {
        runAndroidComposeUiTest<ComponentActivity> {
            setContent {
                val lazyGridState = rememberLazyGridState()
                OSTheme {
                    SelectMoveDestinationRoute(
                        itemToMoveId = itemToMove,
                        itemToMoveName = itemToMoveName,
                        showSnackBar = {},
                        navigateToDestination = navigateToDestination,
                        lazyGridState = lazyGridState,
                        nestedScrollConnection = rememberOSTopBarVisibilityNestedScrollConnection(
                            lazyGridState,
                        ),
                        onMove = {},
                        viewModel = viewModel,
                    )
                }
            }
            block()
        }
    }
}
