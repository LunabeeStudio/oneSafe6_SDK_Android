/*
 * Copyright (c) 2024-2024 Lunabee Studio
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
 * Last modified 9/4/24, 10:39 AM
 */

package studio.lunabee.onesafe.cryptography.android

import kotlin.test.Test
import studio.lunabee.onesafe.test.assertThrows
import studio.lunabee.onesafe.cryptography.android.utils.OSCryptoInputStream
import studio.lunabee.onesafe.error.OSCryptoError
import java.io.IOException
import java.io.InputStream
import java.security.GeneralSecurityException
import kotlin.test.assertEquals

class OSCryptoInputStreamTest {

    @Test
    fun crypto_error_test() {
        val stream = object : InputStream() {
            override fun read(): Int {
                throw GeneralSecurityException()
            }
        }
        val cryptoStream = OSCryptoInputStream(stream)
        val error = assertThrows<OSCryptoError> {
            cryptoStream.read()
        }
        assertEquals(OSCryptoError.Code.DECRYPT_STREAM_CRYPTO_FAILURE, error.code)
    }

    @Test
    fun io_error_test() {
        val stream = object : InputStream() {
            override fun read(): Int {
                throw IOException()
            }
        }
        val cryptoStream = OSCryptoInputStream(stream)
        val error = assertThrows<OSCryptoError> {
            cryptoStream.read()
        }
        assertEquals(OSCryptoError.Code.DECRYPT_STREAM_IO_FAILURE, error.code)
    }
}
