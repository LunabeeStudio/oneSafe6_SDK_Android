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

import studio.lunabee.onesafe.domain.model.camera.CameraSystem
import studio.lunabee.onesafe.domain.model.safeitem.ItemOrder
import studio.lunabee.onesafe.domain.model.safeitem.ItemsLayoutSettings
import studio.lunabee.onesafe.domain.model.verifypassword.VerifyPasswordInterval
import kotlin.time.Duration.Companion.days

object SettingsDefaults {
    const val AutoLockInactivityDelayMsDefault: Long = 30_000
    const val AutoLockAppChangeDelayMsDefault: Long = 10_000
    const val MaterialYouSettingDefault: Boolean = true
    const val AutomationSettingDefault: Boolean = true
    const val AllowScreenshotSettingDefault: Boolean = false
    const val ClipboardClearDelayMsDefault: Long = 30_000
    const val DisplayShareWarningDefault: Boolean = true
    val VerifyPasswordIntervalDefault: VerifyPasswordInterval = VerifyPasswordInterval.EVERY_TWO_MONTHS
    val BubblesResendMessageDelayMsDefault: Long = 1.days.inWholeMilliseconds
    const val BubblesPreviewDefault: Boolean = false
    const val autoBackupEnabledDefault: Boolean = false
    val autoBackupFrequencyMsDefault: Long = 1.days.inWholeMilliseconds
    val CameraSystemDefault: CameraSystem = CameraSystem.InApp
    const val cloudBackupEnabledDefault: Boolean = false
    const val keepLocalBackupEnabledDefault: Boolean = false
    val itemOrderingDefault: ItemOrder = ItemOrder.Alphabetic
    val ItemsLayoutSettingDefault: ItemsLayoutSettings = ItemsLayoutSettings.Grid
}
