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
 * Last modified 6/19/24, 12:19 PM
 */

package studio.lunabee.onesafe.domain.model.safe

import studio.lunabee.onesafe.domain.common.CtaState
import studio.lunabee.onesafe.domain.model.camera.CameraSystem
import studio.lunabee.onesafe.domain.model.safeitem.ItemLayout
import studio.lunabee.onesafe.domain.model.safeitem.ItemOrder
import studio.lunabee.onesafe.domain.model.verifypassword.VerifyPasswordInterval
import java.time.Instant
import kotlin.time.Duration

data class SafeSettings(
    val version: Int,
    val materialYou: Boolean,
    val automation: Boolean,
    val displayShareWarning: Boolean,
    val allowScreenshot: Boolean,
    val shakeToLock: Boolean,
    val bubblesPreview: Boolean,
    val cameraSystem: CameraSystem,
    val autoLockOSKHiddenDelay: Duration,
    val verifyPasswordInterval: VerifyPasswordInterval,
    val bubblesHomeCardCtaState: CtaState,
    val autoLockInactivityDelay: Duration,
    val autoLockAppChangeDelay: Duration,
    val clipboardDelay: Duration,
    val bubblesResendMessageDelay: Duration,
    val autoLockOSKInactivityDelay: Duration,
    val autoBackupEnabled: Boolean,
    val autoBackupFrequency: Duration,
    val autoBackupMaxNumber: Int,
    val cloudBackupEnabled: Boolean,
    val keepLocalBackupEnabled: Boolean,
    val itemOrdering: ItemOrder,
    val itemLayout: ItemLayout,
    val enableAutoBackupCtaState: CtaState,
    val lastPasswordVerification: Instant,
    val independentSafeInfoCtaState: CtaState,
    val preventionWarningCtaState: CtaState,
    val lastExportDate: Instant?,
)
