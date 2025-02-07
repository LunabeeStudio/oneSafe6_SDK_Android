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
import studio.lunabee.onesafe.domain.common.CtaState
import studio.lunabee.onesafe.domain.common.FeatureFlags
import studio.lunabee.onesafe.domain.model.camera.CameraSystem
import studio.lunabee.onesafe.domain.model.safe.AppVisit
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.model.safe.SafeSettings
import studio.lunabee.onesafe.domain.model.safeitem.ItemLayout
import studio.lunabee.onesafe.domain.model.safeitem.ItemOrder
import studio.lunabee.onesafe.domain.model.safeitem.SafeItem
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemField
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemWithIdentifier
import studio.lunabee.onesafe.domain.model.verifypassword.VerifyPasswordInterval
import studio.lunabee.onesafe.domain.usecase.item.CreateItemUseCase
import studio.lunabee.onesafe.importexport.model.GoogleDriveSettings
import java.nio.ByteBuffer
import java.time.Instant
import java.util.UUID
import kotlin.random.Random
import kotlin.time.Duration

object OSTestUtils {
    fun createSafeItem(
        id: UUID = UUID.randomUUID(),
        encName: ByteArray? = byteArrayOf(),
        parentId: UUID? = null,
        isFavorite: Boolean = false,
        updatedAt: Instant = Instant.now(OSTestConfig.clock),
        position: Double = 0.0,
        iconId: UUID? = null,
        encColor: ByteArray? = byteArrayOf(),
        deletedAt: Instant? = null,
        deletedParentId: UUID? = null,
        indexAlpha: Double = 0.0,
        createdAt: Instant = Instant.now(OSTestConfig.clock),
        safeId: SafeId = firstSafeId,
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
            safeId = safeId,
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
        updatedAt: (idx: Int) -> Instant = { Instant.now(OSTestConfig.clock) },
        position: (idx: Int) -> Double = { 0.0 },
        iconId: (idx: Int) -> UUID? = { null },
        encColor: (idx: Int) -> ByteArray? = { byteArrayOf() },
        deletedAt: (idx: Int) -> Instant? = { null },
        deletedParentId: (idx: Int) -> UUID? = { null },
        indexAlpha: (idx: Int) -> Double = { 0.0 },
        createdAt: (idx: Int) -> Instant = { Instant.now(OSTestConfig.clock) },
        safeId: (idx: Int) -> SafeId = { firstSafeId },
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
                safeId = safeId(idx),
            )
        }
    }

    fun createSafeItemField(
        id: UUID = UUID.randomUUID(),
        encName: ByteArray? = byteArrayOf(),
        position: Double = 0.0,
        itemId: UUID = UUID.randomUUID(),
        encPlaceholder: ByteArray? = byteArrayOf(),
        encThumbnailFileName: ByteArray? = byteArrayOf(),
        encValue: ByteArray? = byteArrayOf(),
        showPrediction: Boolean = false,
        encKind: ByteArray? = byteArrayOf(),
        updatedAt: Instant = Instant.now(OSTestConfig.clock),
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
            encThumbnailFileName,
            isSecured,
        )
    }

    fun featureFlags(
        florisBoard: Boolean = false,
        accessibilityService: Boolean = false,
        oneSafeK: Boolean = false,
        bubbles: Boolean = true,
        quickSignIn: Boolean = false,
        cloudBackup: Boolean = false,
        backupForegroundService: Boolean = false,
        sqlcipher: Boolean = true,
    ): FeatureFlags {
        return object : FeatureFlags {
            override fun florisBoard(): Boolean = florisBoard
            override fun accessibilityService(): Boolean = accessibilityService
            override fun oneSafeK(): Boolean = oneSafeK
            override fun bubbles(): Boolean = bubbles
            override fun quickSignIn(): Boolean = quickSignIn
            override fun cloudBackup(): Boolean = cloudBackup
            override fun backupWorkerExpedited(): Boolean = backupForegroundService
            override fun sqlcipher(): Boolean = sqlcipher
        }
    }

    fun safeSettings(
        version: Int = 0,
        materialYou: Boolean = false,
        automation: Boolean = false,
        displayShareWarning: Boolean = false,
        allowScreenshot: Boolean = false,
        shakeToLock: Boolean = false,
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
        preventionWarningCtaState: CtaState = CtaState.Hidden,
    ): SafeSettings = SafeSettings(
        version = version,
        materialYou = materialYou,
        automation = automation,
        displayShareWarning = displayShareWarning,
        allowScreenshot = allowScreenshot,
        shakeToLock = shakeToLock,
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
        preventionWarningCtaState = preventionWarningCtaState,
        lastExportDate = null,
    )

    fun driveSettings(
        selectedAccount: String? = "dummy@test.com",
        folderId: String? = "1234",
        folderUrl: String? = "https://www.google.com/drive/folders/1234",
    ): GoogleDriveSettings = GoogleDriveSettings(
        selectedAccount = selectedAccount,
        folderId = folderId,
        folderUrl = folderUrl,
    )

    fun appVisit(
        hasFinishOneSafeKOnBoarding: Boolean = false,
        hasDoneOnBoardingBubbles: Boolean = false,
        hasDoneTutorialOpenOsk: Boolean = false,
        hasDoneTutorialLockOsk: Boolean = false,
        hasHiddenCameraTips: Boolean = false,
        hasSeenItemEditionUrlToolTip: Boolean = false,
        hasSeenItemEditionEmojiToolTip: Boolean = false,
        hasSeenItemReadEditToolTip: Boolean = false,
        hasSeenDialogMessageSaveConfirmation: Boolean = false,
    ): AppVisit = AppVisit(
        hasFinishOneSafeKOnBoarding = hasFinishOneSafeKOnBoarding,
        hasDoneOnBoardingBubbles = hasDoneOnBoardingBubbles,
        hasDoneTutorialOpenOsk = hasDoneTutorialOpenOsk,
        hasDoneTutorialLockOsk = hasDoneTutorialLockOsk,
        hasHiddenCameraTips = hasHiddenCameraTips,
        hasSeenItemEditionUrlToolTip = hasSeenItemEditionUrlToolTip,
        hasSeenItemEditionEmojiToolTip = hasSeenItemEditionEmojiToolTip,
        hasSeenItemReadEditToolTip = hasSeenItemReadEditToolTip,
        hasSeenDialogMessageSaveConfirmation = hasSeenDialogMessageSaveConfirmation,
    )
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
        OSTestConfig.random.nextBytes(randomBytes)
        buildUUID(randomBytes, buffer)
    }
}

fun buildUUID(randomBytes: ByteArray, buffer: ByteBuffer): UUID {
    randomBytes[6] = (randomBytes[6].toInt() and 0x0f).toByte() // clear version
    randomBytes[6] = (randomBytes[6].toInt() or 0x40).toByte() // set to version 4
    randomBytes[8] = (randomBytes[8].toInt() and 0x3f).toByte() // clear variant
    randomBytes[8] = (randomBytes[8].toInt() or 0x80).toByte() // set to IETF variant
    val firstLong = buffer.long
    val secondLong = buffer.long
    buffer.rewind()
    return UUID(firstLong, secondLong)
}

val firstSafeId: SafeId by lazy { SafeId(testUUIDs[0]) }

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

val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
fun Random.nextString(length: Int = 10): String {
    return buildString(length) { append(charPool.random(this@nextString)) }
}
