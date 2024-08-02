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
 * Last modified 6/19/24, 11:10 AM
 */

package studio.lunabee.onesafe.repository.datasource

import kotlinx.coroutines.flow.Flow
import studio.lunabee.onesafe.domain.common.CtaState
import studio.lunabee.onesafe.domain.model.camera.CameraSystem
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.model.safeitem.ItemLayout
import studio.lunabee.onesafe.domain.model.safeitem.ItemOrder
import studio.lunabee.onesafe.domain.model.verifypassword.VerifyPasswordInterval
import java.time.Instant
import kotlin.time.Duration

interface SafeSettingsLocalDataSource {
    fun autoLockOSKHiddenDelayFlow(safeId: SafeId): Flow<Duration?>
    fun verifyPasswordIntervalFlow(safeId: SafeId): Flow<VerifyPasswordInterval?>
    fun materialYou(safeId: SafeId): Flow<Boolean?>
    fun automationFlow(safeId: SafeId): Flow<Boolean?>
    fun displayShareWarningFlow(safeId: SafeId): Flow<Boolean?>
    fun allowScreenshotFlow(safeId: SafeId): Flow<Boolean?>
    fun shakeToLockFlow(safeId: SafeId): Flow<Boolean?>
    fun bubblesPreview(safeId: SafeId): Flow<Boolean?>
    fun bubblesHomeCardCtaState(safeId: SafeId): Flow<CtaState?>
    fun cameraSystemFlow(safeId: SafeId): Flow<CameraSystem?>
    fun autoLockInactivityDelayFlow(safeId: SafeId): Flow<Duration?>
    fun autoLockAppChangeDelayFlow(safeId: SafeId): Flow<Duration?>
    fun clipboardDelayFlow(safeId: SafeId): Flow<Duration?>
    fun autoLockOSKInactivityDelayFlow(safeId: SafeId): Flow<Duration?>
    fun itemOrdering(safeId: SafeId): Flow<ItemOrder?>
    fun itemLayout(safeId: SafeId): Flow<ItemLayout?>
    fun independentSafeInfoCtaState(safeId: SafeId): Flow<CtaState?>
    fun hasFinishOneSafeKOnBoardingFlow(safeId: SafeId): Flow<Boolean?>
    fun hasDoneOnBoardingBubblesFlow(safeId: SafeId): Flow<Boolean?>
    fun hasHiddenCameraTipsFlow(safeId: SafeId): Flow<Boolean?>
    fun hasSeenItemEditionUrlToolTipFlow(safeId: SafeId): Flow<Boolean?>
    fun hasSeenItemEditionEmojiToolTipFlow(safeId: SafeId): Flow<Boolean?>
    fun hasSeenItemReadEditToolTipFlow(safeId: SafeId): Flow<Boolean?>
    fun bubblesResendMessageDelayFlow(safeId: SafeId): Flow<Duration?>

    suspend fun getCameraSystem(safeId: SafeId): CameraSystem
    suspend fun allowScreenshot(safeId: SafeId): Boolean
    suspend fun toggleMaterialYou(safeId: SafeId)
    suspend fun toggleAutomation(safeId: SafeId)
    suspend fun disableShareWarningDisplay(safeId: SafeId)
    suspend fun toggleAllowScreenshot(safeId: SafeId)
    suspend fun toggleShakeToLock(safeId: SafeId)
    suspend fun setBubblesPreview(safeId: SafeId, value: Boolean)
    suspend fun setCameraSystem(safeId: SafeId, value: CameraSystem)
    suspend fun setBubblesHomeCardCtaState(safeId: SafeId, ctaState: CtaState)
    suspend fun setAutoLockInactivityDelay(safeId: SafeId, duration: Duration)
    suspend fun setAutoLockAppChangeDelay(safeId: SafeId, duration: Duration)
    suspend fun setBubblesResendMessageDelay(safeId: SafeId, delay: Duration)
    suspend fun setAutoLockOSKInactivityDelay(safeId: SafeId, duration: Duration)
    suspend fun setAutoLockOSKHiddenDelay(safeId: SafeId, duration: Duration)
    suspend fun setItemOrdering(safeId: SafeId, order: ItemOrder)
    suspend fun setItemLayout(safeId: SafeId, style: ItemLayout)
    suspend fun autoLockInactivityDelay(safeId: SafeId): Duration
    suspend fun autoLockAppChangeDelay(safeId: SafeId): Duration
    suspend fun clipboardDelay(safeId: SafeId): Duration
    suspend fun setClipboardClearDelay(safeId: SafeId, delay: Duration)
    suspend fun verifyPasswordInterval(safeId: SafeId): VerifyPasswordInterval
    suspend fun setPasswordInterval(safeId: SafeId, passwordInterval: VerifyPasswordInterval)
    suspend fun lastPasswordVerification(safeId: SafeId): Instant
    suspend fun setLastPasswordVerification(safeId: SafeId, instant: Instant)
    suspend fun autoLockOSKInactivityDelay(safeId: SafeId): Duration
    suspend fun autoLockOSKHiddenDelay(safeId: SafeId): Duration
    suspend fun automation(safeId: SafeId): Boolean
    suspend fun displayShareWarning(safeId: SafeId): Boolean
    suspend fun setIndependentSafeInfoCtaState(safeId: SafeId, ctaState: CtaState)
    suspend fun hasSeenItemEditionUrlToolTip(safeId: SafeId): Boolean
    suspend fun hasSeenItemEditionEmojiToolTip(safeId: SafeId): Boolean
    suspend fun setHasFinishOneSafeKOnBoarding(safeId: SafeId, value: Boolean)
    suspend fun setHasDoneOnBoardingBubbles(safeId: SafeId, value: Boolean)
    suspend fun setHasHiddenCameraTips(safeId: SafeId, value: Boolean)
    suspend fun setHasSeenItemEditionUrlToolTip(safeId: SafeId, value: Boolean)
    suspend fun setHasSeenItemEditionEmojiToolTip(safeId: SafeId, value: Boolean)
    suspend fun setHasSeenItemReadEditToolTip(safeId: SafeId, value: Boolean)
    suspend fun bubblesResendMessageDelay(safeId: SafeId): Duration
}
