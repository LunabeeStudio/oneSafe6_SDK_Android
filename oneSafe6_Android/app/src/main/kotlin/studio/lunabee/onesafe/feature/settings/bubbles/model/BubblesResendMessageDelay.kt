package studio.lunabee.onesafe.feature.settings.bubbles.model

import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

enum class BubblesResendMessageDelay(val value: Duration, val text: LbcTextSpec) {
    NEVER(Duration.ZERO, LbcTextSpec.StringResource(OSString.common_delay_never)),
    ONE_DAY(1.days, LbcTextSpec.StringResource(OSString.bubbles_resendDelay_24Hours)),
    TWO_DAY(2.days, LbcTextSpec.StringResource(OSString.bubbles_resendDelay_48Hours)),
    FIVE_DAYS(5.days, LbcTextSpec.StringResource(OSString.bubbles_resendDelay_fiveDays)),
    ALWAYS(Duration.INFINITE, LbcTextSpec.StringResource(OSString.bubbles_resendDelay_always)),
    ;

    companion object {
        fun valueForDuration(delay: Duration): BubblesResendMessageDelay {
            return entries.reversed().firstOrNull { delay >= it.value } ?: ALWAYS
        }
    }
}
