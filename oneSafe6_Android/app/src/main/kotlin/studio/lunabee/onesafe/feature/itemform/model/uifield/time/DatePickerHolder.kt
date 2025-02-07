package studio.lunabee.onesafe.feature.itemform.model.uifield.time

import java.time.LocalDateTime

interface DatePickerHolder {
    var dateTime: LocalDateTime?
    fun onValueDateChanged(dateTime: LocalDateTime)
}
