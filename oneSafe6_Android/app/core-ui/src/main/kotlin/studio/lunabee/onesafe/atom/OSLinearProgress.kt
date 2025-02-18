package studio.lunabee.onesafe.atom

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.ui.UiConstants

/**
 * @param progress if null, [LinearProgressIndicator] will be infinite.
 */
@Composable
fun OSLinearProgress(
    progress: Float?,
    modifier: Modifier = Modifier,
    progressDescription: LbcTextSpec? = null,
    shape: CornerBasedShape = MaterialTheme.shapes.large,
    progressColor: Color = ProgressIndicatorDefaults.linearColor,
    progressTrackColor: Color = ProgressIndicatorDefaults.linearTrackColor,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
) {
    Column(
        modifier = modifier.testTag(tag = UiConstants.TestTag.Item.LinearProgressItem),
        horizontalAlignment = horizontalAlignment,
    ) {
        val linearProgressModifier = Modifier
            .clip(shape)
            .fillMaxWidth()

        progress?.let {
            LinearProgressIndicator(
                progress = { progress },
                modifier = linearProgressModifier,
                color = progressColor,
                trackColor = progressTrackColor,
            )
        } ?: LinearProgressIndicator(
            modifier = linearProgressModifier,
            color = progressColor,
            trackColor = progressTrackColor,
        )

        progressDescription?.let { progressDescription ->
            OSSmallSpacer()
            OSText(
                text = progressDescription,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
}
