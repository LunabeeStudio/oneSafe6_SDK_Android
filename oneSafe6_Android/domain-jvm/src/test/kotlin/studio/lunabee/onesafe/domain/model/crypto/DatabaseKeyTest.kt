/*
 * Copyright (c) 2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 3/20/2024 - for the oneSafe6 SDK.
 * Last modified 3/20/24, 11:37 AM
 */

package studio.lunabee.onesafe.domain.model.crypto

import studio.lunabee.onesafe.error.OSDomainError
import studio.lunabee.onesafe.test.assertDoesNotThrow
import studio.lunabee.onesafe.test.assertThrows
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class DatabaseKeyTest {

    @Test
    fun asCharArray_test() {
        val databaseKey = DatabaseKey(Random.Default.nextBytes(DatabaseKey.DatabaseKeyByteSize))
        val actual = databaseKey.asCharArray()
        val expected = databaseKey.raw.joinToString("") { "%02X".format(it) }.toCharArray()
        assertContentEquals(expected, actual)
    }

    @Test
    fun string_ctor_test() {
        listOf(
            "0123456789ABCDEF0123456789abcdef0123456789ABCDEF0123456789abcdef", // string ok
            "0x0123456789ABCDEF0123456789abcdef0123456789ABCDEF0123456789abcdef", // string ok prefix
            " 0123456789ABCDEF01234567  89abcdef0123456789ABCDEF0123456789abcdef ", // string ok whitespaces
        ).forEach { keyString ->
            val actual = assertDoesNotThrow { DatabaseKey(keyString) }.raw
            assertContentEquals(expected, actual)
        }

        listOf(
            "0123456789ABCDEF0123456789abcdef0123456789ABCDEF0123456789abcd ", // string too short
            "0123456789ABCDEF0123456789abcdef0123456789ABCDEF0123456789abcdeZ", // string wrong char
            "", // string empty
        ).forEach { keyString ->
            val actualCode = assertThrows<OSDomainError> { DatabaseKey(keyString) }.code
            assertEquals(OSDomainError.Code.DATABASE_KEY_BAD_FORMAT, actualCode)
        }
    }

    private val expected: ByteArray = byteArrayOf(
        1,
        35,
        69,
        103,
        -119,
        -85,
        -51,
        -17,
        1,
        35,
        69,
        103,
        -119,
        -85,
        -51,
        -17,
        1,
        35,
        69,
        103,
        -119,
        -85,
        -51,
        -17,
        1,
        35,
        69,
        103,
        -119,
        -85,
        -51,
        -17,
    )
}
