package studio.lunabee.onesafe.feature.itemdetails

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.valentinilk.shimmer.shimmer
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.atom.OSImage
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.feature.itemdetails.model.FileFieldAction
import studio.lunabee.onesafe.model.combinedClickableWithHaptic

@Composable
fun ItemDetailsPhotoFieldLayout(
    contentDescription: LbcTextSpec,
    thumbnail: OSImageSpec?,
    share: () -> Unit,
    download: () -> Unit,
    visualize: () -> Unit,
) {
    var isMenuVisible: Boolean by remember { mutableStateOf(false) }
    val context = LocalContext.current
    if (thumbnail == null) {
        val shimmerColor = MaterialTheme.colorScheme.primary
        Box(
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(AppConstants.Ui.Item.ThumbnailFileCornerRadius))
                .shimmer()
                .drawBehind { drawRect(shimmerColor) }
                .semantics(
                    properties = {
                        this.contentDescription = contentDescription.string(context)
                    },
                )
                .combinedClickableWithHaptic(
                    onClick = visualize,
                    onLongClick = { isMenuVisible = true },
                    onClickLabel = LbcTextSpec.StringResource(OSString.accessibility_field_openPreview).string,
                    onLongClickLabel = LbcTextSpec.StringResource(OSString.accessibility_field_displayActions).string,
                ),
        )
    } else {
        OSImage(
            image = thumbnail,
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(AppConstants.Ui.Item.ThumbnailFileCornerRadius))
                .combinedClickableWithHaptic(
                    onClick = visualize,
                    onLongClick = { isMenuVisible = true },
                    onClickLabel = LbcTextSpec.StringResource(OSString.accessibility_field_openPreview).string,
                    onLongClickLabel = LbcTextSpec.StringResource(OSString.accessibility_field_displayActions).string,
                ),
            contentScale = ContentScale.Crop,
            contentDescription = contentDescription,
        )
    }

    FileFieldActionMenu(
        isMenuExpended = isMenuVisible,
        onDismiss = { isMenuVisible = false },
        actions = listOf(
            FileFieldAction.Share(share),
            FileFieldAction.Download(download),
        ),
    )
}
