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
 * Created by Lunabee Studio / Date - 6/14/2023 - for the oneSafe6 SDK.
 * Last modified 6/14/23, 3:55 PM
 */

package studio.lunabee.onesafe.cryptography

import kotlinx.datetime.toKotlinInstant
import studio.lunabee.bubbles.domain.model.MessageSharingMode
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.doubleratchet.model.toDoubleRatchetUUID
import studio.lunabee.onesafe.domain.model.common.IdentifiableObject
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.error.OSCryptoError
import studio.lunabee.onesafe.toByteArray
import studio.lunabee.onesafe.toUUID
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import kotlin.reflect.KClass

class CryptoDataMapper @Inject constructor() {
    operator fun <Data : Any> invoke(mapBlock: ((Data) -> ByteArray)?, data: Data): ByteArray = when {
        mapBlock != null -> mapBlock(data)
        data is String -> data.encodeToByteArray()
        data is Int -> data.toByteArray()
        data is IdentifiableObject -> data.toByteArray()
        data is UUID -> data.toByteArray()
        data is ByteArray -> data
        data is Instant -> data.toEpochMilli().toByteArray()
        data is kotlinx.datetime.Instant -> data.toEpochMilliseconds().toByteArray()
        data is Boolean -> data.toByteArray()
        data is MessageSharingMode -> data.id.encodeToByteArray()
        data is DoubleRatchetUUID -> data.toByteArray()
        else -> throw OSCryptoError(
            OSCryptoError.Code.MISSING_MAPPER,
            "No mapper found or provided for type ${data::class.simpleName}",
        )
    }

    operator fun <Data : Any> invoke(
        mapBlock: (ByteArray.() -> Data)?,
        rawData: ByteArray,
        clazz: KClass<out Data>,
    ): Data {
        @Suppress("UNCHECKED_CAST")
        return when {
            mapBlock != null -> rawData.mapBlock()
            clazz == String::class -> rawData.decodeToString() as Data
            clazz == Int::class -> rawData.toInt() as Data
            clazz == SafeItemFieldKind::class -> rawData.toSafeItemFieldKind() as Data
            clazz == UUID::class -> rawData.toUUID() as Data
            clazz == ByteArray::class -> rawData as Data
            clazz == Instant::class -> Instant.ofEpochMilli(rawData.toLong()) as Data
            clazz == kotlinx.datetime.Instant::class -> Instant.ofEpochMilli(rawData.toLong()).toKotlinInstant() as Data
            clazz == Boolean::class -> rawData.toBoolean() as Data
            clazz == MessageSharingMode::class -> rawData.toMessageSharingMode() as Data
            clazz == DoubleRatchetUUID::class -> rawData.toDoubleRatchetUUID() as Data
            else -> throw OSCryptoError(
                OSCryptoError.Code.MISSING_MAPPER,
                "No mapper found for type ${clazz.simpleName}",
            )
        }
    }
}
