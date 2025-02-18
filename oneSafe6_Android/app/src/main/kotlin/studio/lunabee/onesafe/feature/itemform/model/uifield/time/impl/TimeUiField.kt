package studio.lunabee.onesafe.feature.itemform.model.uifield.time.impl

import androidx.compose.runtime.MutableState
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.feature.itemform.model.option.UiFieldOption
import studio.lunabee.onesafe.feature.itemform.model.option.time.TimePickerOption
import studio.lunabee.onesafe.feature.itemform.model.uifield.time.TimePickerHolder
import studio.lunabee.onesafe.feature.itemform.model.uifield.time.TimeRelatedUiField
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class TimeUiField(
    override val id: UUID,
    override val safeItemFieldKind: SafeItemFieldKind,
    override var fieldDescription: MutableState<LbcTextSpec>,
    override var placeholder: LbcTextSpec,
) : TimeRelatedUiField(), TimePickerHolder {
    override val formatter: DateTimeFormatter = AppConstants.Ui.TimeRelatedFieldFormatter.TimeFormatter

    override fun onValueTimeChanged(hours: Int, minutes: Int) {
        this.dateTime = LocalDateTime.now()
            .withHour(hours)
            .withMinute(minutes)
    }

    override val options: List<UiFieldOption> = listOf(TimePickerOption(this))
}
