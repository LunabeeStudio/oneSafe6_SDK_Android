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

package studio.lunabee.onesafe

import studio.lunabee.onesafe.domain.Constant
import studio.lunabee.onesafe.domain.common.CtaState
import studio.lunabee.onesafe.domain.model.camera.CameraSystem
import studio.lunabee.onesafe.domain.model.safeitem.ItemLayout
import studio.lunabee.onesafe.domain.model.safeitem.ItemOrder
import studio.lunabee.onesafe.domain.model.verifypassword.VerifyPasswordInterval
import java.time.Clock
import java.time.Instant
import kotlin.time.Duration.Companion.days

object SettingsDefaults {
    const val AutoLockInactivityDelayMsDefault: Long = 30_000
    const val AutoLockAppChangeDelayMsDefault: Long = 10_000
    const val MaterialYouSettingDefault: Boolean = true
    const val AutomationSettingDefault: Boolean = true
    const val AllowScreenshotSettingDefault: Boolean = false
    const val ShakeToLockSettingDefault: Boolean = false
    const val ClipboardClearDelayMsDefault: Long = 30_000
    const val DisplayShareWarningDefault: Boolean = true
    val VerifyPasswordIntervalDefault: VerifyPasswordInterval = VerifyPasswordInterval.EVERY_WEEK
    val BubblesResendMessageDelayMsDefault: Long = 1.days.inWholeMilliseconds
    const val BubblesPreviewDefault: Boolean = false
    val BubblesPreviewCardDefault: CtaState = CtaState.Hidden
    const val AutoBackupEnabledDefault: Boolean = false
    val AutoBackupFrequencyMsDefault: Long = 1.days.inWholeMilliseconds
    const val AutoBackupMaxNumberDefault: Int = 5
    val CameraSystemDefault: CameraSystem = CameraSystem.InApp
    const val CloudBackupEnabledDefault: Boolean = false
    const val KeepLocalBackupEnabledDefault: Boolean = false
    val ItemOrderingDefault: ItemOrder = ItemOrder.Alphabetic
    val ItemLayoutDefault: ItemLayout = ItemLayout.Grid
    val EnableAutoBackupCtaState: CtaState = CtaState.Hidden
    fun lastPasswordVerificationDefault(clock: Clock): Instant = Instant.now(clock)
    fun independentSafeInfoCtaState(clock: Clock): CtaState = CtaState.VisibleSince(Instant.now(clock))
    fun preventionWarningCtaState(clock: Clock): CtaState = CtaState.DismissedAt(
        Instant.now(clock).minus(Constant.InitialDelay, Constant.DelayUnitPreventionWarningCtaState),
    )
}
