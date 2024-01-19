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

import com.lunabee.lbcore.model.LBResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import studio.lunabee.onesafe.domain.common.FeatureFlags
import studio.lunabee.onesafe.domain.model.safeitem.ItemsLayoutSettings
import studio.lunabee.onesafe.domain.model.safeitem.SafeItem
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemField
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemWithIdentifier
import studio.lunabee.onesafe.domain.usecase.item.CreateItemUseCase
import java.nio.ByteBuffer
import java.time.Instant
import java.util.UUID
import kotlin.random.Random

object OSTestUtils {

    // ⚠️ Replaced by CI, see ci/set_test_seed.py
    private val seed = Random.nextInt().also {
        println("Random seed = $it")
    }
    val random: Random = Random(seed)

    val itemsLayoutSettings: ItemsLayoutSettings = ItemsLayoutSettings.entries[Math.floorMod(seed, ItemsLayoutSettings.entries.size)].also {
        println("${ItemsLayoutSettings::class.simpleName} = $it")
    }

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
        indexAlpha: Double = 0.0,
        createdAt: Instant = Instant.now(),
    ): SafeItem {
        return SafeItem(
            id = id,
            encName = encName,
            parentId = parentId,
            isFavorite = isFavorite,
            updatedAt = updatedAt,
            position = position,
            iconId = iconId,
            encColor = encColor,
            deletedAt = deletedAt,
            deletedParentId = deletedParentId,
            indexAlpha = indexAlpha,
            createdAt = createdAt,
        )
    }

    fun createSafeItemWithIdentifier(
        id: UUID = UUID.randomUUID(),
        encName: ByteArray? = byteArrayOf(),
        iconId: UUID? = null,
        encColor: ByteArray? = byteArrayOf(),
        identifier: ByteArray? = byteArrayOf(),
        encIdentifierKind: ByteArray? = byteArrayOf(),
        position: Double = 0.0,
        updatedAt: Instant = Instant.EPOCH,
    ): SafeItemWithIdentifier {
        return SafeItemWithIdentifier(
            id = id,
            encName = encName,
            iconId = iconId,
            encColor = encColor,
            encIdentifier = identifier,
            deletedAt = null,
            encSecuredDisplayMask = null,
            encIdentifierKind = encIdentifierKind,
            position = position,
            updatedAt = updatedAt,
        )
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
        indexAlpha: (idx: Int) -> Double = { 0.0 },
        createdAt: (idx: Int) -> Instant = { Instant.now() },
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
                indexAlpha = indexAlpha(idx),
                createdAt = createdAt(idx),
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

    fun featureFlags(
        florisBoard: Boolean = false,
        accessibilityService: Boolean = false,
        oneSafeK: Boolean = false,
        bubbles: Flow<Boolean> = flowOf(true),
        quickSignIn: Boolean = false,
        cloudBackup: Boolean = false,
        backupForegroundService: Boolean = false,
    ): FeatureFlags {
        return object : FeatureFlags {
            override fun florisBoard(): Boolean = florisBoard
            override fun accessibilityService(): Boolean = accessibilityService
            override fun oneSafeK(): Boolean = oneSafeK
            override fun bubbles(): Flow<Boolean> = bubbles
            override fun quickSignIn(): Boolean = quickSignIn
            override fun cloudBackup(): Boolean = cloudBackup
            override fun backupWorkerExpedited(): Boolean = backupForegroundService
        }
    }
}

/**
 * UUID generated in a reproducible way
 *
 * @see [UUID.randomUUID] for implementation details
 */
val testUUIDs: List<UUID> by lazy {
    val randomBytes = ByteArray(16)
    val buffer = ByteBuffer.wrap(randomBytes)
    (0..999).map {
        OSTestUtils.random.nextBytes(randomBytes)
        randomBytes[6] = (randomBytes[6].toInt() and 0x0f).toByte() // clear version
        randomBytes[6] = (randomBytes[6].toInt() or 0x40).toByte() // set to version 4
        randomBytes[8] = (randomBytes[8].toInt() and 0x3f).toByte() // clear variant
        randomBytes[8] = (randomBytes[8].toInt() or 0x80).toByte() // set to IETF variant
        val firstLong = buffer.long
        val secondLong = buffer.long
        buffer.rewind()
        UUID(firstLong, secondLong)
    }
}

suspend fun CreateItemUseCase.test(
    name: String? = null,
    parentId: UUID? = null,
    isFavorite: Boolean = false,
    icon: ByteArray? = null,
    color: String? = null,
    position: Double? = null,
): SafeItem {
    val result = this(name, parentId, isFavorite, icon, color, position)
    return when (result) {
        is LBResult.Failure -> throw result.throwable!!
        is LBResult.Success -> result.successData
    }
}
