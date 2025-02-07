package studio.lunabee.onesafe.commonui.settings

import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

enum class AutoLockInactivityDelay(val value: Duration, val text: LbcTextSpec) {
    THIRTY_SECONDS(30.seconds, LbcTextSpec.StringResource(OSString.common_delay_seconds30)),
    ONE_MINUTE(1.minutes, LbcTextSpec.StringResource(OSString.common_delay_minute1)),
    TWO_MINUTES(2.minutes, LbcTextSpec.StringResource(OSString.common_delay_minutes2)),
    FIVE_MINUTES(5.minutes, LbcTextSpec.StringResource(OSString.common_delay_minutes5)),
    TEN_MINUTES(10.minutes, LbcTextSpec.StringResource(OSString.common_delay_minutes10)),
    NEVER(Duration.INFINITE, LbcTextSpec.StringResource(OSString.common_delay_never)),
    ;

    companion object {
        fun valueForDuration(delay: Duration): AutoLockInactivityDelay {
            return AutoLockInactivityDelay.entries.reversed().firstOrNull { delay >= it.value } ?: THIRTY_SECONDS
        }
    }
}
