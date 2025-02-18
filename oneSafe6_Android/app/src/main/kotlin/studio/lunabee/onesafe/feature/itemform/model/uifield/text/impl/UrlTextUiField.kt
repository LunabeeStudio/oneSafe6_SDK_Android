package studio.lunabee.onesafe.feature.itemform.model.uifield.text.impl

import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.feature.itemform.model.uifield.DefaultObservableField
import studio.lunabee.onesafe.feature.itemform.model.uifield.ObservableField
import studio.lunabee.onesafe.feature.itemform.model.uifield.text.TextUiField
import java.util.UUID

class UrlTextUiField(
    override val id: UUID,
    override val safeItemFieldKind: SafeItemFieldKind,
    override var fieldDescription: MutableState<LbcTextSpec>,
    override var placeholder: LbcTextSpec,
    private val observableField: DefaultObservableField = DefaultObservableField(),
) : TextUiField(), ObservableField by observableField {
    override fun getVisualTransformation(): VisualTransformation = VisualTransformation.None

    override val isSecured: Boolean = false
    override fun getDisplayedValue(): String = rawValue

    override fun onValueChanged(value: String) {
        rawValue = value
        observableField.textObserver?.onValueChanged(value)
    }

    @Composable
    override fun getTextStyle(): TextStyle = LocalTextStyle.current
}
