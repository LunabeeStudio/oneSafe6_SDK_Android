package studio.lunabee.onesafe.feature.itemform.composable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import studio.lunabee.onesafe.domain.Constant
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens

@Composable
fun ItemActionLoading(
    loadingProgress: Float?,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .padding(all = OSDimens.External.DefaultCircularStrokeWidth),
        content = { content() },
    )

    loadingProgress?.let {
        val modifier = Modifier
            .size(size = OSDimens.SystemRoundContainerDimension.XLarge.dp + OSDimens.ActionButton.AdditionalPaddingWithCircularProgress)
            .testTag(tag = UiConstants.TestTag.Item.UrlMetadataCircularProgressIndicator)
        when (loadingProgress) {
            Constant.IndeterminateProgress -> CircularProgressIndicator(
                modifier = modifier,
            )
            else -> CircularProgressIndicator(
                progress = { loadingProgress },
                modifier = modifier,
            )
        }
    }
}
