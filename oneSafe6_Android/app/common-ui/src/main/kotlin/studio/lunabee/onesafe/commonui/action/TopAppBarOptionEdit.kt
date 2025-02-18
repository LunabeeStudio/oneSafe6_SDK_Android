package studio.lunabee.onesafe.commonui.action

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TooltipState
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.model.TopAppBarOptionTrailing
import studio.lunabee.onesafe.tooltip.OSTooltipAccessibility
import studio.lunabee.onesafe.tooltip.OSTooltipContent

fun topAppBarOptionEdit(
    description: LbcTextSpec,
    onEditItemClick: () -> Unit,
): TopAppBarOptionTrailing = TopAppBarOptionTrailing.primaryIconAction(
    image = OSImageSpec.Drawable(OSDrawable.ic_edit),
    contentDescription = description,
    onClick = onEditItemClick,
)

@OptIn(ExperimentalMaterial3Api::class)
fun topAppBarTooltipOptionEdit(
    description: LbcTextSpec,
    onEditItemClick: () -> Unit,
    tooltipState: TooltipState,
    tooltipContent: OSTooltipContent,
    tooltipAccessibility: OSTooltipAccessibility?,
): TopAppBarOptionTrailing = TopAppBarOptionTrailing.primaryTooltipIconAction(
    image = OSImageSpec.Drawable(OSDrawable.ic_edit),
    contentDescription = description,
    onClick = onEditItemClick,
    tooltipState = tooltipState,
    tooltipContent = tooltipContent,
    tooltipAccessibility = tooltipAccessibility,
)
