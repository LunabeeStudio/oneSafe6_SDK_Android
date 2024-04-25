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
 * Created by Lunabee Studio / Date - 3/19/2024 - for the oneSafe6 SDK.
 * Last modified 3/19/24, 5:31 PM
 */

package studio.lunabee.onesafe.domain.model.crypto

import com.lunabee.lbextensions.remove
import studio.lunabee.onesafe.error.OSDomainError
import studio.lunabee.onesafe.error.OSError.Companion.get
import studio.lunabee.onesafe.randomize
import java.io.Closeable

@JvmInline
value class DatabaseKey(val raw: ByteArray) : Closeable {

    constructor(key: String) : this(fromString(key))

    init {
        check(raw.size == DatabaseKeyByteSize)
    }

    // https://www.baeldung.com/kotlin/byte-arrays-to-hex-strings#loops-and-bitwise-operations
    fun asCharArray(): CharArray {
        val hexChars = CharArray(raw.size * 2)
        raw.indices.forEach { idx ->
            val v = raw[idx].toInt() and 0xFF
            hexChars[idx * 2] = hexArray[v ushr 4]
            hexChars[idx * 2 + 1] = hexArray[v and 0x0F]
        }
        return hexChars
    }

    companion object {
        private val hexArray = "0123456789ABCDEF".toCharArray()
        const val DatabaseKeyByteSize: Int = 32
        const val hexPrefix: String = "0x"

        // https://stackoverflow.com/a/66614516/9994620
        private fun fromString(key: String): ByteArray {
            val rawString = key.removePrefix(hexPrefix).remove(" ")
            return try {
                rawString.chunked(2)
                    .map { it.toInt(16).toByte() }
                    .toByteArray().also {
                        if (it.size != DatabaseKeyByteSize) throw OSDomainError.Code.DATABASE_KEY_BAD_FORMAT.get()
                    }
            } catch (e: NumberFormatException) {
                throw OSDomainError.Code.DATABASE_KEY_BAD_FORMAT.get(cause = e)
            }
        }
    }

    override fun close() {
        raw.randomize()
    }
}
