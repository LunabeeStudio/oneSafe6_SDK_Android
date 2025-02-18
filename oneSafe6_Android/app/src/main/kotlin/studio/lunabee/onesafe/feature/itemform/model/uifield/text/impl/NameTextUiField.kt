package studio.lunabee.onesafe.feature.itemform.model.uifield.text.impl

import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.feature.itemform.model.uifield.text.TextUiField
import java.util.UUID

class NameTextUiField(
    override val id: UUID,
    override val safeItemFieldKind: SafeItemFieldKind,
    override var fieldDescription: MutableState<LbcTextSpec>,
    override var placeholder: LbcTextSpec,
    val onValueChange: (String) -> Unit,
) : TextUiField() {
    override val isSecured: Boolean = true
    override val errorFieldLabel: LbcTextSpec = LbcTextSpec.StringResource(OSString.safeItemDetail_title_empty_errorLabel)
    override fun isInError(): Boolean = rawValue.isBlank()
    override fun getDisplayedValue(): String = rawValue
    private var isValueFromUser: Boolean = false
    fun isValueOverridable(): Boolean = !isValueFromUser || getDisplayedValue().isEmpty()

    fun onValueChangedFromUrlMetadata(value: String) {
        isErrorDisplayed = false
        rawValue = value
        if (isErrorDisplayed) {
            isErrorDisplayed = value.isEmpty()
        }
    }

    override fun onValueChanged(value: String) {
        isValueFromUser = value.isNotEmpty()
        rawValue = value
        isErrorDisplayed = false
        onValueChange(value)
    }

    @Composable
    override fun getTextStyle(): TextStyle = LocalTextStyle.current

    override fun getVisualTransformation(): VisualTransformation = VisualTransformation.None
}
