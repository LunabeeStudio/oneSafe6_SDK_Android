package studio.lunabee.onesafe.tooltip

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.RichTooltipColors
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TooltipScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.button.OSTextButton
import studio.lunabee.onesafe.atom.button.defaults.OSTextButtonDefaults
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.ui.UiConstants

/**
 * Wrap [RichTooltip]. Use it to display a [OSTooltipBox] with an optional title, a description and actions.
 * If you don't have to use an action, you might use a [androidx.compose.material3.PlainTooltip].
 * Preview available in [OSTooltipBox].
 * @param displayCaret will display a little triangle.
 * @see <a href="https://m3.material.io/components/tooltips/overview">Tooltips M3 specifications</a>
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TooltipScope.OSRichTooltip(
    title: LbcTextSpec,
    text: LbcTextSpec,
    dismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    actions: List<OSTooltipAction> = emptyList(),
    displayCaret: Boolean = true,
    shape: Shape = TooltipDefaults.richTooltipContainerShape,
    colors: RichTooltipColors = TooltipDefaults.richTooltipColors(
        containerColor = MaterialTheme.colorScheme.inverseSurface,
        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
        titleContentColor = MaterialTheme.colorScheme.inverseOnSurface,
        actionContentColor = MaterialTheme.colorScheme.inversePrimary,
    ),
    tonalElevation: Dp = UiConstants.GoogleInternalApi.TonalElevation,
    shadowElevation: Dp = UiConstants.GoogleInternalApi.ShadowElevation,
) {
    RichTooltip(
        modifier = modifier,
        title = { OSText(text = title) },
        action = if (actions.isEmpty()) {
            null
        } else {
            {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth(),
                ) {
                    actions.forEach { action ->
                        OSTextButton(
                            text = action.text,
                            onClick = {
                                action.onClick()
                                dismissRequest()
                            },
                            buttonColors = OSTextButtonDefaults.textButtonColors(color = colors.actionContentColor),
                        )
                    }
                }
            }
        },
        shape = shape,
        colors = colors,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
        caretSize = if (displayCaret) {
            TooltipDefaults.caretSize
        } else {
            DpSize.Zero
        },
        text = { OSText(text = text) },
    )
}
