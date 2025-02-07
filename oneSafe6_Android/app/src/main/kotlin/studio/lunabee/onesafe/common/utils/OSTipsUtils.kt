package studio.lunabee.onesafe.common.utils

import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.tooltip.OSTooltipAction

object OSTipsUtils {
    fun getGotItAction(onClick: () -> Unit = { }): OSTooltipAction = OSTooltipAction(
        text = LbcTextSpec.StringResource(OSString.common_tips_gotIt),
        onClick = onClick,
        contentDescription = LbcTextSpec.StringResource(OSString.common_accessibility_popup_dismiss),
    )

    val CommonTipsTitle: LbcTextSpec = LbcTextSpec.StringResource(OSString.common_tips)
}
