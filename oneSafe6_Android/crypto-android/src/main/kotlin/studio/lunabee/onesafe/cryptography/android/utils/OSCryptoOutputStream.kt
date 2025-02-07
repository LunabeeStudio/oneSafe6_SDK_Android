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
 * Created by Lunabee Studio / Date - 2/19/2024 - for the oneSafe6 SDK.
 * Last modified 2/19/24, 4:01 PM
 */

package studio.lunabee.onesafe.cryptography.android.utils

import studio.lunabee.onesafe.error.OSCryptoError
import studio.lunabee.onesafe.jvm.get
import java.io.FilterOutputStream
import java.io.IOException
import java.io.OutputStream
import java.security.GeneralSecurityException

/**
 * An output stream which wraps crypto errors to [OSCryptoError]
 */
class OSCryptoOutputStream(
    cryptoStream: OutputStream,
) : FilterOutputStream(cryptoStream) {
    override fun write(b: Int): Unit = safeWrite {
        super.write(b)
    }

    override fun write(b: ByteArray?): Unit = safeWrite {
        super.write(b)
    }

    override fun write(b: ByteArray?, off: Int, len: Int): Unit = safeWrite {
        super.write(b, off, len)
    }

    private inline fun <T : Any> safeWrite(write: () -> T): T {
        return try {
            write()
        } catch (e: GeneralSecurityException) {
            throw OSCryptoError.Code.DECRYPT_STREAM_CRYPTO_FAILURE.get(cause = e)
        } catch (e: IOException) { // might happen when the stream produce corrupted data
            throw OSCryptoError.Code.DECRYPT_STREAM_IO_FAILURE.get(cause = e)
        }
    }
}
