package studio.lunabee.onesafe.feature.itemform.model.uifield.text.impl

import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.feature.itemform.model.option.UiFieldOption
import studio.lunabee.onesafe.feature.itemform.model.option.text.GeneratePasswordOption
import studio.lunabee.onesafe.feature.itemform.model.option.text.VisibilityFieldOption
import studio.lunabee.onesafe.feature.itemform.model.uifield.cleaner.NumberValueCleaner
import studio.lunabee.onesafe.feature.itemform.model.uifield.cleaner.ValueCleaner
import studio.lunabee.onesafe.feature.itemform.model.uifield.text.TextUiField
import studio.lunabee.onesafe.ui.theme.OSTypography
import java.util.UUID

class PasswordTextUiField(
    override val id: UUID,
    override val safeItemFieldKind: SafeItemFieldKind,
    override var fieldDescription: MutableState<LbcTextSpec>,
    override var placeholder: LbcTextSpec,
    valueCleaner: ValueCleaner? = null,
) : TextUiField(valueCleaner) {
    var isValueHidden: Boolean by mutableStateOf(true)

    override val isSecured: Boolean = true

    @Composable
    override fun getTextStyle(): TextStyle = LocalTextStyle.current.copy(fontFamily = OSTypography.Legibility)
    override fun getDisplayedValue(): String = rawValue

    override fun onFocusEvent(focusState: FocusState) {
        isValueHidden = !focusState.hasFocus
    }

    override fun getVisualTransformation(): VisualTransformation = if (isValueHidden) {
        PasswordVisualTransformation()
    } else {
        super.getVisualTransformation()
    }

    override val options: List<UiFieldOption> = listOfNotNull(
        VisibilityFieldOption(this),
        GeneratePasswordOption(this).takeIf { safeItemFieldKind == SafeItemFieldKind.Password },
    )

    companion object {
        fun pin(id: UUID, fieldDescription: LbcTextSpec, placeholder: LbcTextSpec): PasswordTextUiField {
            return PasswordTextUiField(
                id = id,
                safeItemFieldKind = SafeItemFieldKind.Number,
                fieldDescription = mutableStateOf(fieldDescription),
                placeholder = placeholder,
                valueCleaner = NumberValueCleaner,
            )
        }
    }
}
