package studio.lunabee.onesafe.tooltip

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.PopupPositionProvider
import studio.lunabee.onesafe.accessibility.accessibilityClick
import studio.lunabee.onesafe.ui.UiConstants

object OSTooltipDefaults {

    @Composable
    fun rememberAnchorCenteredProvider(
        spacingBetweenTooltipAndAnchor: Dp = UiConstants.GoogleInternalApi.SpacingBetweenTooltipAndAnchor,
    ): PopupPositionProvider {
        val tooltipAnchorSpacing = with(LocalDensity.current) { spacingBetweenTooltipAndAnchor.roundToPx() }
        return remember(tooltipAnchorSpacing) {
            object : PopupPositionProvider {
                override fun calculatePosition(
                    anchorBounds: IntRect,
                    windowSize: IntSize,
                    layoutDirection: LayoutDirection,
                    popupContentSize: IntSize,
                ): IntOffset {
                    val x = anchorBounds.left - popupContentSize.width + anchorBounds.width

                    // Tooltip prefers to be above the anchor,
                    // but if this causes the tooltip to overlap with the anchor
                    // then we place it below the anchor
                    var y = anchorBounds.top - popupContentSize.height - tooltipAnchorSpacing
                    if (y < 0) y = anchorBounds.bottom + tooltipAnchorSpacing
                    return IntOffset(x, y)
                }
            }
        }
    }

    fun Modifier.accessibilityTooltip(
        tooltipAccessibility: OSTooltipAccessibility,
    ): Modifier {
        return composed {
            val label = tooltipAccessibility.actionText.string
            semantics(mergeDescendants = tooltipAccessibility.mergeDescendants) {
                accessibilityClick(label = label, action = tooltipAccessibility.action)
            }
        }
    }
}
