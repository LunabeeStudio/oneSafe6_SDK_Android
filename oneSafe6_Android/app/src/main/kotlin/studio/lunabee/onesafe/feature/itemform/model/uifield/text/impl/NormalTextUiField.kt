package studio.lunabee.onesafe.feature.itemform.model.uifield.text.impl

import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.text.TextStyle
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.feature.itemform.model.uifield.cleaner.ValueCleaner
import studio.lunabee.onesafe.feature.itemform.model.uifield.text.TextUiField
import java.util.UUID

class NormalTextUiField(
    override val id: UUID,
    override val safeItemFieldKind: SafeItemFieldKind,
    override var fieldDescription: MutableState<LbcTextSpec>,
    override var placeholder: LbcTextSpec,
    valueCleaner: ValueCleaner? = null,
) : TextUiField(valueCleaner) {

    override val isSecured: Boolean = false

    override fun getDisplayedValue(): String = rawValue

    @Composable
    override fun getTextStyle(): TextStyle = LocalTextStyle.current
}
