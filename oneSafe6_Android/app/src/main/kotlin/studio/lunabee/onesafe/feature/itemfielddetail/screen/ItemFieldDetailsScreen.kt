package studio.lunabee.onesafe.feature.itemfielddetail.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.feature.itemfielddetail.model.ItemFieldDetailsState
import studio.lunabee.onesafe.feature.itemfielddetail.viewmodel.ItemFieldDetailsViewModel
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.theme.OSUserTheme

@Composable
fun ItemFieldDetailsRoute(
    navigateBack: () -> Unit,
    viewModel: ItemFieldDetailsViewModel = hiltViewModel(),
) {
    val itemDetailsFieldFullscreenState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = itemDetailsFieldFullscreenState) {
        is ItemFieldDetailsState.Data -> {
            OSUserTheme(customPrimaryColor = state.color) {
                when (state.screenType) {
                    ItemFieldDetailsState.Data.ScreenType.TEXT -> {
                        ItemFieldDetailsTextScreen(
                            fieldName = state.fieldName,
                            fieldValue = state.fieldValue,
                            navigateBack = navigateBack,
                        )
                    }
                    ItemFieldDetailsState.Data.ScreenType.PAGER -> {
                        ItemFieldDetailsPagerScreen(
                            fieldValue = state.fieldValue,
                            navigateBack = navigateBack,
                        )
                    }
                }
            }
        }
        is ItemFieldDetailsState.Error -> {
            // TODO implement corrupt item screen (should be the only reason of failure here, wait for @tkubasik PR)
        }
        ItemFieldDetailsState.Initializing -> {
            OSUserTheme(customPrimaryColor = null) {
                OSScreen(testTag = UiConstants.TestTag.Screen.ItemDetailsFieldFullScreen) { }
            }
        }
    }
}
