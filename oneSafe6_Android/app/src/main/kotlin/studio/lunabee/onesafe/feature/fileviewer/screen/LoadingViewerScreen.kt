package studio.lunabee.onesafe.feature.fileviewer.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.action.topAppBarOptionNavBack
import studio.lunabee.onesafe.molecule.ElevatedTopAppBar
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens

@Composable
fun LoadingViewerScreen(
    onBackClick: () -> Unit,
    name: String?,
) {
    OSScreen(testTag = UiConstants.TestTag.Screen.FileViewerScreen) {
        ElevatedTopAppBar(
            title = name?.let(LbcTextSpec::Raw) ?: LbcTextSpec.StringResource(OSString.common_loading),
            options = listOf(topAppBarOptionNavBack(onBackClick)),
            elevation = OSDimens.Elevation.TopAppBarElevation,
        )
        Box(
            Modifier.fillMaxSize(),
        ) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}
