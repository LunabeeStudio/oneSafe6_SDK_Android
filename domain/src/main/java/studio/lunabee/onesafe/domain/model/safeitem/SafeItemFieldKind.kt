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
    val mimeType: String,
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
                Photo.id -> Photo
                Video.id -> Video
                File.id -> File
                else -> Unknown(raw)
            }
        }

        fun isKindFile(kind: SafeItemFieldKind): Boolean {
            return when (kind) {
                is File,
                is Photo,
                is Video,
                -> true
                else -> false
            }
        }

        const val textMimeType: String = "text/plain"
        const val allMimeType: String = "*/*"
        const val imageMimeType: String = "image/*"
        const val videoMimeType: String = "video/*"
    }

    data class Unknown(override val id: String) : SafeItemFieldKind(
        font = SafeItemFieldFont.Default,
        inputType = SafeItemInputType.Default,
        mimeType = textMimeType,
    )

    data object Text : SafeItemFieldKind(
        font = SafeItemFieldFont.Default,
        inputType = SafeItemInputType.Default,
        mimeType = textMimeType,
    ) {
        override val id: String = "text"
    }

    data object Url : SafeItemFieldKind(
        font = SafeItemFieldFont.Default,
        inputType = SafeItemInputType.Url,
        mimeType = textMimeType,
    ) {
        override val id: String = "url"
    }

    data object Password : SafeItemFieldKind(
        font = SafeItemFieldFont.Legibility,
        inputType = SafeItemInputType.Password,
        maskList = FieldMask.PasswordMaks,
        mimeType = textMimeType,
    ) {
        override val id: String = "password"
    }

    object Email : SafeItemFieldKind(
        font = SafeItemFieldFont.Default,
        inputType = SafeItemInputType.Email,
        mimeType = textMimeType,
    ) {
        override val id: String = "email"
    }

    data object Note : SafeItemFieldKind(
        font = SafeItemFieldFont.Default,
        inputType = SafeItemInputType.Default,
        mimeType = textMimeType,
    ) {
        override val id: String = "note"
    }

    data object Phone : SafeItemFieldKind(
        font = SafeItemFieldFont.Default,
        inputType = SafeItemInputType.Phone,
        mimeType = textMimeType,
    ) {
        override val id: String = "phone"
    }

    data object Date : SafeItemFieldKind(
        font = SafeItemFieldFont.Default,
        inputType = SafeItemInputType.Default,
        mimeType = textMimeType,
    ) {
        override val id: String = "date"
    }

    data object Hour : SafeItemFieldKind(
        font = SafeItemFieldFont.Default,
        inputType = SafeItemInputType.Default,
        mimeType = textMimeType,
    ) {
        override val id: String = "time"
    }

    data object DateAndHour : SafeItemFieldKind(
        font = SafeItemFieldFont.Default,
        inputType = SafeItemInputType.Default,
        mimeType = textMimeType,
    ) {
        override val id: String = "dateAndTime"
    }

    data object Number : SafeItemFieldKind(
        font = SafeItemFieldFont.Default,
        inputType = SafeItemInputType.Number,
        mimeType = textMimeType,
    ) {
        override val id: String = "number"
    }

    object CreditCardNumber : SafeItemFieldKind(
        font = SafeItemFieldFont.Default,
        inputType = SafeItemInputType.Number,
        maskList = FieldMask.CreditCardMasks,
        mimeType = textMimeType,
    ) {
        override val id: String = "creditCardNumber"
    }

    object YearMonth : SafeItemFieldKind(
        font = SafeItemFieldFont.Default,
        inputType = SafeItemInputType.Number,
        maskList = FieldMask.MonthYearDateMasks,
        mimeType = textMimeType,
    ) {
        override val id: String = "monthYear"
    }

    data object Iban : SafeItemFieldKind(
        font = SafeItemFieldFont.Default,
        inputType = SafeItemInputType.DefaultCap,
        maskList = FieldMask.IbanMasks,
        mimeType = textMimeType,
    ) {
        override val id: String = "iban"
    }

    data object SocialSecurityNumber : SafeItemFieldKind(
        font = SafeItemFieldFont.Default,
        inputType = SafeItemInputType.Number,
        maskList = FieldMask.SocialSecurityNumberMasks,
        mimeType = textMimeType,
    ) {
        override val id: String = "socialSecurityNumber"
    }

    data object File : SafeItemFieldKind(
        font = SafeItemFieldFont.Default,
        inputType = SafeItemInputType.Default,
        mimeType = allMimeType,
    ) {
        override val id: String = "file"
    }

    data object Photo : SafeItemFieldKind(
        font = SafeItemFieldFont.Default,
        inputType = SafeItemInputType.Default,
        mimeType = imageMimeType,
    ) {
        override val id: String = "photo"
    }

    data object Video : SafeItemFieldKind(
        font = SafeItemFieldFont.Default,
        inputType = SafeItemInputType.Default,
        mimeType = videoMimeType,
    ) {
        override val id: String = "video"
    }
}
