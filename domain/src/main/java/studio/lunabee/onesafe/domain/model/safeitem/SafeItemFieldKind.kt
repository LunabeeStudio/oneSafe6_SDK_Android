/*
 * Copyright (c) 2023 Lunabee Studio
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Created by Lunabee Studio / Date - 4/7/2023 - for the oneSafe6 SDK.
 * Last modified 4/7/23, 12:24 AM
 */

package studio.lunabee.onesafe.domain.model.safeitem

import studio.lunabee.onesafe.domain.model.common.IdentifiableObject

sealed class SafeItemFieldKind(
    val font: SafeItemFieldFont,
    val inputType: SafeItemInputType,
    val maskList: List<FieldMask> = listOf(),
) : IdentifiableObject {

    companion object {
        fun fromString(raw: String): SafeItemFieldKind {
            return when (raw) {
                Text.id -> Text
                Url.id -> Url
                Password.id -> Password
                Email.id -> Email
                Note.id -> Note
                Phone.id -> Phone
                Date.id -> Date
                Hour.id -> Hour
                DateAndHour.id -> DateAndHour
                Number.id -> Number
                CreditCardNumber.id -> CreditCardNumber
                Iban.id -> Iban
                SocialSecurityNumber.id -> SocialSecurityNumber
                YearMonth.id -> YearMonth
                else -> Unknown(raw)
            }
        }
    }

    data class Unknown(override val id: String) : SafeItemFieldKind(
        font = SafeItemFieldFont.Default,
        inputType = SafeItemInputType.Default,
    )

    object Text : SafeItemFieldKind(
        font = SafeItemFieldFont.Default,
        inputType = SafeItemInputType.Default,
    ) {
        override val id: String = "text"
    }

    object Url : SafeItemFieldKind(
        font = SafeItemFieldFont.Default,
        inputType = SafeItemInputType.Url,
    ) {
        override val id: String = "url"
    }

    object Password : SafeItemFieldKind(
        font = SafeItemFieldFont.Legibility,
        inputType = SafeItemInputType.Password,
        maskList = FieldMask.PasswordMaks,
    ) {
        override val id: String = "password"
    }

    object Email : SafeItemFieldKind(
        font = SafeItemFieldFont.Default,
        inputType = SafeItemInputType.Email,
    ) {
        override val id: String = "email"
    }

    object Note : SafeItemFieldKind(
        font = SafeItemFieldFont.Default,
        inputType = SafeItemInputType.Default,
    ) {
        override val id: String = "note"
    }

    object Phone : SafeItemFieldKind(
        font = SafeItemFieldFont.Default,
        inputType = SafeItemInputType.Phone,
    ) {
        override val id: String = "phone"
    }

    object Date : SafeItemFieldKind(
        font = SafeItemFieldFont.Default,
        inputType = SafeItemInputType.Default,
    ) {
        override val id: String = "date"
    }

    object Hour : SafeItemFieldKind(
        font = SafeItemFieldFont.Default,
        inputType = SafeItemInputType.Default,
    ) {
        override val id: String = "time"
    }

    object DateAndHour : SafeItemFieldKind(
        font = SafeItemFieldFont.Default,
        inputType = SafeItemInputType.Default,
    ) {
        override val id: String = "dateAndTime"
    }

    object Number : SafeItemFieldKind(
        font = SafeItemFieldFont.Default,
        inputType = SafeItemInputType.Number,
    ) {
        override val id: String = "number"
    }

    object CreditCardNumber : SafeItemFieldKind(
        font = SafeItemFieldFont.Default,
        inputType = SafeItemInputType.Number,
        maskList = FieldMask.CreditCardMasks,
    ) {
        override val id: String = "creditCardNumber"
    }

    object YearMonth : SafeItemFieldKind(
        font = SafeItemFieldFont.Default,
        inputType = SafeItemInputType.Number,
        maskList = FieldMask.MonthYearDateMasks,
    ) {
        override val id: String = "monthYear"
    }

    object Iban : SafeItemFieldKind(
        font = SafeItemFieldFont.Default,
        inputType = SafeItemInputType.Default,
        maskList = FieldMask.IbanMasks,
    ) {
        override val id: String = "iban"
    }

    object SocialSecurityNumber : SafeItemFieldKind(
        font = SafeItemFieldFont.Default,
        inputType = SafeItemInputType.Number,
        maskList = FieldMask.SocialSecurityNumberMasks,
    ) {
        override val id: String = "socialSecurityNumber"
    }
}
