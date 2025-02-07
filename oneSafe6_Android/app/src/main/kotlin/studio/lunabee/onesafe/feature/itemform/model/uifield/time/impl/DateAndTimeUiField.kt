package studio.lunabee.onesafe.feature.itemform.model.uifield.time.impl

import androidx.compose.runtime.MutableState
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.feature.itemform.model.option.UiFieldOption
import studio.lunabee.onesafe.feature.itemform.model.option.time.DatePickerOption
import studio.lunabee.onesafe.feature.itemform.model.option.time.TimePickerOption
import studio.lunabee.onesafe.feature.itemform.model.uifield.time.DatePickerHolder
import studio.lunabee.onesafe.feature.itemform.model.uifield.time.TimePickerHolder
import studio.lunabee.onesafe.feature.itemform.model.uifield.time.TimeRelatedUiField
import java.util.UUID

class DateAndTimeUiField(
    override val id: UUID,
    override val safeItemFieldKind: SafeItemFieldKind,
    override var fieldDescription: MutableState<LbcTextSpec>,
    override var placeholder: LbcTextSpec,
) : TimeRelatedUiField(), DatePickerHolder, TimePickerHolder {
    override val formatter: DateTimeFormatter = AppConstants.Ui.TimeRelatedFieldFormatter.DateAndTimeFormatter

    override fun onValueDateChanged(dateTime: LocalDateTime) {
        this.dateTime = dateTime
            .withHour(this.dateTime?.hour ?: 0)
            .withMinute(this.dateTime?.minute ?: 0)
    }

    override val options: List<UiFieldOption> = listOf(
        DatePickerOption(this),
        TimePickerOption(this),
    )

    override fun onValueTimeChanged(hours: Int, minutes: Int) {
        val dateTime = dateTime ?: LocalDateTime.now()
        this.dateTime = dateTime
            .withHour(hours)
            .withMinute(minutes)
    }
}
