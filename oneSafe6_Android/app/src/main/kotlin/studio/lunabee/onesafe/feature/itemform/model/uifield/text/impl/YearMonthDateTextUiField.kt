package studio.lunabee.onesafe.feature.itemform.model.uifield.text.impl

import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.text.TextStyle
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.feature.itemform.model.option.UiFieldOption
import studio.lunabee.onesafe.feature.itemform.model.uifield.cleaner.NumberValueCleaner
import studio.lunabee.onesafe.feature.itemform.model.uifield.cleaner.ValueCleaner
import studio.lunabee.onesafe.feature.itemform.model.uifield.text.TextUiField
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.UUID

class YearMonthDateTextUiField(
    override val id: UUID,
    override val safeItemFieldKind: SafeItemFieldKind,
    override var fieldDescription: MutableState<LbcTextSpec>,
    override var placeholder: LbcTextSpec,
    val cleaner: ValueCleaner = NumberValueCleaner,
) : TextUiField() {
    override val isSecured: Boolean = false
    override val errorFieldLabel: LbcTextSpec = LbcTextSpec.StringResource(OSString.safeItemDetail_yearMonth_errorLabel)
    private val parsePatternString = "MMyy"
    private val parsePattern = DateTimeFormatter.ofPattern(parsePatternString)
    override fun getDisplayedValue(): String = rawValue

    override fun isInError(): Boolean = rawValue.isNotBlank() && getSavedValue().isBlank()

    override fun getSavedValue(): String = try {
        YearMonth.parse(rawValue, parsePattern).atEndOfMonth().atStartOfDay().toString()
    } catch (e: Exception) {
        ""
    }

    override fun initValues(isIdentifier: Boolean, initialValue: String?) {
        this.isIdentifier = isIdentifier
        this.rawValue = initialValue?.let {
            parsePattern.format(LocalDateTime.parse(initialValue))
        }.orEmpty()
    }

    override fun onValueChanged(value: String) {
        rawValue = cleaner(value)
        isErrorDisplayed = false
    }

    @Composable
    override fun getTextStyle(): TextStyle = LocalTextStyle.current

    override val options: List<UiFieldOption> = listOf()
}
