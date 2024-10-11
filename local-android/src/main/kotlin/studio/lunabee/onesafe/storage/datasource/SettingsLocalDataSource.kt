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
 * Created by Lunabee Studio / Date - 6/19/2024 - for the oneSafe6 SDK.
 * Last modified 6/19/24, 4:08 PM
 */

package studio.lunabee.onesafe.storage.datasource

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import studio.lunabee.importexport.datasource.AutoBackupSettingsDataSource
import studio.lunabee.onesafe.domain.common.CtaState
import studio.lunabee.onesafe.domain.model.camera.CameraSystem
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.model.safeitem.ItemLayout
import studio.lunabee.onesafe.domain.model.safeitem.ItemOrder
import studio.lunabee.onesafe.domain.model.verifypassword.VerifyPasswordInterval
import studio.lunabee.onesafe.importexport.data.GoogleDriveEnginePreferencesDatasource
import studio.lunabee.onesafe.importexport.model.SafeAutoBackupEnabled
import studio.lunabee.onesafe.repository.datasource.SafeSettingsLocalDataSource
import studio.lunabee.onesafe.storage.dao.SettingsDao
import studio.lunabee.onesafe.storage.model.RoomCtaState
import java.time.Instant
import javax.inject.Inject
import kotlin.time.Duration

class SettingsLocalDataSource @Inject constructor(
    private val dao: SettingsDao,
) : SafeSettingsLocalDataSource, AutoBackupSettingsDataSource, GoogleDriveEnginePreferencesDatasource {
    override fun autoLockOSKHiddenDelayFlow(safeId: SafeId): Flow<Duration?> =
        dao.getAutoLockOSKHiddenDelayFlow(safeId).distinctUntilChanged()

    override fun verifyPasswordIntervalFlow(safeId: SafeId): Flow<VerifyPasswordInterval?> =
        dao.getVerifyPasswordIntervalFlow(safeId).distinctUntilChanged()

    override fun materialYou(safeId: SafeId): Flow<Boolean?> =
        dao.getMaterialYou(safeId).distinctUntilChanged()

    override fun automationFlow(safeId: SafeId): Flow<Boolean?> =
        dao.getAutomationFlow(safeId).distinctUntilChanged()

    override fun displayShareWarningFlow(safeId: SafeId): Flow<Boolean?> =
        dao.getDisplayShareWarningFlow(safeId).distinctUntilChanged()

    override fun allowScreenshotFlow(safeId: SafeId): Flow<Boolean?> =
        dao.getAllowScreenshotFlow(safeId).distinctUntilChanged()

    override fun bubblesPreview(safeId: SafeId): Flow<Boolean?> =
        dao.getBubblesPreview(safeId).distinctUntilChanged()

    override fun shakeToLockFlow(safeId: SafeId): Flow<Boolean?> =
        dao.getShakeToLockFlow(safeId).distinctUntilChanged()

    override fun bubblesHomeCardCtaState(safeId: SafeId): Flow<CtaState?> =
        dao.getBubblesHomeCardCtaState(safeId).map { it?.toCtaState() }.distinctUntilChanged()

    override fun cameraSystemFlow(safeId: SafeId): Flow<CameraSystem?> =
        dao.getCameraSystemFlow(safeId).distinctUntilChanged()

    override fun autoLockInactivityDelayFlow(safeId: SafeId): Flow<Duration?> =
        dao.getAutoLockInactivityDelayFlow(safeId).distinctUntilChanged()

    override fun autoLockAppChangeDelayFlow(safeId: SafeId): Flow<Duration?> =
        dao.getAutoLockAppChangeDelayFlow(safeId).distinctUntilChanged()

    override fun clipboardDelayFlow(safeId: SafeId): Flow<Duration?> =
        dao.getClipboardDelayFlow(safeId).distinctUntilChanged()

    override fun autoLockOSKInactivityDelayFlow(safeId: SafeId): Flow<Duration?> =
        dao.getAutoLockOSKInactivityDelayFlow(safeId).distinctUntilChanged()

    override fun autoBackupEnabledFlow(safeId: SafeId): Flow<Boolean?> =
        dao.getAutoBackupEnabledFlow(safeId).distinctUntilChanged()

    override fun autoBackupFrequencyFlow(safeId: SafeId): Flow<Duration?> =
        dao.getAutoBackupFrequencyFlow(safeId).distinctUntilChanged()

    override fun autoBackupMaxNumberFlow(safeId: SafeId): Flow<Int?> =
        dao.getAutoBackupMaxNumberFlow(safeId).distinctUntilChanged()

    override fun cloudBackupEnabled(safeId: SafeId): Flow<Boolean?> =
        dao.getCloudBackupEnabled(safeId).distinctUntilChanged()

    override fun keepLocalBackupEnabled(safeId: SafeId): Flow<Boolean?> =
        dao.getKeepLocalBackupEnabled(safeId).distinctUntilChanged()

    override fun itemOrdering(safeId: SafeId): Flow<ItemOrder?> =
        dao.getItemOrdering(safeId).distinctUntilChanged()

    override fun itemLayout(safeId: SafeId): Flow<ItemLayout?> =
        dao.getItemLayout(safeId).distinctUntilChanged()

    override fun independentSafeInfoCtaState(safeId: SafeId): Flow<CtaState?> =
        dao.getIndependentSafeInfoCtaState(safeId).map { it?.toCtaState() }.distinctUntilChanged()

    override fun hasFinishOneSafeKOnBoardingFlow(safeId: SafeId): Flow<Boolean?> =
        dao.getHasFinishOneSafeKOnBoardingFlow(safeId).distinctUntilChanged()

    override fun hasDoneOnBoardingBubblesFlow(safeId: SafeId): Flow<Boolean?> =
        dao.getHasDoneOnBoardingBubblesFlow(safeId).distinctUntilChanged()

    override fun hasHiddenCameraTipsFlow(safeId: SafeId): Flow<Boolean?> =
        dao.getHasHiddenCameraTipsFlow(safeId).distinctUntilChanged()

    override fun hasSeenItemEditionUrlToolTipFlow(safeId: SafeId): Flow<Boolean?> =
        dao.getHasSeenItemEditionUrlToolTipFlow(safeId).distinctUntilChanged()

    override fun hasSeenItemEditionEmojiToolTipFlow(safeId: SafeId): Flow<Boolean?> =
        dao.getHasSeenItemEditionEmojiToolTipFlow(safeId).distinctUntilChanged()

    override fun hasSeenItemReadEditToolTipFlow(safeId: SafeId): Flow<Boolean?> =
        dao.getHasSeenItemReadEditToolTipFlow(safeId).distinctUntilChanged()

    override fun selectedAccountFlow(safeId: SafeId): Flow<String?> =
        dao.selectedDriveAccountFlow(safeId)

    override fun folderIdFlow(safeId: SafeId): Flow<String?> =
        dao.driveFolderId(safeId)

    override fun folderUrlFlow(safeId: SafeId): Flow<String?> =
        dao.driveFolderUrlFlow(safeId)

    override fun enableAutoBackupCtaState(safeId: SafeId): Flow<CtaState?> =
        dao.getEnableAutoBackupCtaState(safeId).map { it?.toCtaState() }.distinctUntilChanged()

    override fun bubblesResendMessageDelayFlow(safeId: SafeId): Flow<Duration?> {
        return dao.getBubblesResendMessageDelayFlow(safeId)
    }

    override suspend fun hasSeenDialogMessageSaveConfirmation(safeId: SafeId): Boolean {
        return dao.hasSeenDialogMessageSaveConfirmation(safeId)
    }

    override suspend fun getCameraSystem(safeId: SafeId): CameraSystem =
        dao.getCameraSystem(safeId)

    override suspend fun allowScreenshot(safeId: SafeId): Boolean =
        dao.getAllowScreenshot(safeId)

    override suspend fun shakeToLock(safeId: SafeId): Boolean =
        dao.getShakeToLock(safeId)

    override suspend fun toggleMaterialYou(safeId: SafeId): Unit =
        dao.toggleMaterialYou(safeId)

    override suspend fun toggleAutomation(safeId: SafeId): Unit =
        dao.toggleAutomation(safeId)

    override suspend fun disableShareWarningDisplay(safeId: SafeId): Unit =
        dao.disableShareWarningDisplay(safeId)

    override suspend fun toggleAllowScreenshot(safeId: SafeId): Unit =
        dao.toggleAllowScreenshot(safeId)

    override suspend fun toggleShakeToLock(safeId: SafeId): Unit =
        dao.toggleShakeToLock(safeId)

    override suspend fun setBubblesPreview(safeId: SafeId, value: Boolean): Unit =
        dao.setBubblesPreview(safeId, value)

    override suspend fun setCameraSystem(safeId: SafeId, value: CameraSystem): Unit =
        dao.setCameraSystem(safeId, value)

    override suspend fun setBubblesHomeCardCtaState(safeId: SafeId, ctaState: CtaState) {
        val state = RoomCtaState.fromCtaState(ctaState)
        dao.setBubblesHomeCardCtaState(safeId, state.state, state.timestamp)
    }

    override suspend fun setAutoLockInactivityDelay(safeId: SafeId, duration: Duration): Unit =
        dao.setAutoLockInactivityDelay(safeId, duration)

    override suspend fun setAutoLockAppChangeDelay(safeId: SafeId, duration: Duration): Unit =
        dao.setAutoLockAppChangeDelay(safeId, duration)

    override suspend fun setHasSeenDialogMessageSaveConfirmation(safeId: SafeId): Unit =
        dao.setHasSeenDialogMessageSaveConfirmation(safeId)

    override suspend fun setBubblesResendMessageDelay(safeId: SafeId, delay: Duration): Unit =
        dao.setBubblesResendMessageDelay(safeId, delay)

    override suspend fun setAutoLockOSKInactivityDelay(safeId: SafeId, duration: Duration): Unit =
        dao.setAutoLockOSKInactivityDelay(safeId, duration)

    override suspend fun setAutoLockOSKHiddenDelay(safeId: SafeId, duration: Duration): Unit =
        dao.setAutoLockOSKHiddenDelay(safeId, duration)

    override suspend fun toggleAutoBackupSettings(safeId: SafeId): Unit =
        dao.toggleAutoBackupSettings(safeId)

    override suspend fun setAutoBackupFrequency(safeId: SafeId, frequency: Duration): Unit =
        dao.setAutoBackupFrequency(safeId, frequency)

    override suspend fun updateAutoBackupMaxNumber(safeId: SafeId, updatedValue: Int): Unit =
        dao.updateAutoBackupMaxNumber(safeId, updatedValue)

    override suspend fun setCloudBackupEnabled(safeId: SafeId, enabled: Boolean): Unit =
        dao.setCloudBackupEnabled(safeId, enabled)

    override suspend fun setKeepLocalBackupSettings(safeId: SafeId, enabled: Boolean): Unit =
        dao.setKeepLocalBackupSettings(safeId, enabled)

    override suspend fun setItemOrdering(safeId: SafeId, order: ItemOrder): Unit =
        dao.setItemOrdering(safeId, order)

    override suspend fun setItemLayout(safeId: SafeId, style: ItemLayout): Unit =
        dao.setItemLayout(safeId, style)

    override suspend fun setEnableAutoBackupCtaState(safeId: SafeId, ctaState: CtaState) {
        val state = RoomCtaState.fromCtaState(ctaState)
        dao.setEnableAutoBackupCtaState(safeId, state.state, state.timestamp)
    }

    override suspend fun autoLockInactivityDelay(safeId: SafeId): Duration =
        dao.getAutoLockInactivityDelay(safeId)

    override suspend fun autoLockAppChangeDelay(safeId: SafeId): Duration =
        dao.autoLockAppChangeDelay(safeId)

    override suspend fun clipboardDelay(safeId: SafeId): Duration =
        dao.clipboardDelay(safeId)

    override suspend fun setClipboardClearDelay(safeId: SafeId, delay: Duration): Unit =
        dao.setClipboardClearDelay(safeId, delay)

    override suspend fun verifyPasswordInterval(safeId: SafeId): VerifyPasswordInterval =
        dao.verifyPasswordInterval(safeId)

    override suspend fun setVerifyPasswordInterval(safeId: SafeId, passwordInterval: VerifyPasswordInterval): Unit =
        dao.setVerifyPasswordInterval(safeId, passwordInterval)

    override suspend fun lastPasswordVerification(safeId: SafeId): Instant =
        dao.getLastPasswordVerification(safeId)

    override suspend fun setLastPasswordVerification(safeId: SafeId, instant: Instant): Unit =
        dao.setLastPasswordVerification(safeId, instant)

    override suspend fun autoLockOSKInactivityDelay(safeId: SafeId): Duration =
        dao.autoLockOSKInactivityDelay(safeId)

    override suspend fun autoLockOSKHiddenDelay(safeId: SafeId): Duration =
        dao.autoLockOSKHiddenDelay(safeId)

    override suspend fun automation(safeId: SafeId): Boolean =
        dao.getAutomation(safeId)

    override suspend fun displayShareWarning(safeId: SafeId): Boolean =
        dao.getDisplayShareWarning(safeId)

    override suspend fun setIndependentSafeInfoCtaState(safeId: SafeId, ctaState: CtaState) {
        val state = RoomCtaState.fromCtaState(ctaState)
        dao.setIndependentSafeInfoCtaState(safeId, state.state, state.timestamp)
    }

    override suspend fun hasSeenItemEditionUrlToolTip(safeId: SafeId): Boolean =
        dao.getHasSeenItemEditionUrlToolTip(safeId)

    override suspend fun hasSeenItemEditionEmojiToolTip(safeId: SafeId): Boolean =
        dao.getHasSeenItemEditionEmojiToolTip(safeId)

    override suspend fun setHasFinishOneSafeKOnBoarding(safeId: SafeId, value: Boolean) {
        dao.setHasFinishOneSafeKOnBoarding(safeId, value)
    }

    override suspend fun setHasDoneOnBoardingBubbles(safeId: SafeId, value: Boolean) {
        dao.setHasDoneOnBoardingBubbles(safeId, value)
    }

    override suspend fun setHasHiddenCameraTips(safeId: SafeId, value: Boolean) {
        dao.setHasHiddenCameraTips(safeId, value)
    }

    override suspend fun setHasSeenItemEditionUrlToolTip(safeId: SafeId, value: Boolean) {
        dao.setHasSeenItemEditionUrlToolTip(safeId, value)
    }

    override suspend fun setHasSeenItemEditionEmojiToolTip(safeId: SafeId, value: Boolean) {
        dao.setHasSeenItemEditionEmojiToolTip(safeId, value)
    }

    override suspend fun setHasSeenItemReadEditToolTip(safeId: SafeId, value: Boolean) {
        dao.setHasSeenItemReadEditToolTip(safeId, value)
    }

    override suspend fun autoBackupEnabled(safeId: SafeId): Boolean =
        dao.autoBackupEnabled(safeId)

    override suspend fun autoBackupFrequency(safeId: SafeId): Duration =
        dao.autoBackupFrequency(safeId)

    override suspend fun autoBackupMaxNumber(safeId: SafeId): Int =
        dao.autoBackupMaxNumber(safeId)

    override suspend fun getSafeAutoBackupEnabled(): List<SafeAutoBackupEnabled> =
        dao.getSafeAutoBackupEnabled()

    override suspend fun setDriveSelectedAccount(safeId: SafeId, account: String?): Unit =
        dao.setSelectedDriveAccount(safeId, account)

    override suspend fun setDriveFolderId(safeId: SafeId, id: String?): Unit =
        dao.setDriveFolderId(safeId, id)

    override suspend fun setDriveFolderUrl(safeId: SafeId, url: String?): Unit =
        dao.setDriveFolderUrl(safeId, url)

    override suspend fun selectedDriveAccount(safeId: SafeId): String? =
        dao.selectedDriveAccount(safeId)

    override suspend fun folderId(safeId: SafeId): String? =
        dao.folderId(safeId)

    override suspend fun folderUrl(safeId: SafeId): String? =
        dao.driveFolderUrl(safeId)

    override suspend fun bubblesResendMessageDelay(safeId: SafeId): Duration {
        return dao.getBubblesResendMessageDelay(safeId)
    }
}
