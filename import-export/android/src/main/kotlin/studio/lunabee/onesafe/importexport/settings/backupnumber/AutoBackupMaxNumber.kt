/*
 * Copyright (c) 2024-2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 4/23/2024 - for the oneSafe6 SDK.
 * Last modified 4/23/24, 3:14 PM
 */

package studio.lunabee.onesafe.importexport.settings.backupnumber

import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString

enum class AutoBackupMaxNumber(val value: Int, val text: LbcTextSpec) {
    FIVE(5, LbcTextSpec.StringResource(OSString.settings_autoBackup_maxNumber_five)),
    TEN(10, LbcTextSpec.StringResource(OSString.settings_autoBackup_maxNumber_ten)),
    TWENTY(20, LbcTextSpec.StringResource(OSString.settings_autoBackup_maxNumber_twenty)),
    FORTY(40, LbcTextSpec.StringResource(OSString.settings_autoBackup_maxNumber_forty)),
    ;

    companion object {
        fun valueForInt(value: Int): AutoBackupMaxNumber {
            return when (value) {
                5 -> FIVE
                10 -> TEN
                20 -> TWENTY
                40 -> FORTY
                else -> throw IllegalArgumentException("Unknown value: $value")
            }
        }
    }
}
