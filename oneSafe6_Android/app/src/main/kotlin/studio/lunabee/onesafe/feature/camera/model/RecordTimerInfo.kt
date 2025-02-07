package studio.lunabee.onesafe.feature.camera.model

import kotlin.time.Duration

/**
 * Data model for record timer
 * @property fileSizeInfo is a pair containing the actual value of the size in the string, and @StringRes used to display it in the int
 */
data class RecordTimerInfo(
    val timer: Duration,
    val fileSizeInfo: Pair<String, Int>,
)
