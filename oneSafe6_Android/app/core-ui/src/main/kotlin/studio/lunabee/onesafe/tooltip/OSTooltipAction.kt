package studio.lunabee.onesafe.tooltip

import studio.lunabee.compose.core.LbcTextSpec

data class OSTooltipAction(
    val text: LbcTextSpec,
    val onClick: () -> Unit,
    val contentDescription: LbcTextSpec? = null,
)
