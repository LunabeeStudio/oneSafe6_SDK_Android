package studio.lunabee.onesafe.tooltip

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TooltipScope
import androidx.compose.material3.TooltipState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.PopupPositionProvider
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.button.OSIconButton
import studio.lunabee.onesafe.coreui.R
import studio.lunabee.onesafe.extension.loremIpsumSpec
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

/**
 * Wrap [TooltipBox]. By default, the [TooltipBox] is not persistent (i.e auto-dismiss). Use a custom [TooltipState] to achieve that.
 * @param tooltip represents the tooltip displayed above the anchor representing by [content]. You can use [OSRichTooltip] for example.
 * @param content represents the content displayed on screen without tooltip. This will be used as anchor.
 * @see <a href="https://m3.material.io/components/tooltips/overview">Tooltips M3 specifications</a>
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OSTooltipBox(
    modifier: Modifier = Modifier,
    state: TooltipState = rememberTooltipState(),
    spacingBetweenTooltipAndAnchor: Dp = UiConstants.GoogleInternalApi.SpacingBetweenTooltipAndAnchor,
    positionProvider: PopupPositionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(spacingBetweenTooltipAndAnchor),
    focusable: Boolean = true,
    enableUserInput: Boolean = true,
    tooltip: @Composable TooltipScope.() -> Unit,
    content: @Composable () -> Unit,
) {
    TooltipBox(
        positionProvider = positionProvider,
        state = state,
        modifier = modifier,
        focusable = focusable,
        enableUserInput = enableUserInput,
        tooltip = tooltip,
        content = content,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@OsDefaultPreview
@Composable
private fun OSRichTooltipPreview() {
    OSTheme {
        val tooltipState: TooltipState = rememberTooltipState(isPersistent = true, initialIsVisible = true)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(all = OSDimens.SystemSpacing.Large),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            OSTooltipBox(
                state = tooltipState,
                spacingBetweenTooltipAndAnchor = OSDimens.SystemSpacing.Large,
                tooltip = {
                    OSRichTooltip(
                        title = loremIpsumSpec(1),
                        text = loremIpsumSpec(10),
                        actions = listOf(
                            OSTooltipAction(text = loremIpsumSpec(1), onClick = { }),
                        ),
                        dismissRequest = { },
                    )
                },
            ) {
                OSIconButton(image = OSImageSpec.Drawable(R.drawable.os_ic_sample), onClick = { }, contentDescription = null)
            }
        }
    }
}
