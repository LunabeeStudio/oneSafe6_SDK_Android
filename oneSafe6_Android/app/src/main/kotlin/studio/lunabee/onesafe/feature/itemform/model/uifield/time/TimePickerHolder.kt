package studio.lunabee.onesafe.feature.itemform.model.uifield.time

import java.time.LocalDateTime

interface TimePickerHolder {
    val dateTime: LocalDateTime?

    fun onValueTimeChanged(hours: Int, minutes: Int)
}
