package studio.lunabee.onesafe.feature.itemform.model

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.domain.common.FieldIdProvider
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.feature.itemform.model.uifield.UiField
import studio.lunabee.onesafe.feature.itemform.model.uifield.cleaner.IbanValueCleaner
import studio.lunabee.onesafe.feature.itemform.model.uifield.cleaner.NumberValueCleaner
import studio.lunabee.onesafe.feature.itemform.model.uifield.text.impl.NormalTextUiField
import studio.lunabee.onesafe.feature.itemform.model.uifield.text.impl.PasswordTextUiField
import studio.lunabee.onesafe.feature.itemform.model.uifield.text.impl.UrlTextUiField
import studio.lunabee.onesafe.feature.itemform.model.uifield.time.impl.DateAndTimeUiField
import studio.lunabee.onesafe.feature.itemform.model.uifield.time.impl.DateUiField
import studio.lunabee.onesafe.feature.itemform.model.uifield.time.impl.TimeUiField

@Stable
sealed interface ItemFieldType {
    val id: String
    val titleText: LbcTextSpec

    @get:DrawableRes val iconRes: Int

    companion object {
        private val DateItemFieldType = StandardItemFieldType(
            id = "date",
            safeItemFieldKind = SafeItemFieldKind.Date,
            titleText = LbcTextSpec.StringResource(id = OSString.fieldName_date),
            iconRes = OSDrawable.ic_calendar_today,
            isSecured = false,
        ) { fieldIdProvider ->
            DateUiField(
                id = fieldIdProvider(),
                safeItemFieldKind = SafeItemFieldKind.Date,
                fieldDescription = mutableStateOf(LbcTextSpec.StringResource(id = OSString.fieldName_date)),
                placeholder = LbcTextSpec.StringResource(id = OSString.fieldName_date),
            )
        }

        private val DateAndTimeItemFieldType = StandardItemFieldType(
            id = "date and time",
            safeItemFieldKind = SafeItemFieldKind.DateAndHour,
            titleText = LbcTextSpec.StringResource(id = OSString.fieldName_dateAndTime),
            iconRes = OSDrawable.ic_today,
            isSecured = false,
        ) { fieldIdProvider ->
            DateAndTimeUiField(
                id = fieldIdProvider(),
                safeItemFieldKind = SafeItemFieldKind.DateAndHour,
                fieldDescription = mutableStateOf(LbcTextSpec.StringResource(id = OSString.fieldName_dateAndTime)),
                placeholder = LbcTextSpec.StringResource(id = OSString.fieldName_dateAndTime),
            )
        }

        private val EmailItemFieldType = StandardItemFieldType(
            id = "email",
            safeItemFieldKind = SafeItemFieldKind.Email,
            titleText = LbcTextSpec.StringResource(id = OSString.fieldName_email),
            iconRes = OSDrawable.ic_email,
            isSecured = false,
        ) { fieldIdProvider ->
            NormalTextUiField(
                id = fieldIdProvider(),
                safeItemFieldKind = SafeItemFieldKind.Email,
                fieldDescription = mutableStateOf(LbcTextSpec.StringResource(id = OSString.fieldName_email)),
                placeholder = LbcTextSpec.StringResource(id = OSString.fieldName_email),
            )
        }
        private val TimeItemFieldType = StandardItemFieldType(
            id = "time",
            safeItemFieldKind = SafeItemFieldKind.Hour,
            titleText = LbcTextSpec.StringResource(id = OSString.fieldName_time),
            iconRes = OSDrawable.ic_hour,
            isSecured = false,
        ) { fieldIdProvider ->
            TimeUiField(
                id = fieldIdProvider(),
                safeItemFieldKind = SafeItemFieldKind.Hour,
                fieldDescription = mutableStateOf(LbcTextSpec.StringResource(id = OSString.fieldName_time)),
                placeholder = LbcTextSpec.StringResource(id = OSString.fieldName_time),
            )
        }

        private val SecuredNoteItemFieldType = StandardItemFieldType(
            id = "secured_note",
            safeItemFieldKind = SafeItemFieldKind.Note,
            titleText = LbcTextSpec.StringResource(id = OSString.fieldName_securedNote),
            iconRes = OSDrawable.ic_security,
            isSecured = true,
        ) { fieldIdProvider ->
            PasswordTextUiField(
                id = fieldIdProvider(),
                safeItemFieldKind = SafeItemFieldKind.Note,
                fieldDescription = mutableStateOf(LbcTextSpec.StringResource(id = OSString.fieldName_securedNote)),
                placeholder = LbcTextSpec.StringResource(id = OSString.fieldName_securedNote),
            )
        }

        private val NoteItemFieldType = StandardItemFieldType(
            id = "note",
            safeItemFieldKind = SafeItemFieldKind.Note,
            titleText = LbcTextSpec.StringResource(id = OSString.fieldName_note),
            iconRes = OSDrawable.ic_note,
            isSecured = false,
        ) { fieldIdProvider ->
            NormalTextUiField(
                id = fieldIdProvider(),
                safeItemFieldKind = SafeItemFieldKind.Note,
                fieldDescription = mutableStateOf(LbcTextSpec.StringResource(id = OSString.fieldName_note)),
                placeholder = LbcTextSpec.StringResource(id = OSString.fieldName_note),
            )
        }

        private val NumberItemFieldType = StandardItemFieldType(
            id = "number",
            safeItemFieldKind = SafeItemFieldKind.Number,
            titleText = LbcTextSpec.StringResource(id = OSString.fieldName_number),
            iconRes = OSDrawable.ic_field_number,
            isSecured = false,
        ) { fieldIdProvider ->
            NormalTextUiField(
                id = fieldIdProvider(),
                safeItemFieldKind = SafeItemFieldKind.Number,
                fieldDescription = mutableStateOf(LbcTextSpec.StringResource(id = OSString.fieldName_number)),
                placeholder = LbcTextSpec.StringResource(id = OSString.fieldName_number),
                valueCleaner = NumberValueCleaner,
            )
        }

        private val PasswordItemFieldType = StandardItemFieldType(
            id = "password",
            safeItemFieldKind = SafeItemFieldKind.Password,
            titleText = LbcTextSpec.StringResource(id = OSString.fieldName_password),
            iconRes = OSDrawable.ic_key,
            isSecured = true,
        ) { fieldIdProvider ->
            PasswordTextUiField(
                id = fieldIdProvider(),
                safeItemFieldKind = SafeItemFieldKind.Password,
                fieldDescription = mutableStateOf(LbcTextSpec.StringResource(id = OSString.fieldName_password)),
                placeholder = LbcTextSpec.StringResource(id = OSString.safeItemDetail_common_password_placeholder),
            )
        }

        private val PhoneItemFieldType = StandardItemFieldType(
            id = "phone",
            safeItemFieldKind = SafeItemFieldKind.Phone,
            titleText = LbcTextSpec.StringResource(id = OSString.fieldName_phone),
            iconRes = OSDrawable.ic_local_phone,
            isSecured = false,
        ) { fieldIdProvider ->
            NormalTextUiField(
                id = fieldIdProvider(),
                safeItemFieldKind = SafeItemFieldKind.Phone,
                fieldDescription = mutableStateOf(LbcTextSpec.StringResource(id = OSString.fieldName_phone)),
                placeholder = LbcTextSpec.StringResource(id = OSString.fieldName_phone),
            )
        }

        private val UrlItemFieldType = StandardItemFieldType(
            id = "url",
            safeItemFieldKind = SafeItemFieldKind.Url,
            titleText = LbcTextSpec.StringResource(id = OSString.fieldName_url),
            iconRes = OSDrawable.ic_web,
            isSecured = false,
        ) { fieldIdProvider ->
            UrlTextUiField(
                id = fieldIdProvider(),
                safeItemFieldKind = SafeItemFieldKind.Url,
                fieldDescription = mutableStateOf(LbcTextSpec.StringResource(id = OSString.fieldName_url)),
                placeholder = LbcTextSpec.StringResource(id = OSString.safeItemDetail_url_link_placeholder),
            )
        }

        private val TextItemFieldType: StandardItemFieldType = StandardItemFieldType(
            id = "text",
            safeItemFieldKind = SafeItemFieldKind.Text,
            titleText = LbcTextSpec.StringResource(id = OSString.fieldName_text),
            iconRes = OSDrawable.ic_text_fields,
            isSecured = false,
        ) { fieldIdProvider ->
            NormalTextUiField(
                id = fieldIdProvider(),
                safeItemFieldKind = SafeItemFieldKind.Text,
                fieldDescription = mutableStateOf(LbcTextSpec.StringResource(id = OSString.fieldName_text)),
                placeholder = LbcTextSpec.StringResource(id = OSString.fieldName_text),
            )
        }

        private val CreditCardNumberItemFieldType: StandardItemFieldType = StandardItemFieldType(
            id = "cardNumber",
            safeItemFieldKind = SafeItemFieldKind.CreditCardNumber,
            titleText = LbcTextSpec.StringResource(id = OSString.fieldName_card_number),
            iconRes = OSDrawable.ic_credit_card,
            isSecured = true,
        ) { fieldIdProvider ->
            PasswordTextUiField(
                id = fieldIdProvider(),
                safeItemFieldKind = SafeItemFieldKind.CreditCardNumber,
                fieldDescription = mutableStateOf(LbcTextSpec.StringResource(id = OSString.fieldName_card_number)),
                placeholder = LbcTextSpec.StringResource(id = OSString.safeItemDetail_card_number_placeholder),
            )
        }

        private val IbanItemFieldType: StandardItemFieldType = StandardItemFieldType(
            id = "iban",
            safeItemFieldKind = SafeItemFieldKind.Iban,
            titleText = LbcTextSpec.StringResource(OSString.fieldName_iban),
            iconRes = OSDrawable.ic_bank,
            isSecured = false,
        ) { fieldIdProvider ->
            NormalTextUiField(
                id = fieldIdProvider(),
                safeItemFieldKind = SafeItemFieldKind.Iban,
                fieldDescription = mutableStateOf(LbcTextSpec.StringResource(id = OSString.fieldName_iban)),
                placeholder = LbcTextSpec.StringResource(id = OSString.fieldName_iban),
                valueCleaner = IbanValueCleaner,
            )
        }

        private val SocialSecurityNumberFieldType: StandardItemFieldType = StandardItemFieldType(
            id = "socialSecurityNumber",
            safeItemFieldKind = SafeItemFieldKind.SocialSecurityNumber,
            titleText = LbcTextSpec.StringResource(OSString.fieldName_socialSecurityNumber),
            iconRes = OSDrawable.ic_person,
            isSecured = false,
        ) { fieldIdProvider ->
            NormalTextUiField(
                id = fieldIdProvider(),
                safeItemFieldKind = SafeItemFieldKind.SocialSecurityNumber,
                fieldDescription = mutableStateOf(LbcTextSpec.StringResource(id = OSString.fieldName_socialSecurityNumber)),
                placeholder = LbcTextSpec.StringResource(id = OSString.fieldName_socialSecurityNumber),
            )
        }

        fun getMainItemFieldTypes(): List<ItemFieldType> = listOf(
            EmailItemFieldType,
            PasswordItemFieldType,
            NumberItemFieldType,
            NoteItemFieldType,
            PhoneItemFieldType,
            UrlItemFieldType,
            TextItemFieldType,
        )

        fun getTimeItemFieldTypes(): List<ItemFieldType> = listOf(
            DateItemFieldType,
            DateAndTimeItemFieldType,
            TimeItemFieldType,
        )

        fun getSensitiveItemFieldType(): List<ItemFieldType> = listOf(
            SecuredNoteItemFieldType,
            CreditCardNumberItemFieldType,
            IbanItemFieldType,
            SocialSecurityNumberFieldType,
        )
    }
}

@Stable
class NotImplementedItemFieldType(
    override val id: String,
    override val titleText: LbcTextSpec,
    @DrawableRes override val iconRes: Int,
) : ItemFieldType

@Stable
data class StandardItemFieldType(
    override val id: String,
    val safeItemFieldKind: SafeItemFieldKind,
    override val titleText: LbcTextSpec,
    val isSecured: Boolean,
    @DrawableRes override val iconRes: Int,
    val instantiateUiField: (FieldIdProvider) -> UiField,
) : ItemFieldType
