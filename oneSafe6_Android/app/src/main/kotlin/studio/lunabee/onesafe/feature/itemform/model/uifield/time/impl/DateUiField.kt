package studio.lunabee.onesafe.feature.itemform.model.uifield.time.impl

import androidx.compose.runtime.MutableState
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.feature.itemform.model.option.UiFieldOption
import studio.lunabee.onesafe.feature.itemform.model.option.time.DatePickerOption
import studio.lunabee.onesafe.feature.itemform.model.uifield.time.DatePickerHolder
import studio.lunabee.onesafe.feature.itemform.model.uifield.time.TimeRelatedUiField
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class DateUiField(
    override val id: UUID,
    override val safeItemFieldKind: SafeItemFieldKind,
    override var fieldDescription: MutableState<LbcTextSpec>,
    override var placeholder: LbcTextSpec,
) : TimeRelatedUiField(), DatePickerHolder {
    override val isSecured: Boolean = false
    override val formatter: DateTimeFormatter = AppConstants.Ui.TimeRelatedFieldFormatter.DateFormatter

    override fun onValueDateChanged(dateTime: LocalDateTime) {
        this.dateTime = dateTime
    }

    override val options: List<UiFieldOption> = listOf(DatePickerOption(this))
}
