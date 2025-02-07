package studio.lunabee.onesafe.feature.itemform.model.option.time

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.atom.button.OSTextButton
import studio.lunabee.onesafe.dialog.OSDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerUiField(
    hour: Int,
    minutes: Int,
    onDismiss: () -> Unit,
    onValueChanged: (Int, Int) -> Unit,
) {
    val state = rememberTimePickerState(
        initialHour = hour,
        initialMinute = minutes,
    )
    TimePickerDialog(
        onDismiss = onDismiss,
        content = {
            TimePicker(state = state)
        },
        confirmButton = {
            OSTextButton(
                text = LbcTextSpec.StringResource(OSString.common_confirm),
                onClick = {
                    onValueChanged(state.hour, state.minute)
                    onDismiss()
                },
            )
        },
        cancelButton = {
            OSTextButton(
                text = LbcTextSpec.StringResource(OSString.common_cancel),
                onClick = onDismiss,
            )
        },
    )
}

/**
 * TODO Replace later when its added to lib
 * Taken from sample
 * https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/material3/material3/samples/src/main/java/androidx/compose/material3/samples/TimePickerSamples.kt
 */
@Composable
fun TimePickerDialog(
    confirmButton: @Composable () -> Unit,
    cancelButton: @Composable () -> Unit,
    onDismiss: () -> Unit,
    toggle: @Composable RowScope.() -> Unit = {},
    content: @Composable () -> Unit,
) {
    OSDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .background(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surface,
                ),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.padding(20.dp))
                content()
                Row(
                    modifier = Modifier
                        .height(40.dp)
                        .fillMaxWidth(),
                ) {
                    toggle()
                    Spacer(modifier = Modifier.weight(1f))
                    cancelButton()
                    confirmButton()
                }
            }
        }
    }
}
