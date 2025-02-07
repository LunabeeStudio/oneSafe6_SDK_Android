/*
 * Copyright (c) 2023-2023 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 10/17/2023 - for the oneSafe6 SDK.
 * Last modified 10/9/23, 6:29 PM
 */

package studio.lunabee.onesafe.importexport.settings

import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

enum class AutoBackupFrequency(val repeat: Duration, val flex: Duration, val text: LbcTextSpec) {
    DAILY(1.days, 1.hours, LbcTextSpec.StringResource(OSString.settings_autoBackup_frequency_everyDay_title)),
    WEEKLY(7.days, 12.hours, LbcTextSpec.StringResource(OSString.settings_autoBackup_frequency_everyWeek_title)),
    MONTHLY(30.days, 2.days, LbcTextSpec.StringResource(OSString.settings_autoBackup_frequency_everyMonth_title)),
    ;

    companion object {
        fun valueForDuration(delay: Duration): AutoBackupFrequency {
            return AutoBackupFrequency.entries.reversed().firstOrNull { delay >= it.repeat } ?: DAILY
        }
    }
}
