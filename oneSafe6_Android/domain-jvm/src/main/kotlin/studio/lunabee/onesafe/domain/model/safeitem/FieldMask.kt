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

import studio.lunabee.onesafe.domain.utils.hasMatchRegex

data class FieldMask(
    val formattingMask: String?,
    val securedDisplayingMask: String,
) {
    fun isMatchingWithValue(value: String): Boolean {
        val formattingMask = formattingMask ?: return true

        val maskedValue = applyMaskToValue(value)
        return if (maskedValue.length > formattingMask.length) {
            false
        } else {
            val symbolsIndices = formattingMask.indices.filter { index ->
                SpecialSymbols.map { it.second }.contains(formattingMask[index])
            }
            val clearedMask = formattingMask.filterIndexed { index, _ -> symbolsIndices.contains(index) }
            val clearedValue = maskedValue.filterIndexed { index, _ -> symbolsIndices.contains(index) }
            clearedValue
                .mapIndexed { index, char ->
                    clearedMask.getOrNull(index)?.let { maskChar ->
                        SpecialSymbols.firstOrNull { it.second == maskChar }?.let { (regex, _) ->
                            char.toString().hasMatchRegex(regex)
                        } ?: false
                    } ?: false
                }.all { it }
        }
    }

    private fun applyMaskToValue(value: String): String {
        if (formattingMask != null) {
            val spacerSymbolsIndices = formattingMask.indices.filter {
                !SpecialSymbols.map { it.second }.contains(formattingMask[it])
            }

            var out = ""
            var maskIndex = 0
            value.forEach { char ->
                while (spacerSymbolsIndices.contains(maskIndex)) {
                    out += formattingMask[maskIndex]
                    maskIndex++
                }
                if (char != formattingMask.getOrNull(maskIndex - 1)) {
                    out += char
                    maskIndex++
                }
            }
            return out
        }
        return value
    }

    companion object {
        private val LowerCaseSymbol: Pair<String, Char> = Pair("[a-z]", 'a')
        private val CapitalizeSymbol: Pair<String, Char> = Pair("[A-Z]", 'A')
        private val FigureSymbol: Pair<String, Char> = Pair("[0-9]", '#')
        private val AnySymbol: Pair<String, Char> = Pair(".", '*')
        private val HiddenSymbol: Pair<String, Char> = Pair(".", '•')
        val SpecialSymbols: List<Pair<String, Char>> =
            listOf(LowerCaseSymbol, CapitalizeSymbol, FigureSymbol, AnySymbol)

        val CreditCardMasks: List<FieldMask> = listOf(
            // 4-6
            FieldMask("#### ######", "•••• ••****"),
            // 4-6-5
            FieldMask("#### ###### #####", "**•• •••••• *****"),
            // 4-4-5
            FieldMask("#### #### #####", "**•• •••• *****"),
            // 4-4-4-4
            FieldMask("#### #### #### ####", "**•• •••• •••• ****"),
            // 4-4-4-4-3
            FieldMask("#### #### #### #### ###", "**•• •••• •••• •••• ***"),
        )

        val MonthYearDateMasks: List<FieldMask> = listOf(
            FieldMask("##/##", "**/**"),
        )

        val IbanMasks: List<FieldMask> = listOf(
            FieldMask("**** **** **** **** **** **** **** **** ****", "**** •••• •••• ***"),
        )

        val SocialSecurityNumberMasks: List<FieldMask> = listOf(
            FieldMask("# ## ## ## ### ###", "# ## •• •• ••• ###"),
            FieldMask("# ## ## ## ### ### ##", "# ## •• •• ••• ••• ##"),
        )

        val PasswordMaks: List<FieldMask> = listOf(
            FieldMask(null, "••••••"),
        )

        fun getMatchingMask(maskList: List<FieldMask>, value: String): FieldMask? = maskList.firstOrNull {
            it
                .isMatchingWithValue(value)
        }

        fun getApplyMaskOnString(value: CharArray, mask: String): String {
            var indexOnValue = 0
            var formattedString = ""
            mask.forEach { char ->
                when {
                    indexOnValue >= value.size -> return@forEach
                    SpecialSymbols.map { it.second }.contains(char) || value.getOrNull(indexOnValue) == char -> {
                        formattedString += value.getOrNull(indexOnValue) ?: ""
                        indexOnValue++
                    }
                    char == HiddenSymbol.second -> {
                        indexOnValue++
                        formattedString += char
                    }
                    else -> formattedString += char
                }
            }
            return formattedString
        }
    }
}
