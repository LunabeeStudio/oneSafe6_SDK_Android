package studio.lunabee.onesafe.feature.itemform.bottomsheet.newfile

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSClickableRow
import studio.lunabee.onesafe.atom.OSClickableRowText
import studio.lunabee.onesafe.atom.OSIconDecorationButton
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.bottomsheet.BottomSheetHolderColumnContent
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem

@Composable
fun AddNewFileBottomSheetContent(
    paddingValues: PaddingValues,
    onFileFromGalleryRequested: () -> Unit,
    onFileFromCameraRequested: () -> Unit,
    onFileFromExplorerRequested: () -> Unit,
) {
    val actions = listOf(
        FileCreationAction.FromGallery(onFileFromGalleryRequested),
        FileCreationAction.FromCamera(onFileFromCameraRequested),
        FileCreationAction.FromFileExplorer(onFileFromExplorerRequested),
    )
    val context = LocalContext.current
    BottomSheetHolderColumnContent(
        paddingValues = paddingValues,
    ) {
        OSText(
            text = LbcTextSpec.StringResource(OSString.safeItemDetail_addMedia_bottomsheet_menu_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth().padding(OSDimens.SystemSpacing.Regular),
        )
        actions.forEachIndexed { index, action ->
            OSClickableRow(
                modifier = Modifier
                    .clearAndSetSemantics {
                        contentDescription = action.title.string(context)
                    },
                onClick = action.onClick,
                label = { modifier ->
                    OSClickableRowText(text = action.title, modifier = modifier)
                },
                contentPadding = LocalDesignSystem.current.getRowClickablePaddingValuesDependingOnIndex(
                    index = index,
                    elementsCount = actions.size,
                ),
                leadingIcon = {
                    OSIconDecorationButton(image = OSImageSpec.Drawable(drawable = action.iconRes))
                },
            )
        }
    }
}
