package studio.lunabee.onesafe.feature.itemform.model

import androidx.compose.runtime.mutableStateOf
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.domain.common.FieldIdProvider
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.feature.home.model.ItemCreationEntryWithTemplate
import studio.lunabee.onesafe.feature.itemform.model.uifield.UiField
import studio.lunabee.onesafe.feature.itemform.model.uifield.text.impl.NormalTextUiField
import studio.lunabee.onesafe.feature.itemform.model.uifield.text.impl.PasswordTextUiField
import studio.lunabee.onesafe.feature.itemform.model.uifield.text.impl.UrlTextUiField
import studio.lunabee.onesafe.feature.itemform.model.uifield.text.impl.YearMonthDateTextUiField

sealed class ItemCreationTemplateUiField(
    val getTextFields: (fieldIdProvider: FieldIdProvider) -> List<UiField>,
) {
    data object CreditCard : ItemCreationTemplateUiField(
        getTextFields = { fieldIdProvider ->
            listOf(
                PasswordTextUiField(
                    id = fieldIdProvider(),
                    safeItemFieldKind = SafeItemFieldKind.CreditCardNumber,
                    fieldDescription = mutableStateOf(LbcTextSpec.StringResource(OSString.fieldName_card_number)),
                    placeholder = LbcTextSpec.StringResource(OSString.safeItemDetail_card_number_placeholder),
                ).also { it.isIdentifier = true },
                YearMonthDateTextUiField(
                    id = fieldIdProvider(),
                    fieldDescription = mutableStateOf(LbcTextSpec.StringResource(OSString.fieldName_card_expiryDate)),
                    placeholder = LbcTextSpec.StringResource(OSString.safeItemDetail_card_expirationDate_placeholder),
                    safeItemFieldKind = SafeItemFieldKind.YearMonth,
                ),
                NormalTextUiField(
                    id = fieldIdProvider(),
                    fieldDescription = mutableStateOf(LbcTextSpec.StringResource(OSString.fieldName_card_holderName)),
                    placeholder = LbcTextSpec.StringResource(OSString.safeItemDetail_card_owner_placeholder),
                    safeItemFieldKind = SafeItemFieldKind.Text,
                ),
                PasswordTextUiField.pin(
                    id = fieldIdProvider(),
                    fieldDescription = LbcTextSpec.StringResource(OSString.fieldName_card_cvv),
                    placeholder = LbcTextSpec.StringResource(OSString.safeItemDetail_card_cvc_placeholder),
                ),
                PasswordTextUiField.pin(
                    id = fieldIdProvider(),
                    fieldDescription = LbcTextSpec.StringResource(OSString.fieldName_card_code),
                    placeholder = LbcTextSpec.StringResource(OSString.safeItemDetail_card_pinCode_placeholder),
                ),
            )
        },
    )

    data object Website : ItemCreationTemplateUiField(
        getTextFields = { fieldIdProvider ->
            listOf(
                UrlTextUiField(
                    id = fieldIdProvider(),
                    fieldDescription = mutableStateOf(LbcTextSpec.StringResource(OSString.fieldName_url)),
                    placeholder = LbcTextSpec.StringResource(OSString.safeItemDetail_url_link_placeholder),
                    safeItemFieldKind = SafeItemFieldKind.Url,
                ),
                NormalTextUiField(
                    id = fieldIdProvider(),
                    fieldDescription = mutableStateOf(LbcTextSpec.StringResource(OSString.fieldName_email)),
                    placeholder = LbcTextSpec.StringResource(OSString.fieldName_email),
                    safeItemFieldKind = SafeItemFieldKind.Email,
                ).also { it.isIdentifier = true },
                defaultItemPasswordField(fieldIdProvider),
            )
        },
    )

    data object Application : ItemCreationTemplateUiField(
        getTextFields = { fieldIdProvider ->
            listOf(
                NormalTextUiField(
                    id = fieldIdProvider(),
                    fieldDescription = mutableStateOf(LbcTextSpec.StringResource(OSString.fieldName_email)),
                    placeholder = LbcTextSpec.StringResource(OSString.fieldName_email),
                    safeItemFieldKind = SafeItemFieldKind.Email,
                ).also { it.isIdentifier = true },
                defaultItemPasswordField(fieldIdProvider),
            )
        },
    )

    data object Folder : ItemCreationTemplateUiField(
        getTextFields = { listOf() },
    )

    data object Note : ItemCreationTemplateUiField(
        getTextFields = { fieldIdProvider ->
            listOf(
                NormalTextUiField(
                    id = fieldIdProvider(),
                    fieldDescription = mutableStateOf(LbcTextSpec.StringResource(OSString.fieldName_note)),
                    placeholder = LbcTextSpec.StringResource(OSString.fieldName_note),
                    safeItemFieldKind = SafeItemFieldKind.Note,
                ),
            )
        },
    )

    data object Custom : ItemCreationTemplateUiField(
        getTextFields = { listOf() },
    )

    companion object {
        private fun defaultItemPasswordField(fieldIdProvider: FieldIdProvider): UiField =
            PasswordTextUiField(
                id = fieldIdProvider(),
                safeItemFieldKind = SafeItemFieldKind.Password,
                fieldDescription = mutableStateOf(LbcTextSpec.StringResource(OSString.fieldName_password)),
                placeholder = LbcTextSpec.StringResource(OSString.safeItemDetail_common_password_placeholder),
            )

        fun fromItemCreationTemplate(
            fieldIdProvider: FieldIdProvider,
            itemCreationTemplate: ItemCreationEntryWithTemplate.Template,
        ): List<UiField> {
            return when (itemCreationTemplate) {
                ItemCreationEntryWithTemplate.Template.CreditCard -> CreditCard
                ItemCreationEntryWithTemplate.Template.Website -> Website
                ItemCreationEntryWithTemplate.Template.Application -> Application
                ItemCreationEntryWithTemplate.Template.Folder -> Folder
                ItemCreationEntryWithTemplate.Template.Custom -> Custom
                ItemCreationEntryWithTemplate.Template.Note -> Note
            }.getTextFields(fieldIdProvider)
        }
    }
}
