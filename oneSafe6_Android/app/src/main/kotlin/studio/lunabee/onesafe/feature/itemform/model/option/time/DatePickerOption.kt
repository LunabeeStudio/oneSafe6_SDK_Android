package studio.lunabee.onesafe.feature.itemform.model.option.time

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import java.time.LocalDateTime
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.textfield.OSTrailingAction
import studio.lunabee.onesafe.feature.itemform.model.option.UiFieldOption
import studio.lunabee.onesafe.feature.itemform.model.uifield.time.DatePickerHolder
import studio.lunabee.onesafe.ui.UiConstants

class DatePickerOption(
    val field: DatePickerHolder,
    override val clickLabel: LbcTextSpec = LbcTextSpec.StringResource(OSString.safeItemDetail_accessibility_showDatePicker),
) : UiFieldOption {
    private var isPickerVisible: Boolean by mutableStateOf(false)

    override fun onClick() {
        isPickerVisible = true
    }

    @Composable
    override fun ComposableLayout(modifier: Modifier) {
        val focusManager = LocalFocusManager.current
        OSTrailingAction(
            image = OSImageSpec.Drawable(OSDrawable.ic_today, tintColor = MaterialTheme.colorScheme.primary),
            onClick = { isPickerVisible = true },
            contentDescription = null,
            testTag = UiConstants.TestTag.Item.DatePickerAction,
            modifier = modifier,
        )
        if (isPickerVisible) {
            focusManager.clearFocus(true)
            UiFieldDatePicker(
                dateTime = field.dateTime ?: LocalDateTime.now(),
                onDismiss = { isPickerVisible = false },
                onValueChanged = field::onValueDateChanged,
            )
        }
    }
}
