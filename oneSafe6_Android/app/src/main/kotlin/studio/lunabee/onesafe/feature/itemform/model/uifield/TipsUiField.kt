package studio.lunabee.onesafe.feature.itemform.model.uifield

import studio.lunabee.onesafe.tooltip.OSTooltipContent

data class TipsUiField(
    val tooltipContent: OSTooltipContent,
    val onDismiss: () -> Unit,
)
