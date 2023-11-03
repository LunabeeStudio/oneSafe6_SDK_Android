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
import studio.lunabee.onesafe.commonui.R
import studio.lunabee.onesafe.commonui.settings.CardSettingsAction

internal class CardSettingsRestoreAutoBackup(override val onClick: () -> Unit) : CardSettingsAction(
    icon = R.drawable.ic_save,
    text = LbcTextSpec.StringResource(R.string.settings_autoBackupScreen_restore_button),
)

internal class CardSettingsSelectFrequency(frequency: AutoBackupFrequency, override val onClick: () -> Unit) : CardSettingsAction(
    icon = null,
    text = LbcTextSpec.StringResource(R.string.settings_autoBackupScreen_autoBackupFrequency_title),
    onClickLabel = LbcTextSpec.StringResource(R.string.common_modify),
    secondaryText = frequency.text,
)

internal class CardSettingsAccessLocalBackup(override val onClick: () -> Unit) : CardSettingsAction(
    icon = R.drawable.ic_phone,
    text = LbcTextSpec.StringResource(R.string.settings_autoBackupScreen_saveAccess_localSaves),
)

internal class CardSettingsAccessRemoteBackup(override val onClick: () -> Unit) : CardSettingsAction(
    icon = R.drawable.ic_cloud,
    text = LbcTextSpec.Raw("TODO Drive"), // TODO drive loco
)