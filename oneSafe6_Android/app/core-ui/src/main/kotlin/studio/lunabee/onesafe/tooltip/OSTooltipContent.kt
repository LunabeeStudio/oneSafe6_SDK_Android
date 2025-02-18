package studio.lunabee.onesafe.tooltip

import studio.lunabee.compose.core.LbcTextSpec

data class OSTooltipContent(
    val title: LbcTextSpec,
    val description: LbcTextSpec,
    val actions: List<OSTooltipAction>,
)
