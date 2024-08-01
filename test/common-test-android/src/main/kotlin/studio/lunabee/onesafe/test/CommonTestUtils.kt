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
 * Created by Lunabee Studio / Date - 1/29/2024 - for the oneSafe6 SDK.
 * Last modified 1/29/24, 8:53 AM
 */

package studio.lunabee.onesafe.test

import studio.lunabee.onesafe.domain.model.camera.CameraSystem
import studio.lunabee.onesafe.domain.model.safe.BiometricCryptoMaterial
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.model.safeitem.ItemFieldData
import studio.lunabee.onesafe.domain.model.safeitem.ItemLayout
import studio.lunabee.onesafe.domain.model.safeitem.ItemOrder
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.domain.model.verifypassword.VerifyPasswordInterval
import studio.lunabee.onesafe.storage.model.RoomAppVisit
import studio.lunabee.onesafe.storage.model.RoomCtaState
import studio.lunabee.onesafe.storage.model.RoomDriveSettings
import studio.lunabee.onesafe.storage.model.RoomSafe
import studio.lunabee.onesafe.storage.model.RoomSafeCrypto
import studio.lunabee.onesafe.storage.model.RoomSafeSettings
import java.time.Instant
import java.util.UUID
import kotlin.random.Random
import kotlin.time.Duration

object CommonTestUtils {
    fun createItemFieldData(
        id: UUID = UUID.randomUUID(),
        name: String? = UUID.randomUUID().toString(),
        kind: SafeItemFieldKind? = SafeItemFieldKind.Text,
        position: Double = Random.nextDouble(),
        placeholder: String? = UUID.randomUUID().toString(),
        value: String? = UUID.randomUUID().toString(),
        showPrediction: Boolean = Random.nextBoolean(),
        isItemIdentifier: Boolean = false,
        formattingMask: String? = null,
        secureDisplayMask: String? = null,
        isSecured: Boolean = false,
    ): ItemFieldData = ItemFieldData(
        id = id,
        name = name,
        position = position,
        placeholder = placeholder,
        value = value,
        kind = kind,
        showPrediction = showPrediction,
        isItemIdentifier = isItemIdentifier,
        formattingMask = formattingMask,
        secureDisplayMask = secureDisplayMask,
        isSecured = isSecured,
    )

    fun roomAppVisit(
        hasFinishOneSafeKOnBoarding: Boolean = false,
        hasDoneOnBoardingBubbles: Boolean = false,
        hasHiddenCameraTips: Boolean = false,
        hasSeenItemEditionUrlToolTip: Boolean = false,
        hasSeenItemEditionEmojiToolTip: Boolean = false,
        hasSeenItemReadEditToolTip: Boolean = false,
    ): RoomAppVisit = RoomAppVisit(
        hasFinishOneSafeKOnBoarding = hasFinishOneSafeKOnBoarding,
        hasDoneOnBoardingBubbles = hasDoneOnBoardingBubbles,
        hasHiddenCameraTips = hasHiddenCameraTips,
        hasSeenItemEditionUrlToolTip = hasSeenItemEditionUrlToolTip,
        hasSeenItemEditionEmojiToolTip = hasSeenItemEditionEmojiToolTip,
        hasSeenItemReadEditToolTip = hasSeenItemReadEditToolTip,
    )

    fun roomSafe(
        id: SafeId = firstSafeId,
    ): RoomSafe {
        return RoomSafe(
            id = id,
            crypto = RoomSafeCrypto(
                salt = byteArrayOf(),
                encTest = byteArrayOf(),
                encIndexKey = byteArrayOf(),
                encBubblesKey = null,
                encItemEditionKey = byteArrayOf(),
                biometricCryptoMaterial = BiometricCryptoMaterial(
                    iv = OSTestConfig.random.nextBytes(16),
                    key = OSTestConfig.random.nextBytes(48),
                ),
            ),
            settings = RoomSafeSettings(
                materialYou = false,
                automation = false,
                displayShareWarning = false,
                allowScreenshot = false,
                shakeToLock = false,
                bubblesPreview = false,
                cameraSystem = CameraSystem.InApp,
                autoLockOSKHiddenDelay = Duration.ZERO,
                verifyPasswordInterval = VerifyPasswordInterval.NEVER,
                lastPasswordVerification = Instant.EPOCH,
                bubblesHomeCardCtaState = RoomCtaState(
                    state = RoomCtaState.State.Hidden,
                    timestamp = null,
                ),
                autoLockInactivityDelay = Duration.ZERO,
                autoLockAppChangeDelay = Duration.ZERO,
                clipboardDelay = Duration.ZERO,
                bubblesResendMessageDelay = Duration.ZERO,
                autoLockOSKInactivityDelay = Duration.ZERO,
                autoBackupEnabled = false,
                autoBackupFrequency = Duration.ZERO,
                autoBackupMaxNumber = 0,
                cloudBackupEnabled = false,
                keepLocalBackupEnabled = false,
                itemOrdering = ItemOrder.Position,
                itemLayout = ItemLayout.Grid,
                driveSettings = RoomDriveSettings(
                    selectedAccount = null,
                    folderId = null,
                    folderUrl = null,
                ),
                enableAutoBackupCtaState = RoomCtaState(
                    state = RoomCtaState.State.Hidden,
                    timestamp = null,
                ),
                independentSafeInfoCtaState = RoomCtaState(
                    state = RoomCtaState.State.Hidden,
                    timestamp = null,
                ),
            ),
            appVisit = RoomAppVisit(
                hasFinishOneSafeKOnBoarding = false,
                hasDoneOnBoardingBubbles = false,
                hasHiddenCameraTips = false,
                hasSeenItemEditionUrlToolTip = false,
                hasSeenItemEditionEmojiToolTip = false,
                hasSeenItemReadEditToolTip = false,
            ),
            version = 0,
        )
    }
}
