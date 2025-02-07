package studio.lunabee.onesafe.common.utils

import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.commonui.extension.formatNumber
import studio.lunabee.onesafe.commonui.extension.replaceSpaceWithAsciiChar
import studio.lunabee.onesafe.domain.model.safeitem.FieldMask
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.domain.usecase.item.ItemDecryptUseCase
import java.time.LocalDateTime
import java.util.UUID

object FormattingHelper {

    /**
     * Function that "build" a string ready to be displayed from an encrypted value.
     * If the [mask] is not null , it will be applied to the returning string.
     */
    suspend fun getVisibleValue(
        mask: ByteArray?,
        encValue: ByteArray?,
        itemId: UUID,
        safeItemFieldKind: SafeItemFieldKind?,
        decryptUseCase: ItemDecryptUseCase,
    ): String? = encValue?.let { decryptUseCase(it, itemId, String::class).data }?.let { _rawValue ->
        val decFormattingMask: String? = mask?.let { decryptUseCase(it, itemId, String::class).data }
        val formattedValue = when (safeItemFieldKind) {
            is SafeItemFieldKind.Date -> AppConstants.Ui.TimeRelatedFieldFormatter.DateFormatter.format(
                LocalDateTime.parse(_rawValue, AppConstants.Ui.TimeRelatedFieldFormatter.UnzonedLocalDateTimeParser),
            )
            is SafeItemFieldKind.DateAndHour -> AppConstants.Ui.TimeRelatedFieldFormatter.DateAndTimeFormatter.format(
                LocalDateTime.parse(_rawValue, AppConstants.Ui.TimeRelatedFieldFormatter.UnzonedLocalDateTimeParser),
            )
            is SafeItemFieldKind.Hour -> AppConstants.Ui.TimeRelatedFieldFormatter.TimeFormatter.format(
                LocalDateTime.parse(_rawValue, AppConstants.Ui.TimeRelatedFieldFormatter.UnzonedLocalDateTimeParser),
            )
            is SafeItemFieldKind.YearMonth -> AppConstants.Ui.TimeRelatedFieldFormatter.YearMonthDateFormatter.format(
                LocalDateTime.parse(_rawValue, AppConstants.Ui.TimeRelatedFieldFormatter.UnzonedLocalDateTimeParser),
            )
            is SafeItemFieldKind.Number -> {
                _rawValue.formatNumber()
            }
            is SafeItemFieldKind.Password -> _rawValue.replaceSpaceWithAsciiChar()
            else -> _rawValue
        }
        return decFormattingMask?.let {
            FieldMask.getApplyMaskOnString(formattedValue.toCharArray(), it)
        } ?: formattedValue
    }
}
