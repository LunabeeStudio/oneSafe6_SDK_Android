package studio.lunabee.onesafe.feature.itemform.model.option.time

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.textfield.OSTrailingAction
import studio.lunabee.onesafe.feature.itemform.model.option.UiFieldOption
import studio.lunabee.onesafe.feature.itemform.model.uifield.time.TimePickerHolder
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.compose.core.LbcTextSpec

class TimePickerOption(
    private val field: TimePickerHolder,
    override val clickLabel: LbcTextSpec? = LbcTextSpec.StringResource(OSString.safeItemDetail_accessibility_showTimePicker),
) : UiFieldOption {
    private var isPickerVisible: Boolean by mutableStateOf(false)

    override fun onClick() {
        isPickerVisible = true
    }

    @Composable
    override fun ComposableLayout(modifier: Modifier) {
        val focusManager = LocalFocusManager.current
        OSTrailingAction(
            image = OSImageSpec.Drawable(OSDrawable.ic_hour, tintColor = MaterialTheme.colorScheme.primary),
            onClick = { isPickerVisible = true },
            contentDescription = null,
            testTag = UiConstants.TestTag.Item.TimePickerAction,
            modifier = modifier,
        )
        if (isPickerVisible) {
            focusManager.clearFocus(true)
            TimePickerUiField(
                onDismiss = { isPickerVisible = false },
                hour = field.dateTime?.hour ?: 0,
                minutes = field.dateTime?.minute ?: 0,
                onValueChanged = field::onValueTimeChanged,
            )
        }
    }
}
