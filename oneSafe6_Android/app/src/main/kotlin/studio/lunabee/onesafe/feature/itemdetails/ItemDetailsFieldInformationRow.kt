package studio.lunabee.onesafe.feature.itemdetails

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import com.valentinilk.shimmer.shimmer
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.atom.OSClickableRow
import studio.lunabee.onesafe.atom.OSIconDecorationButton
import studio.lunabee.onesafe.atom.OSImage
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.button.defaults.OSTextButtonDefaults
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.extension.loremIpsumSpec
import studio.lunabee.onesafe.feature.itemdetails.model.FileFieldAction
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.model.combinedClickableWithHaptic
import studio.lunabee.onesafe.molecule.OSRow
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewOnSurfaceTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun ItemDetailsFieldInformationRow(
    thumbnail: OSImageSpec?,
    name: LbcTextSpec,
    isLoading: Boolean,
    share: () -> Unit,
    download: () -> Unit,
    visualize: () -> Unit,
) {
    var isMenuVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current
    OSRow(
        text = name,
        textMaxLines = 1,
        modifier = Modifier
            .combinedClickableWithHaptic(
                onClick = visualize,
                onLongClick = { isMenuVisible = true },
                onClickLabel = LbcTextSpec.StringResource(OSString.accessibility_field_openPreview).string,
                onLongClickLabel = LbcTextSpec.StringResource(OSString.accessibility_field_displayActions).string,
            )
            .clearAndSetSemantics { this.contentDescription = name.string(context) }
            .padding(
                horizontal = OSDimens.SystemSpacing.Regular,
                vertical = OSDimens.SystemSpacing.Small,
            ),
        startContent = {
            val shimmerColor = MaterialTheme.colorScheme.primary
            if (thumbnail == null) {
                Box(
                    modifier = Modifier
                        .size(AppConstants.Ui.Item.ThumbnailFileDpSize)
                        .clip(RoundedCornerShape(AppConstants.Ui.Item.ThumbnailFileCornerRadius))
                        .shimmer()
                        .drawBehind { drawRect(shimmerColor) },
                )
            } else {
                OSImage(
                    image = thumbnail,
                    modifier = Modifier
                        .size(AppConstants.Ui.Item.ThumbnailFileDpSize)
                        .clip(RoundedCornerShape(AppConstants.Ui.Item.ThumbnailFileCornerRadius)),
                    contentScale = ContentScale.Crop,
                )
            }
        },
        endContent = {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(AppConstants.Ui.Item.ProgressIndicatorSize))
            }
        },
    )
    FileFieldActionMenu(
        isMenuExpended = isMenuVisible,
        onDismiss = { isMenuVisible = false },
        actions = listOf(
            FileFieldAction.Share(share),
            FileFieldAction.Download(download),
        ),
    )
}

@Composable
fun FileFieldActionMenu(
    isMenuExpended: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    actions: List<FileFieldAction>,
) {
    DropdownMenu(
        expanded = isMenuExpended,
        onDismissRequest = onDismiss,
        modifier = modifier,
    ) {
        actions.forEach { action ->
            OSClickableRow(
                onClick = {
                    action.onClick()
                    onDismiss()
                },
                text = action.text,
                leadingIcon = {
                    OSIconDecorationButton(image = OSImageSpec.Drawable(action.icon))
                },
                buttonColors = OSTextButtonDefaults.secondaryTextButtonColors(OSActionState.Enabled),
            )
        }
    }
}

@OsDefaultPreview
@Composable
fun ItemDetailsFieldInformationRowPreview() {
    OSPreviewOnSurfaceTheme {
        ItemDetailsFieldInformationRow(
            thumbnail = OSImageSpec.Drawable(OSDrawable.ic_crown),
            name = loremIpsumSpec(3),
            isLoading = true,
            share = {},
            visualize = {},
            download = {},
        )
    }
}
