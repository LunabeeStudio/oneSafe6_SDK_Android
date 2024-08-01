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

package studio.lunabee.onesafe.storage

import studio.lunabee.onesafe.domain.common.CtaState
import studio.lunabee.onesafe.domain.model.camera.CameraSystem
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.model.safeitem.ItemLayout
import studio.lunabee.onesafe.domain.model.safeitem.ItemOrder
import studio.lunabee.onesafe.domain.model.verifypassword.VerifyPasswordInterval
import studio.lunabee.onesafe.storage.migration.RoomMigration12to13
import studio.lunabee.onesafe.storage.model.RoomSafeItem
import studio.lunabee.onesafe.storage.model.RoomUpdateSafeItem
import studio.lunabee.onesafe.test.OSTestConfig
import studio.lunabee.onesafe.test.firstSafeId
import java.time.Instant
import java.util.UUID
import kotlin.random.Random
import kotlin.time.Duration

internal object OSStorageTestUtils {
    fun createRoomSafeItem(
        id: UUID = UUID.randomUUID(),
        encName: ByteArray = Random.nextBytes(0),
        parentId: UUID? = null,
        isFavorite: Boolean = false,
        updatedAt: Instant = Instant.ofEpochMilli(0),
        position: Double = 0.0,
        iconId: UUID = UUID.randomUUID(),
        encColor: ByteArray = Random.nextBytes(0),
        deletedAt: Instant? = null,
        deletedParentId: UUID? = null,
        consultedAt: Instant? = null,
        indexAlpha: Double = 0.0,
        createdAt: Instant = Instant.ofEpochMilli(0),
        safeId: SafeId = firstSafeId,
    ): RoomSafeItem {
        return RoomSafeItem(
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
            consultedAt = consultedAt,
            indexAlpha = indexAlpha,
            createdAt = createdAt,
            safeId = safeId,
        )
    }

    fun safeSettingsMigration(
        version: Int = 0,
        materialYou: Boolean = false,
        automation: Boolean = false,
        displayShareWarning: Boolean = false,
        allowScreenshot: Boolean = false,
        bubblesPreview: Boolean = false,
        cameraSystem: CameraSystem = CameraSystem.InApp,
        autoLockOSKHiddenDelay: Duration = Duration.ZERO,
        verifyPasswordInterval: VerifyPasswordInterval = VerifyPasswordInterval.NEVER,
        bubblesHomeCardCtaState: CtaState = CtaState.Hidden,
        autoLockInactivityDelay: Duration = Duration.ZERO,
        autoLockAppChangeDelay: Duration = Duration.ZERO,
        clipboardDelay: Duration = Duration.ZERO,
        bubblesResendMessageDelay: Duration = Duration.ZERO,
        autoLockOSKInactivityDelay: Duration = Duration.ZERO,
        autoBackupEnabled: Boolean = false,
        autoBackupFrequency: Duration = Duration.ZERO,
        autoBackupMaxNumber: Int = 0,
        cloudBackupEnabled: Boolean = false,
        keepLocalBackupEnabled: Boolean = false,
        itemOrdering: ItemOrder = ItemOrder.Position,
        itemLayout: ItemLayout = ItemLayout.Grid,
        enableAutoBackupCtaState: CtaState = CtaState.Hidden,
        lastPasswordVerification: Instant = Instant.now(OSTestConfig.clock),
        independentSafeInfoCtaState: CtaState = CtaState.Hidden,
    ): RoomMigration12to13.SafeSettingsMigration = RoomMigration12to13.SafeSettingsMigration(
        version = version,
        materialYou = materialYou,
        automation = automation,
        displayShareWarning = displayShareWarning,
        allowScreenshot = allowScreenshot,
        bubblesPreview = bubblesPreview,
        cameraSystem = cameraSystem,
        autoLockOSKHiddenDelay = autoLockOSKHiddenDelay,
        verifyPasswordInterval = verifyPasswordInterval,
        bubblesHomeCardCtaState = bubblesHomeCardCtaState,
        autoLockInactivityDelay = autoLockInactivityDelay,
        autoLockAppChangeDelay = autoLockAppChangeDelay,
        clipboardDelay = clipboardDelay,
        bubblesResendMessageDelay = bubblesResendMessageDelay,
        autoLockOSKInactivityDelay = autoLockOSKInactivityDelay,
        autoBackupEnabled = autoBackupEnabled,
        autoBackupFrequency = autoBackupFrequency,
        autoBackupMaxNumber = autoBackupMaxNumber,
        cloudBackupEnabled = cloudBackupEnabled,
        keepLocalBackupEnabled = keepLocalBackupEnabled,
        itemOrdering = itemOrdering,
        itemLayout = itemLayout,
        enableAutoBackupCtaState = enableAutoBackupCtaState,
        lastPasswordVerification = lastPasswordVerification,
        independentSafeInfoCtaState = independentSafeInfoCtaState,
    )
}

fun RoomSafeItem.toRoomUpdateSafeItem(): RoomUpdateSafeItem =
    RoomUpdateSafeItem(
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
    )
