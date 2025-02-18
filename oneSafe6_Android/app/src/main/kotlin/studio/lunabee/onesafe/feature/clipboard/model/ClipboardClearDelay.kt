package studio.lunabee.onesafe.feature.clipboard.model

import android.os.Build
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Clipboard background clear delay available depending on the API
 *
 * @param value the delay value mapped to [Duration]
 * @param apiMax the Android API max (inclusive) supported
 */
enum class ClipboardClearDelay(val value: Duration, val text: LbcTextSpec, private val apiMax: Int = Int.MAX_VALUE) {
    TEN_SECONDS(10.seconds, LbcTextSpec.StringResource(OSString.common_delay_seconds10)),
    THIRTY_SECONDS(30.seconds, LbcTextSpec.StringResource(OSString.common_delay_seconds30)),
    ONE_MINUTE(1.minutes, LbcTextSpec.StringResource(OSString.common_delay_minute1)),
    TWO_MINUTES(2.minutes, LbcTextSpec.StringResource(OSString.common_delay_minutes2), Build.VERSION_CODES.R),
    TEN_MINUTES(10.minutes, LbcTextSpec.StringResource(OSString.common_delay_minutes10), Build.VERSION_CODES.R),
    NEVER(Duration.INFINITE, LbcTextSpec.StringResource(OSString.common_delay_never)),
    ;

    companion object {
        /**
         * @return The ordered list (delay ascending) of [ClipboardClearDelay] filtered by the Android API
         */
        fun filteredValues(): List<ClipboardClearDelay> {
            return ClipboardClearDelay.entries.filter {
                Build.VERSION.SDK_INT <= it.apiMax
            }
        }

        /**
         * @return The [ClipboardClearDelay] closest to the [delay] param
         */
        fun valueForDuration(delay: Duration): ClipboardClearDelay {
            return filteredValues().reversed().firstOrNull { delay >= it.value } ?: TEN_SECONDS
        }
    }
}
