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
 * Created by Lunabee Studio / Date - 9/26/2023 - for the oneSafe6 SDK.
 * Last modified 9/26/23, 5:41 PM
 */

package studio.lunabee.onesafe.ime.ui.settings

import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.R
import studio.lunabee.onesafe.commonui.settings.AutoLockBackgroundDelay
import studio.lunabee.onesafe.commonui.settings.AutoLockInactivityDelay
import studio.lunabee.onesafe.commonui.settings.CardSettingsNavAction

class CardSettingsActionAutoLockOSKInactivityAction(
    delay: AutoLockInactivityDelay,
    override val onClick: () -> Unit,
) : CardSettingsNavAction(
    icon = null,
    text = LbcTextSpec.StringResource(R.string.oneSafeK_extension_configuration_autolock_inactivity),
    onClickLabel = LbcTextSpec.StringResource(R.string.common_modify),
    secondaryText = delay.text,
)

class CardSettingsActionAutoLockOSKHiddenAction(delay: AutoLockBackgroundDelay, override val onClick: () -> Unit) : CardSettingsNavAction(
    icon = null,
    text = LbcTextSpec.StringResource(R.string.oneSafeK_extension_configuration_autolock_hidden),
    onClickLabel = LbcTextSpec.StringResource(R.string.common_modify),
    secondaryText = delay.text,
)
