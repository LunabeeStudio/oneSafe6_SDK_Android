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
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ItemFieldMaskTest {

    private val creditCardMask: FieldMask = FieldMask("#### #### #### ####", "**•• •••• •••• ****")

    // with letter
    private val withLetterMask: FieldMask = FieldMask("AAaa-####", "••••-****")

    // with any
    private val withAnyMask: FieldMask = FieldMask("**** AAaa ####", "•••• •••• ****")

    @Test
    fun credit_card_mask_test() {
        val goodValueFull = "1234123412341234"
        val goodValueNotAllChars = "1234123412"
        val badValueTooMuchChars = "123412341234123412341234"
        val badValueBadChars = "aaaabbbbccccdddd"
        assertTrue { creditCardMask.isMatchingWithValue(goodValueFull) }
        assertTrue { creditCardMask.isMatchingWithValue(goodValueNotAllChars) }
        assertFalse { creditCardMask.isMatchingWithValue(badValueTooMuchChars) }
        assertFalse { creditCardMask.isMatchingWithValue(badValueBadChars) }

        val formattingResultExpected = "1234 1234 1234 1234"
        val securedResultExpected = "12•• •••• •••• 1234"
        assertEquals(
            formattingResultExpected,
            FieldMask.getApplyMaskOnString(goodValueFull.toCharArray(), creditCardMask.formattingMask.orEmpty()),
        )
        assertEquals(
            securedResultExpected,
            FieldMask.getApplyMaskOnString(goodValueFull.toCharArray(), creditCardMask.securedDisplayingMask),
        )
    }

    @Test
    fun with_letter_mask_test() {
        val goodValueFull = "ABab1234"
        val goodValueNotAllChars = "ABab1"
        val badValueTooMuchChars = "ABab12341234"
        val badValueBadChars = "A123abab"
        assertTrue { withLetterMask.isMatchingWithValue(goodValueFull) }
        assertTrue { withLetterMask.isMatchingWithValue(goodValueNotAllChars) }
        assertFalse { withLetterMask.isMatchingWithValue(badValueTooMuchChars) }
        assertFalse { withLetterMask.isMatchingWithValue(badValueBadChars) }

        val formattingResultExpected = "ABab-1234"
        val securedResultExpected = "••••-1234"
        assertEquals(
            formattingResultExpected,
            FieldMask.getApplyMaskOnString(goodValueFull.toCharArray(), withLetterMask.formattingMask.orEmpty()),
        )
        assertEquals(
            securedResultExpected,
            FieldMask.getApplyMaskOnString(goodValueFull.toCharArray(), withLetterMask.securedDisplayingMask),
        )
    }

    @Test
    fun with_any_mask_test() {
        val goodValueFull = "ééééABab1234"
        val goodValueNotAllChars = "éé"
        val badValueTooMuchChars = "ééééABab1234AZER"
        val badValueBadChars = "ABCD12341234"
        assertTrue { withAnyMask.isMatchingWithValue(goodValueFull) }
        assertTrue { withAnyMask.isMatchingWithValue(goodValueNotAllChars) }
        assertFalse { withAnyMask.isMatchingWithValue(badValueTooMuchChars) }
        assertFalse { withAnyMask.isMatchingWithValue(badValueBadChars) }

        val formattingResultExpected = "éééé ABab 1234"
        val securedResultExpected = "•••• •••• 1234"
        assertEquals(
            formattingResultExpected,
            FieldMask.getApplyMaskOnString(goodValueFull.toCharArray(), withAnyMask.formattingMask.orEmpty()),
        )
        assertEquals(
            securedResultExpected,
            FieldMask.getApplyMaskOnString(goodValueFull.toCharArray(), withAnyMask.securedDisplayingMask),
        )
    }

    @Test
    fun credit_card_mask_self_format_test() {
        val formattedInput = "1234 1234 1234 1234"
        assertTrue { creditCardMask.isMatchingWithValue(formattedInput) }
        val formattingResultExpected = "1234 1234 1234 1234"
        val securedResultExpected = "12•• •••• •••• 1234"
        assertEquals(
            formattingResultExpected,
            FieldMask.getApplyMaskOnString(formattedInput.toCharArray(), creditCardMask.formattingMask.orEmpty()),
        )
        assertEquals(
            securedResultExpected,
            FieldMask.getApplyMaskOnString(formattedInput.toCharArray(), creditCardMask.securedDisplayingMask),
        )
    }
}
