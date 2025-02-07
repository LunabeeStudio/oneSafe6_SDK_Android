package studio.lunabee.onesafe.feature.itemform.model.uifield

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.tooltip.OSRichTooltip
import studio.lunabee.onesafe.tooltip.OSTooltipAccessibility
import studio.lunabee.onesafe.tooltip.OSTooltipBox
import studio.lunabee.onesafe.tooltip.OSTooltipDefaults.accessibilityTooltip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormUiFieldTooltip(
    tipsUiField: TipsUiField,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val state = rememberTooltipState(isPersistent = true)
    LaunchedEffect(key1 = tipsUiField) {
        delay(timeMillis = 200L)
        state.show()
    }
    OSTooltipBox(
        modifier = modifier,
        state = state,
        tooltip = {
            val dismissRequest: () -> Unit = {
                state.dismiss()
            }
            OSRichTooltip(
                title = tipsUiField.tooltipContent.title,
                text = tipsUiField.tooltipContent.description,
                actions = tipsUiField.tooltipContent.actions,
                dismissRequest = dismissRequest,
                modifier = Modifier
                    .accessibilityTooltip(
                        tooltipAccessibility = OSTooltipAccessibility(
                            actionText = LbcTextSpec.StringResource(OSString.common_accessibility_popup_dismiss),
                            action = dismissRequest,
                        ),
                    ),
            )
            DisposableEffect(tipsUiField) {
                onDispose {
                    tipsUiField.onDismiss()
                }
            }
        },
        content = { content() },
    )
}
