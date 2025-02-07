package studio.lunabee.onesafe.feature.itemform.model.uifield.time

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.VisualTransformation
import studio.lunabee.compose.accessibility.state.AccessibilityState
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.accessibility.accessibilityInvisibleToUser
import studio.lunabee.onesafe.accessibility.rememberOSAccessibilityState
import studio.lunabee.onesafe.atom.textfield.OSTextField
import studio.lunabee.onesafe.feature.itemform.model.uifield.UiField
import studio.lunabee.onesafe.ui.UiConstants
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

abstract class TimeRelatedUiField : UiField() {

    var dateTime: LocalDateTime? by mutableStateOf(null)
    abstract val formatter: DateTimeFormatter

    override val isSecured: Boolean = false
    private fun getFormattedValueForDisplay(): String = dateTime?.let { formatter.format(it) }.orEmpty()

    override fun getDisplayedValue(): String = dateTime?.toString().orEmpty()

    override fun initValues(isIdentifier: Boolean, initialValue: String?) {
        this.isIdentifier = isIdentifier
        this.dateTime = initialValue?.let {
            LocalDateTime.parse(
                initialValue,
                AppConstants.Ui.TimeRelatedFieldFormatter.UnzonedLocalDateTimeParser,
            )
        }
    }

    @Composable
    override fun InnerComposable(modifier: Modifier, hasNext: Boolean) {
        val accessibilityState: AccessibilityState = rememberOSAccessibilityState()
        OSTextField(
            value = getFormattedValueForDisplay(),
            placeholder = if (accessibilityState.isTouchExplorationEnabled) {
                null
            } else {
                placeholder
            },
            label = fieldDescription.value,
            onValueChange = {},
            modifier = modifier
                .fillMaxWidth()
                .onFocusEvent {
                    if (it.hasFocus) {
                        options
                            .first()
                            .onClick()
                    }
                }
                .testTag(UiConstants.TestTag.Item.ItemFormField),
            inputTextStyle = LocalTextStyle.current,
            visualTransformation = VisualTransformation.None,
            maxLines = 1,
            readOnly = true,
            errorLabel = errorFieldLabel,
            isError = isErrorDisplayed,
            trailingAction = {
                Row {
                    options.forEach {
                        it.ComposableLayout(
                            modifier = Modifier
                                .accessibilityInvisibleToUser(), // handle by parent
                        )
                    }
                }
            },
        )
    }
}
