package studio.lunabee.onesafe.feature.fileviewer.screen

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImage
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.atom.rememberOSImageState
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.action.topAppBarOptionNavBack
import studio.lunabee.onesafe.feature.fileviewer.model.fileViewerTopBarAction
import studio.lunabee.onesafe.feature.itemdetails.model.FileFieldAction
import studio.lunabee.onesafe.molecule.ElevatedTopAppBar
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens

@Composable
fun ImageViewerScreen(
    onBackClick: () -> Unit,
    uri: Uri,
    title: String,
    actions: List<FileFieldAction>,
) {
    val imageState = rememberOSImageState()
    val error = imageState.error
    if (error != null) {
        UnknownViewerScreen(
            onBackClick = onBackClick,
            name = title,
            actions = actions,
            image = OSImageSpec.Drawable(OSDrawable.ic_image),
            text = LbcTextSpec.StringResource(OSString.safeItemDetail_fields_image_viewerError),
        )
    } else {
        OSScreen(testTag = UiConstants.TestTag.Screen.FileViewerScreen) {
            Column {
                ElevatedTopAppBar(
                    title = LbcTextSpec.Raw(title),
                    options = listOf(
                        topAppBarOptionNavBack(onBackClick),
                        fileViewerTopBarAction(actions),
                    ),
                    elevation = OSDimens.Elevation.TopAppBarElevation,
                )
                OSImage(
                    image = OSImageSpec.Uri(uri = uri),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .zoomable(rememberZoomState()),
                    imageState = imageState,
                )
            }
        }
    }
}
