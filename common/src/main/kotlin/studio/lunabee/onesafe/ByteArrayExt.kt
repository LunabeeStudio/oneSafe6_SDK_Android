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

package studio.lunabee.onesafe

import java.nio.ByteBuffer
import java.security.SecureRandom
import java.util.Arrays

fun ByteArray.randomize(): ByteArray {
    SecureRandom().nextBytes(this)
    return this
}

inline fun <T> ByteArray.use(block: (ByteArray) -> T): T {
    return try {
        block(this)
    } finally {
        this.randomize()
    }
}

fun ByteArray.toCharArray(): CharArray {
    val byteBuffer = ByteBuffer.wrap(this)
    val charBuffer = Charsets.UTF_8.decode(byteBuffer)
    return Arrays.copyOfRange(
        charBuffer.array(),
        charBuffer.position(),
        charBuffer.limit(),
    )
}
