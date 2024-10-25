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
 * Created by Lunabee Studio / Date - 6/19/2024 - for the oneSafe6 SDK.
 * Last modified 6/19/24, 11:06 AM
 */

package studio.lunabee.onesafe.repository.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.messaging.domain.repository.MessagingSettingsRepository
import studio.lunabee.onesafe.domain.common.CtaState
import studio.lunabee.onesafe.domain.model.camera.CameraSystem
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.model.safeitem.ItemLayout
import studio.lunabee.onesafe.domain.model.safeitem.ItemOrder
import studio.lunabee.onesafe.domain.model.verifypassword.VerifyPasswordInterval
import studio.lunabee.onesafe.domain.repository.AppVisitRepository
import studio.lunabee.onesafe.domain.repository.ItemSettingsRepository
import studio.lunabee.onesafe.domain.repository.SafeSettingsRepository
import studio.lunabee.onesafe.domain.repository.SecuritySettingsRepository
import studio.lunabee.onesafe.repository.datasource.GlobalSettingsLocalDataSource
import studio.lunabee.onesafe.repository.datasource.SafeSettingsLocalDataSource
import java.time.Instant
import javax.inject.Inject
import kotlin.time.Duration

class SettingsRepository @Inject constructor(
    private val safeDataSource: SafeSettingsLocalDataSource,
    private val globalDataSource: GlobalSettingsLocalDataSource,
) : SafeSettingsRepository, SecuritySettingsRepository, ItemSettingsRepository, AppVisitRepository, MessagingSettingsRepository {

    override fun materialYou(safeId: SafeId): Flow<Boolean> =
        safeDataSource.materialYou(safeId).filterNotNull()

    override fun automationFlow(safeId: SafeId): Flow<Boolean> =
        safeDataSource.automationFlow(safeId).filterNotNull()

    override fun displayShareWarningFlow(safeId: SafeId): Flow<Boolean> =
        safeDataSource.displayShareWarningFlow(safeId).filterNotNull()

    override fun allowScreenshotFlow(safeId: SafeId): Flow<Boolean> =
        safeDataSource.allowScreenshotFlow(safeId).filterNotNull()

    override fun shakeToLockFlow(safeId: SafeId): Flow<Boolean> =
        safeDataSource.shakeToLockFlow(safeId).filterNotNull()

    override fun bubblesPreview(safeId: SafeId): Flow<Boolean> =
        safeDataSource.bubblesPreview(safeId).filterNotNull()

    override fun bubblesHomeCardCtaState(safeId: SafeId): Flow<CtaState> =
        safeDataSource.bubblesHomeCardCtaState(safeId).filterNotNull()

    override fun independentSafeInfoCtaState(safeId: SafeId): Flow<CtaState> =
        safeDataSource.independentSafeInfoCtaState(safeId).filterNotNull()

    override fun preventionWarningCtaState(safeId: SafeId): Flow<CtaState?> =
        safeDataSource.preventionWarningCtaState(safeId)

    override fun cameraSystemFlow(safeId: SafeId): Flow<CameraSystem> =
        safeDataSource.cameraSystemFlow(safeId).filterNotNull()

    override fun autoLockInactivityDelayFlow(safeId: SafeId): Flow<Duration> =
        safeDataSource.autoLockInactivityDelayFlow(safeId).filterNotNull()

    override fun autoLockAppChangeDelayFlow(safeId: SafeId): Flow<Duration> =
        safeDataSource.autoLockAppChangeDelayFlow(safeId).filterNotNull()

    override fun clipboardDelayFlow(safeId: SafeId): Flow<Duration> =
        safeDataSource.clipboardDelayFlow(safeId).filterNotNull()

    override fun verifyPasswordIntervalFlow(safeId: SafeId): Flow<VerifyPasswordInterval> =
        safeDataSource.verifyPasswordIntervalFlow(safeId).filterNotNull()

    override fun bubblesResendMessageDelayFlow(safeId: SafeId): Flow<Duration> =
        safeDataSource.bubblesResendMessageDelayFlow(safeId).filterNotNull()

    override fun autoLockOSKInactivityDelayFlow(safeId: SafeId): Flow<Duration> =
        safeDataSource.autoLockOSKInactivityDelayFlow(safeId).filterNotNull()

    override fun autoLockOSKHiddenDelayFlow(safeId: SafeId): Flow<Duration> =
        safeDataSource.autoLockOSKHiddenDelayFlow(safeId).filterNotNull()

    override fun itemOrdering(safeId: SafeId): Flow<ItemOrder> =
        safeDataSource.itemOrdering(safeId).filterNotNull()

    override fun itemLayout(safeId: SafeId): Flow<ItemLayout> =
        safeDataSource.itemLayout(safeId).filterNotNull()

    override fun hasBackupSince(safeId: SafeId, duration: Duration): Flow<Boolean> =
        safeDataSource.hasBackupSince(safeId = safeId, duration = duration)

    override suspend fun cameraSystem(safeId: SafeId): CameraSystem =
        safeDataSource.getCameraSystem(safeId)

    override suspend fun allowScreenshot(safeId: SafeId): Boolean =
        safeDataSource.allowScreenshot(safeId)

    override suspend fun automation(safeId: SafeId): Boolean {
        return safeDataSource.automation(safeId)
    }

    override suspend fun setBubblesPreview(safeId: SafeId, value: Boolean): Unit =
        safeDataSource.setBubblesPreview(safeId, value)

    override suspend fun setCameraSystem(safeId: SafeId, value: CameraSystem): Unit =
        safeDataSource.setCameraSystem(safeId, value)

    override suspend fun setBubblesHomeCardCtaState(safeId: SafeId, ctaState: CtaState): Unit =
        safeDataSource.setBubblesHomeCardCtaState(safeId, ctaState)

    override suspend fun displayShareWarning(safeId: SafeId): Boolean =
        safeDataSource.displayShareWarning(safeId)

    override suspend fun shakeToLock(safeId: SafeId): Boolean =
        safeDataSource.shakeToLock(safeId)

    override suspend fun setIndependentSafeInfoCtaState(safeId: SafeId, ctaState: CtaState): Unit =
        safeDataSource.setIndependentSafeInfoCtaState(safeId, ctaState)

    override suspend fun toggleAllowScreenshot(safeId: SafeId): Unit =
        safeDataSource.toggleAllowScreenshot(safeId)

    override suspend fun toggleShakeToLock(safeId: SafeId): Unit =
        safeDataSource.toggleShakeToLock(safeId)

    override suspend fun toggleAutomation(safeId: SafeId): Unit =
        safeDataSource.toggleAutomation(safeId)

    override suspend fun disableShareWarningDisplay(safeId: SafeId): Unit =
        safeDataSource.disableShareWarningDisplay(safeId)

    override suspend fun toggleMaterialYou(safeId: SafeId): Unit =
        safeDataSource.toggleMaterialYou(safeId)

    override suspend fun autoLockInactivityDelay(safeId: SafeId): Duration =
        safeDataSource.autoLockInactivityDelay(safeId)

    override suspend fun setAutoLockInactivityDelay(safeId: SafeId, delay: Duration) {
        safeDataSource.setAutoLockInactivityDelay(safeId, delay)
    }

    override suspend fun autoLockAppChangeDelay(safeId: SafeId): Duration =
        safeDataSource.autoLockAppChangeDelay(safeId)

    override suspend fun setAutoLockAppChangeDelay(safeId: SafeId, delay: Duration) {
        safeDataSource.setAutoLockAppChangeDelay(safeId, delay)
    }

    override suspend fun clipboardClearDelay(safeId: SafeId): Duration =
        safeDataSource.clipboardDelay(safeId)

    override suspend fun setClipboardClearDelay(safeId: SafeId, delay: Duration) {
        safeDataSource.setClipboardClearDelay(safeId, delay)
    }

    override suspend fun verifyPasswordInterval(safeId: SafeId): VerifyPasswordInterval =
        safeDataSource.verifyPasswordInterval(safeId)

    override suspend fun setVerifyPasswordInterval(safeId: SafeId, passwordInterval: VerifyPasswordInterval) {
        safeDataSource.setVerifyPasswordInterval(safeId, passwordInterval)
    }

    override suspend fun lastPasswordVerificationInstant(safeId: SafeId): Instant =
        safeDataSource.lastPasswordVerification(safeId)

    override suspend fun setLastPasswordVerification(safeId: SafeId, instant: Instant) {
        safeDataSource.setLastPasswordVerification(safeId, instant)
    }

    override suspend fun setBubblesResendMessageDelay(safeId: SafeId, delay: Duration) {
        safeDataSource.setBubblesResendMessageDelay(safeId, delay)
    }

    override suspend fun autoLockOSKInactivityDelay(safeId: SafeId): Duration =
        safeDataSource.autoLockOSKInactivityDelay(safeId)

    override suspend fun setAutoLockOSKInactivityDelay(safeId: SafeId, delay: Duration) {
        safeDataSource.setAutoLockOSKInactivityDelay(safeId, delay)
    }

    override suspend fun autoLockOSKHiddenDelay(safeId: SafeId): Duration =
        safeDataSource.autoLockOSKHiddenDelay(safeId)

    override suspend fun setAutoLockOSKHiddenDelay(safeId: SafeId, delay: Duration) {
        safeDataSource.setAutoLockOSKHiddenDelay(safeId, delay)
    }

    override suspend fun setItemOrdering(safeId: SafeId, order: ItemOrder) {
        safeDataSource.setItemOrdering(safeId, order)
    }

    override suspend fun setItemsLayout(safeId: SafeId, style: ItemLayout) {
        safeDataSource.setItemLayout(safeId, style)
    }

    override fun hasVisitedLogin(): Flow<Boolean> {
        return globalDataSource.hasVisitedLogin()
    }

    override fun hasDoneTutorialOpenOsk(): Flow<Boolean> {
        return globalDataSource.hasDoneTutorialOpenOsk()
    }

    override fun hasDoneTutorialLockOsk(): Flow<Boolean> {
        return globalDataSource.hasDoneTutorialLockOsk()
    }

    override fun hasFinishOneSafeKOnBoardingFlow(safeId: SafeId): Flow<Boolean> {
        return safeDataSource.hasFinishOneSafeKOnBoardingFlow(safeId).filterNotNull()
    }

    override fun hasDoneOnBoardingBubblesFlow(safeId: SafeId): Flow<Boolean> {
        return safeDataSource.hasDoneOnBoardingBubblesFlow(safeId).filterNotNull()
    }

    override fun hasHiddenCameraTipsFlow(safeId: SafeId): Flow<Boolean> {
        return safeDataSource.hasHiddenCameraTipsFlow(safeId).filterNotNull()
    }

    override fun hasSeenItemEditionUrlToolTipFlow(safeId: SafeId): Flow<Boolean> {
        return safeDataSource.hasSeenItemEditionUrlToolTipFlow(safeId).filterNotNull()
    }

    override fun hasSeenItemEditionEmojiToolTipFlow(safeId: SafeId): Flow<Boolean> {
        return safeDataSource.hasSeenItemEditionEmojiToolTipFlow(safeId).filterNotNull()
    }

    override fun hasSeenItemReadEditToolTipFlow(safeId: SafeId): Flow<Boolean> {
        return safeDataSource.hasSeenItemReadEditToolTipFlow(safeId).filterNotNull()
    }

    override suspend fun hasSeenDialogMessageSaveConfirmation(safeId: SafeId): Boolean {
        return safeDataSource.hasSeenDialogMessageSaveConfirmation(safeId)
    }

    override suspend fun hasSeenItemEditionUrlToolTip(safeId: SafeId): Boolean {
        return safeDataSource.hasSeenItemEditionUrlToolTip(safeId)
    }

    override suspend fun hasSeenItemEditionEmojiToolTip(safeId: SafeId): Boolean {
        return safeDataSource.hasSeenItemEditionEmojiToolTip(safeId)
    }

    override suspend fun setHasSeenDialogMessageSaveConfirmation(safeId: SafeId) {
        safeDataSource.setHasSeenDialogMessageSaveConfirmation(safeId)
    }

    override suspend fun setHasVisitedLogin(value: Boolean) {
        return globalDataSource.setHasVisitedLogin(value)
    }

    override suspend fun setHasDoneTutorialOpenOsk(value: Boolean) {
        return globalDataSource.setHasDoneTutorialOpenOsk(value)
    }

    override suspend fun setHasDoneTutorialLockOsk(value: Boolean) {
        return globalDataSource.setHasDoneTutorialLockOsk(value)
    }

    override suspend fun setHasFinishOneSafeKOnBoarding(safeId: SafeId, value: Boolean) {
        return safeDataSource.setHasFinishOneSafeKOnBoarding(safeId, value)
    }

    override suspend fun setHasDoneOnBoardingBubbles(safeId: SafeId, value: Boolean) {
        return safeDataSource.setHasDoneOnBoardingBubbles(safeId, value)
    }

    override suspend fun setHasHiddenCameraTips(safeId: SafeId, value: Boolean) {
        return safeDataSource.setHasHiddenCameraTips(safeId, value)
    }

    override suspend fun setHasSeenItemEditionUrlToolTip(safeId: SafeId, value: Boolean) {
        return safeDataSource.setHasSeenItemEditionUrlToolTip(safeId, value)
    }

    override suspend fun setHasSeenItemEditionEmojiToolTip(safeId: SafeId, value: Boolean) {
        return safeDataSource.setHasSeenItemEditionEmojiToolTip(safeId, value)
    }

    override suspend fun setHasSeenItemReadEditToolTip(safeId: SafeId, value: Boolean) {
        return safeDataSource.setHasSeenItemReadEditToolTip(safeId, value)
    }

    override suspend fun bubblesResendMessageDelayInMillis(safeId: DoubleRatchetUUID): Long {
        return safeDataSource.bubblesResendMessageDelay(SafeId(safeId.uuid)).inWholeMilliseconds
    }

    override suspend fun setPreventionWarningCtaState(safeId: SafeId, ctaState: CtaState) {
        safeDataSource.setPreventionWarningCtaState(safeId, ctaState)
    }
}
