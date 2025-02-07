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
 * Last modified 9/4/24, 9:51 AM
 */

package studio.lunabee.onesafe.jvm

import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.security.SecureRandom
import java.util.Arrays

fun CharArray.randomize(): CharArray {
    val random = SecureRandom()
    val randomBytes = ByteArray(this.size * 4)
    random.nextBytes(randomBytes)
    val byteBuffer = ByteBuffer.wrap(randomBytes)
    val charBuffer = Charsets.UTF_16.decode(byteBuffer)
    return Arrays.copyOfRange(
        charBuffer.array(),
        charBuffer.position(),
        charBuffer.limit(),
    ).copyInto(this, endIndex = this.size)
}

inline fun <T> CharArray.use(block: (CharArray) -> T): T {
    return try {
        block(this)
    } finally {
        this.randomize()
    }
}

// https://stackoverflow.com/a/9670279
fun CharArray.toByteArray(): ByteArray {
    val charBuffer = CharBuffer.wrap(this)
    val byteBuffer = Charsets.UTF_8.encode(charBuffer)
    return Arrays.copyOfRange(
        byteBuffer.array(),
        byteBuffer.position(),
        byteBuffer.limit(),
    )
}
