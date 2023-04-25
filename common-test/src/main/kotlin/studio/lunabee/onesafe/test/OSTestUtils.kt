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

package studio.lunabee.onesafe.test

import studio.lunabee.onesafe.domain.model.safeitem.SafeItem
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemField
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemWithIdentifier
import studio.lunabee.onesafe.domain.repository.MainCryptoRepository
import java.nio.ByteBuffer
import java.time.Instant
import java.util.UUID
import kotlin.random.Random
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible

object OSTestUtils {
    fun createSafeItem(
        id: UUID = UUID.randomUUID(),
        encName: ByteArray? = byteArrayOf(),
        parentId: UUID? = null,
        isFavorite: Boolean = false,
        updatedAt: Instant = Instant.now(),
        position: Double = 0.0,
        iconId: UUID? = null,
        encColor: ByteArray? = byteArrayOf(),
        deletedAt: Instant? = null,
        deletedParentId: UUID? = null,
    ): SafeItem {
        return SafeItem(id, encName, parentId, isFavorite, updatedAt, position, iconId, encColor, deletedAt, deletedParentId)
    }

    fun createSafeItemWithIdentifier(
        id: UUID = UUID.randomUUID(),
        encName: ByteArray? = byteArrayOf(),
        iconId: UUID? = null,
        encColor: ByteArray? = byteArrayOf(),
        identifier: ByteArray? = byteArrayOf(),
        encIdentifierKind: ByteArray? = byteArrayOf(),
    ): SafeItemWithIdentifier {
        return SafeItemWithIdentifier(id, encName, iconId, encColor, identifier, null, null, encIdentifierKind)
    }

    fun createSafeItems(
        size: Int,
        id: (idx: Int) -> UUID = { UUID.randomUUID() },
        encName: (idx: Int) -> ByteArray? = { byteArrayOf() },
        parentId: (idx: Int) -> UUID? = { null },
        isFavorite: (idx: Int) -> Boolean = { false },
        updatedAt: (idx: Int) -> Instant = { Instant.now() },
        position: (idx: Int) -> Double = { 0.0 },
        iconId: (idx: Int) -> UUID? = { null },
        encColor: (idx: Int) -> ByteArray? = { byteArrayOf() },
        deletedAt: (idx: Int) -> Instant? = { null },
        deletedParentId: (idx: Int) -> UUID? = { null },
    ): List<SafeItem> {
        return (0 until size).map { idx ->
            SafeItem(
                id = id(idx),
                encName = encName(idx),
                parentId = parentId(idx),
                isFavorite = isFavorite(idx),
                updatedAt = updatedAt(idx),
                position = position(idx),
                iconId = iconId(idx),
                encColor = encColor(idx),
                deletedAt = deletedAt(idx),
                deletedParentId = deletedParentId(idx),
            )
        }
    }

    fun createSafeItemField(
        id: UUID = UUID.randomUUID(),
        encName: ByteArray? = byteArrayOf(),
        position: Double = 0.0,
        itemId: UUID = UUID.randomUUID(),
        encPlaceholder: ByteArray? = byteArrayOf(),
        encValue: ByteArray? = byteArrayOf(),
        showPrediction: Boolean = false,
        encKind: ByteArray? = byteArrayOf(),
        updatedAt: Instant = Instant.now(),
        isItemIdentifier: Boolean = false,
        formattingMask: ByteArray? = null,
        secureDisplayMask: ByteArray? = null,
        isSecured: Boolean = false,
    ): SafeItemField {
        return SafeItemField(
            id,
            encName,
            position,
            itemId,
            encPlaceholder,
            encValue,
            showPrediction,
            encKind,
            updatedAt,
            isItemIdentifier,
            formattingMask,
            secureDisplayMask,
            isSecured,
        )
    }

    fun unloadMasterKey(cryptoRepository: MainCryptoRepository) {
        val propertyMasterKey = (
            cryptoRepository::class.declaredMemberProperties.find { it.name == "masterKey" }
                .apply { this?.isAccessible = true } as KMutableProperty<*>
            )
        propertyMasterKey.setter.call(cryptoRepository, null)

        val propertySearchIndexKey = (
            cryptoRepository::class.declaredMemberProperties.find { it.name == "searchIndexKey" }
                .apply { this?.isAccessible = true } as KMutableProperty<*>
            )
        propertySearchIndexKey.setter.call(
            cryptoRepository,
            null,
        )
    }
}

/**
 * UUID generated in a reproducible way
 *
 * @see [UUID.randomUUID] for implementation details
 */
val testUUIDs: List<UUID> by lazy {
    val ng = Random(0)
    val randomBytes = ByteArray(16)
    val buffer = ByteBuffer.wrap(randomBytes)
    (0..999).map {
        ng.nextBytes(randomBytes)
        randomBytes[6] = (randomBytes[6].toInt() and 0x0f).toByte() /* clear version        */
        randomBytes[6] = (randomBytes[6].toInt() or 0x40).toByte() /* set to version 4     */
        randomBytes[8] = (randomBytes[8].toInt() and 0x3f).toByte() /* clear variant        */
        randomBytes[8] = (randomBytes[8].toInt() or 0x80).toByte() /* set to IETF variant  */
        val firstLong = buffer.long
        val secondLong = buffer.long
        buffer.rewind()
        UUID(firstLong, secondLong)
    }
}
