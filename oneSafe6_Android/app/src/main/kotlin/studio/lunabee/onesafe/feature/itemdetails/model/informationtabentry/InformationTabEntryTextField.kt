package studio.lunabee.onesafe.feature.itemdetails.model.informationtabentry

import androidx.compose.runtime.Stable
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.common.utils.FormattingHelper
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.domain.model.safeitem.FieldMask
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemField
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.domain.usecase.item.ItemDecryptUseCase
import studio.lunabee.onesafe.jvm.use
import java.util.UUID

@Stable
data class InformationTabEntryTextField(
    override val id: UUID,
    val value: LbcTextSpec,
    val label: LbcTextSpec,
    override val kind: SafeItemFieldKind,
    val isSecured: Boolean,
    val getDecryptedDisplayValue: suspend () -> String?, // Get the value as it will be displayed i.e. when the mask is applied
    val getDecryptedRawValue: suspend () -> String?, // Get the value as it is store in the database.
) : InformationTabEntry {
    companion object {
        private suspend fun getHiddenValue(
            encSecureDisplayMask: ByteArray?,
            encValue: ByteArray?,
            itemId: UUID,
            decryptUseCase: ItemDecryptUseCase,
        ): LbcTextSpec? {
            val securedDisplayMask: String? = encSecureDisplayMask?.let { decryptUseCase(it, itemId, String::class).data }
            return when {
                encValue == null -> null
                securedDisplayMask == null -> LbcTextSpec.StringResource(OSString.safeItemDetail_contentCard_informations_securedValue)
                else -> decryptUseCase(encValue, itemId, String::class).data?.toCharArray()?.use { _decValue ->
                    FieldMask.getApplyMaskOnString(value = _decValue, securedDisplayMask).let(LbcTextSpec::Raw)
                }
            }
        }

        suspend fun fromTextSafeItemField(
            field: SafeItemField,
            decryptUseCase: ItemDecryptUseCase,
            kind: SafeItemFieldKind,
        ): InformationTabEntry? {
            val name = field.encName?.let { decryptUseCase(it, field.itemId, String::class).data }
            val entryValue = if (field.isSecured) {
                getHiddenValue(
                    encSecureDisplayMask = field.encSecureDisplayMask,
                    encValue = field.encValue,
                    itemId = field.itemId,
                    decryptUseCase = decryptUseCase,
                )
            } else {
                FormattingHelper.getVisibleValue(
                    mask = field.encFormattingMask,
                    encValue = field.encValue,
                    itemId = field.itemId,
                    decryptUseCase = decryptUseCase,
                    safeItemFieldKind = kind,
                )?.let(LbcTextSpec::Raw)
            }
            return entryValue?.let { textSpec ->
                InformationTabEntryTextField(
                    id = field.id,
                    value = textSpec,
                    label = LbcTextSpec.Raw(name.orEmpty()),
                    kind = kind,
                    isSecured = field.isSecured,
                    getDecryptedDisplayValue = {
                        FormattingHelper.getVisibleValue(field.encFormattingMask, field.encValue, field.itemId, kind, decryptUseCase)
                    },
                    getDecryptedRawValue = {
                        decryptUseCase(field.encValue!!, field.itemId, String::class).data
                    },
                )
            }
        }
    }
}
