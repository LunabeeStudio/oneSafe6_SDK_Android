package studio.lunabee.onesafe.common.extensions

import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.domain.model.verifypassword.VerifyPasswordInterval

fun VerifyPasswordInterval.getLabel(): LbcTextSpec = when (this) {
    VerifyPasswordInterval.NEVER -> LbcTextSpec.StringResource(OSString.verifyPassword_interval_never)
    VerifyPasswordInterval.EVERY_WEEK -> LbcTextSpec.StringResource(OSString.verifyPassword_interval_weekly)
    VerifyPasswordInterval.EVERY_TWO_WEEKS -> LbcTextSpec.StringResource(OSString.verifyPassword_interval_everyTwoWeeks)
    VerifyPasswordInterval.EVERY_MONTH -> LbcTextSpec.StringResource(OSString.verifyPassword_interval_everyMonth)
    VerifyPasswordInterval.EVERY_TWO_MONTHS -> LbcTextSpec.StringResource(OSString.verifyPassword_interval_everyTwoMonths)
    VerifyPasswordInterval.EVERY_SIX_MONTHS -> LbcTextSpec.StringResource(OSString.verifyPassword_interval_everySixMonths)
}
