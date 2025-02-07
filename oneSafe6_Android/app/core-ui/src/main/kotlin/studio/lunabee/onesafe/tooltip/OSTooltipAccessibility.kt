package studio.lunabee.onesafe.tooltip

import studio.lunabee.compose.core.LbcTextSpec

data class OSTooltipAccessibility(
    val actionText: LbcTextSpec,
    val action: () -> Unit,
    val mergeDescendants: Boolean = true,
)
