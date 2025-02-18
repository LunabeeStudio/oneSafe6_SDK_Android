package studio.lunabee.onesafe.feature.itemdetails

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import studio.lunabee.compose.androidtest.LbcComposeTest
import studio.lunabee.onesafe.AppAndroidTestUtils
import studio.lunabee.onesafe.test.testUUIDs
import studio.lunabee.onesafe.ui.theme.OSTheme

@OptIn(ExperimentalTestApi::class)
abstract class ItemDetailsTest : LbcComposeTest() {
    protected val mockItemDetailsViewModel: ItemDetailsViewModel
        get() = mockk<ItemDetailsViewModel>().apply {
            every { this@apply.itemId } returns testUUIDs[0]
            every { this@apply.snackbarState } returns MutableStateFlow(null)
            every { this@apply.navigationAction } returns MutableStateFlow(null)
            every { this@apply.itemActionDialogState } returns MutableStateFlow(null)
            every { this@apply.itemActionSnackbarState } returns MutableStateFlow(null)
        }

    fun setItemDetailsScreen(
        viewModel: ItemDetailsViewModel,
        block: AndroidComposeUiTest<ComponentActivity>.(a: ComponentActivity) -> Unit,
    ) {
        invoke(
            clazz = ComponentActivity::class.java,
        ) {
            val snackbarHostState = SnackbarHostState()
            setContent {
                val coroutineScope = rememberCoroutineScope()
                OSTheme {
                    Box {
                        with(AppAndroidTestUtils.composeItemActionNavScopeTest()) {
                            ItemDetailsRoute(
                                viewModel = viewModel,
                                navigateBack = {},
                                navigateToItemDetails = { _, _ -> },
                                navigateToEditItem = { },
                                { _, _ -> },
                                showSnackbar = { coroutineScope.launch { snackbarHostState.showSnackbar(it) } },
                                navigateToFileViewer = {},
                            )
                            SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.Center))
                        }
                    }
                }
            }
            block(activity!!)
        }
    }
}
