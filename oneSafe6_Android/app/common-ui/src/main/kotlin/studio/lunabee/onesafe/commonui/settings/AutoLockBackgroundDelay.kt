package studio.lunabee.onesafe.commonui.settings

import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

enum class AutoLockBackgroundDelay(val value: Duration, val text: LbcTextSpec) {
    IMMEDIATELY(Duration.ZERO, LbcTextSpec.StringResource(OSString.common_delay_immediately)),
    FIVE_SECONDS(5.seconds, LbcTextSpec.StringResource(OSString.common_delay_seconds5)),
    TEN_SECONDS(10.seconds, LbcTextSpec.StringResource(OSString.common_delay_seconds10)),
    ONE_MINUTE(1.minutes, LbcTextSpec.StringResource(OSString.common_delay_minute1)),
    FIVE_MINUTES(5.minutes, LbcTextSpec.StringResource(OSString.common_delay_minutes5)),
    NEVER(Duration.INFINITE, LbcTextSpec.StringResource(OSString.common_delay_never)),
    ;

    companion object {
        fun valueForDuration(delay: Duration): AutoLockBackgroundDelay {
            return AutoLockBackgroundDelay.entries.reversed().firstOrNull { delay >= it.value } ?: IMMEDIATELY
        }
    }
}
