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

package studio.lunabee.onesafe.cryptography

import studio.lunabee.onesafe.domain.model.common.IdentifiableObject
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.error.OSCryptoError
import java.nio.ByteBuffer
import java.util.UUID

internal fun IdentifiableObject.toByteArray(): ByteArray = id.encodeToByteArray()

internal fun ByteArray.toSafeItemFieldKind(): SafeItemFieldKind = SafeItemFieldKind.fromString(decodeToString())

// https://stackoverflow.com/a/67229929/10935947
internal fun ByteArray.toInt(): Int {
    if (this.size != 4) {
        throw OSCryptoError(OSCryptoError.Code.ILLEGAL_VALUE, "ByteArray size must be 4. Got ${this.size}.")
    }
    return (this[3].toInt() shl 24) or
        (this[2].toInt() and 0xff shl 16) or
        (this[1].toInt() and 0xff shl 8) or
        (this[0].toInt() and 0xff)
}

internal fun ByteArray.toLong(): Long {
    if (this.size != 8) {
        throw OSCryptoError(OSCryptoError.Code.ILLEGAL_VALUE, "ByteArray size must be 4. Got ${this.size}.")
    }
    return (this[7].toLong() shl 56) or
        (this[6].toLong() and 0xff shl 48) or
        (this[5].toLong() and 0xff shl 40) or
        (this[4].toLong() and 0xff shl 32) or
        (this[3].toLong() and 0xff shl 24) or
        (this[2].toLong() and 0xff shl 16) or
        (this[1].toLong() and 0xff shl 8) or
        (this[0].toLong() and 0xff)
}

// https://stackoverflow.com/a/67229929/10935947
internal fun Int.toByteArray(): ByteArray = ByteArray(4).apply {
    this[0] = (this@toByteArray shr 0).toByte()
    this[1] = (this@toByteArray shr 8).toByte()
    this[2] = (this@toByteArray shr 16).toByte()
    this[3] = (this@toByteArray shr 24).toByte()
}

internal fun Long.toByteArray(): ByteArray = ByteArray(8).apply {
    this[0] = (this@toByteArray shr 0).toByte()
    this[1] = (this@toByteArray shr 8).toByte()
    this[2] = (this@toByteArray shr 16).toByte()
    this[3] = (this@toByteArray shr 24).toByte()
    this[4] = (this@toByteArray shr 32).toByte()
    this[5] = (this@toByteArray shr 40).toByte()
    this[6] = (this@toByteArray shr 48).toByte()
    this[7] = (this@toByteArray shr 56).toByte()
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
