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
 * Created by Lunabee Studio / Date - 10/17/2024 - for the oneSafe6 SDK.
 * Last modified 17/10/2024 14:05
 */

package studio.lunabee.onesafe.feature.settings.prevention

import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.R

enum class UiPreventionSettingsWarning(
    val title: LbcTextSpec,
    val description: LbcTextSpec,
) {
    PasswordVerification(
        title = LbcTextSpec.StringResource(R.string.settings_warning_password_title),
        description = LbcTextSpec.StringResource(R.string.settings_warning_password_description),
    ),
    Backup(
        title = LbcTextSpec.StringResource(R.string.settings_warning_backup_title),
        description = LbcTextSpec.StringResource(R.string.settings_warning_backup_description),
    ),
    PasswordVerificationAndBackup(
        title = LbcTextSpec.StringResource(R.string.settings_warning_passwordAndBackup_title),
        description = LbcTextSpec.StringResource(R.string.settings_warning_passwordAndBackup_description),
    ),
}
