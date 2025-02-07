package studio.lunabee.onesafe.feature.fileviewer.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.atom.OSImage
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.OSRegularSpacer
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.atom.button.OSTextButton
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.action.topAppBarOptionNavBack
import studio.lunabee.onesafe.extension.loremIpsum
import studio.lunabee.onesafe.feature.fileviewer.model.fileViewerTopBarAction
import studio.lunabee.onesafe.feature.itemdetails.model.FileFieldAction
import studio.lunabee.onesafe.molecule.ElevatedTopAppBar
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun UnknownViewerScreen(
    onBackClick: () -> Unit,
    name: String,
    actions: List<FileFieldAction>,
    image: OSImageSpec = OSImageSpec.Drawable(OSDrawable.ic_file),
    text: LbcTextSpec = LbcTextSpec.StringResource(OSString.safeItemDetail_fields_file_noViewer),
) {
    OSScreen(testTag = UiConstants.TestTag.Screen.FileViewerScreen) {
        ElevatedTopAppBar(
            title = LbcTextSpec.Raw(name),
            options = listOfNotNull(
                topAppBarOptionNavBack(onBackClick),
                actions.takeIf { it.isNotEmpty() }?.let { fileViewerTopBarAction(it) },
            ),
        )
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            OSImage(
                image = image,
                modifier = Modifier.size(AppConstants.Ui.FileViewer.UnknownFileSize),
            )
            OSText(
                text = text,
                modifier = Modifier.padding(OSDimens.SystemSpacing.Regular),
                textAlign = TextAlign.Center,
            )
            OSRegularSpacer()
            actions.forEach { action ->
                OSTextButton(
                    text = action.text,
                    onClick = action.onClick,
                    leadingIcon = { OSImage(image = OSImageSpec.Drawable(action.icon)) },
                )
            }
        }
    }
}

@OsDefaultPreview
@Composable
private fun UnknownViewerScreenPreview() {
    OSTheme {
        UnknownViewerScreen(
            onBackClick = {},
            name = loremIpsum(2),
            actions = listOf(
                FileFieldAction.Share { },
                FileFieldAction.Download { },
            ),
        )
    }
}
