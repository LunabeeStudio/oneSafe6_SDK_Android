/*
 * Copyright (c) 2023-2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 9/4/2024 - for the oneSafe6 SDK.
 * Last modified 8/30/24, 10:58 AM
 */

package studio.lunabee.onesafe.cryptography.android

import studio.lunabee.onesafe.cryptography.toByteArray
import studio.lunabee.onesafe.cryptography.toInt
import studio.lunabee.onesafe.error.OSCryptoError
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CustomMapper {

    @Test
    fun int_byteArray_conversion_max() {
        val byteArrayValue = Int.MAX_VALUE.toByteArray()
        val convertedValue = byteArrayValue.toInt()
        assertEquals(Int.MAX_VALUE, convertedValue)
    }

    @Test
    fun int_byteArray_conversion_min() {
        val byteArrayValue = Int.MIN_VALUE.toByteArray()
        val convertedValue = byteArrayValue.toInt()
        assertEquals(Int.MIN_VALUE, convertedValue)
    }

    @Test
    fun int_byteArray_conversion_zero() {
        val byteArrayValue = 0.toByteArray()
        val convertedValue = byteArrayValue.toInt()
        assertEquals(0, convertedValue)
    }

    @Test
    fun int_byteArray_conversion_random() {
        val random = Random.nextInt()
        val byteArrayValue = random.toByteArray()
        val convertedValue = byteArrayValue.toInt()
        assertEquals(random, convertedValue)
    }

    @Test
    fun byteArray_to_int_illegal() {
        val error1 = assertFailsWith<OSCryptoError> {
            byteArrayOf(0).toInt()
        }
        assertEquals(error1.code, OSCryptoError.Code.ILLEGAL_VALUE)

        val error2 = assertFailsWith<OSCryptoError> {
            byteArrayOf(10).toInt()
        }
        assertEquals(error2.code, OSCryptoError.Code.ILLEGAL_VALUE)
    }
}
