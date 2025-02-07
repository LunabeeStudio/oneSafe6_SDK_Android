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

import kotlin.test.Test

class SafeItemFieldKindTest {
    private val text: String = "text"
    private val password: String = "password"
    private val note: String = "note"
    private val email: String = "email"
    private val phone: String = "phone"
    private val date: String = "date"
    private val number: String = "number"
    private val rawUnknown: String = "raw_unknown"

    @Test
    fun fromStringTest() {
        assert(SafeItemFieldKind.fromString(text) is SafeItemFieldKind.Text) {
            "Expected ${SafeItemFieldKind.Text::class.simpleName} instance"
        }
        assert(SafeItemFieldKind.fromString(password) is SafeItemFieldKind.Password) {
            "Expected ${SafeItemFieldKind.Password::class.simpleName} instance"
        }
        assert(SafeItemFieldKind.fromString(note) is SafeItemFieldKind.Note) {
            "Expected ${SafeItemFieldKind.Note::class.simpleName} instance"
        }
        assert(SafeItemFieldKind.fromString(email) is SafeItemFieldKind.Email) {
            "Expected ${SafeItemFieldKind.Email::class.simpleName} instance"
        }
        assert(SafeItemFieldKind.fromString(phone) is SafeItemFieldKind.Phone) {
            "Expected ${SafeItemFieldKind.Phone::class.simpleName} instance"
        }
        assert(SafeItemFieldKind.fromString(date) is SafeItemFieldKind.Date) {
            "Expected ${SafeItemFieldKind.Date::class.simpleName} instance"
        }
        assert(SafeItemFieldKind.fromString(number) is SafeItemFieldKind.Number) {
            "Expected ${SafeItemFieldKind.Number::class.simpleName} instance"
        }
        assert((SafeItemFieldKind.fromString(rawUnknown) as SafeItemFieldKind.Unknown).id == rawUnknown) {
            "Expected ${SafeItemFieldKind.Unknown::class.simpleName} instance with raw = $rawUnknown"
        }
    }
}
