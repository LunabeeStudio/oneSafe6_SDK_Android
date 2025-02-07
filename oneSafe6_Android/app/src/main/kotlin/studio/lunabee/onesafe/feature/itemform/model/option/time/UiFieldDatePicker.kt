package studio.lunabee.onesafe.feature.itemform.model.option.time

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.atom.button.OSTextButton
import java.time.LocalDateTime
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UiFieldDatePicker(
    dateTime: LocalDateTime,
    onDismiss: () -> Unit,
    onValueChanged: (LocalDateTime) -> Unit,
) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = dateTime.toInstant(ZoneOffset.UTC).toEpochMilli(),
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        content = {
            DatePicker(state = state)
        },
        confirmButton = {
            OSTextButton(
                text = LbcTextSpec.StringResource(OSString.common_confirm),
                onClick = {
                    state.selectedDateMillis?.let {
                        onDismiss()
                        onValueChanged(LocalDateTime.ofEpochSecond(it / 1000, 0, ZoneOffset.UTC))
                    }
                },
            )
        },
        dismissButton = {
            OSTextButton(
                text = LbcTextSpec.StringResource(OSString.common_cancel),
                onClick = onDismiss,
            )
        },
    )
}
