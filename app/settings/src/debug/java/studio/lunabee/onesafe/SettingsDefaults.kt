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

import studio.lunabee.onesafe.domain.model.verifypassword.VerifyPasswordInterval
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

/**
 * ⚠️ Debug defaults values
 *
 * See release default in release/java/studio/lunabee/onesafe/SettingsDefaults.kt
 */
internal object SettingsDefaults {
    val AutoLockInactivityDelayMsDefault: Long = Duration.INFINITE.inWholeMilliseconds
    val AutoLockAppChangeDelayMsDefault: Long = Duration.INFINITE.inWholeMilliseconds
    const val MaterialYouSettingDefault: Boolean = true
    const val AutomationSettingDefault: Boolean = true
    const val AllowScreenshotSettingDefault: Boolean = true
    val ClipboardClearDelayMsDefault: Long = Duration.INFINITE.inWholeMilliseconds
    const val DisplayShareWarningDefault: Boolean = true
    val VerifyPasswordIntervalDefault: VerifyPasswordInterval = VerifyPasswordInterval.NEVER
    const val BubblesPreviewDefault: Boolean = true
    val BubblesResendMessageDelayMsDefault: Long = 1.days.inWholeMilliseconds
    const val autoBackupEnabledDefault: Boolean = false
    val autoBackupFrequencyMsDefault: Long = 1.days.inWholeMilliseconds
}
