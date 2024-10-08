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
import java.util.Arrays
import java.util.UUID

fun ByteArray.toCharArray(): CharArray {
    val byteBuffer = ByteBuffer.wrap(this)
    val charBuffer = Charsets.UTF_8.decode(byteBuffer)
    return Arrays.copyOfRange(
        charBuffer.array(),
        charBuffer.position(),
        charBuffer.limit(),
    )
}

// Copied from Room
// https://android-review.googlesource.com/c/platform/frameworks/support/+/1812173/9/room/room-runtime/src/main/java/androidx/room/util/UUIDUtil.java
fun ByteArray.toUUID(): UUID {
    val buffer = ByteBuffer.wrap(this)
    val firstLong = buffer.long
    val secondLong = buffer.long
    return UUID(firstLong, secondLong)
}

fun UUID.toByteArray(): ByteArray {
    val bytes = ByteArray(16)
    val buffer = ByteBuffer.wrap(bytes)
    buffer.putLong(mostSignificantBits)
    buffer.putLong(leastSignificantBits)
    return buffer.array()
}
