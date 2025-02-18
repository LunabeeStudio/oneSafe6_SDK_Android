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
 * Created by Lunabee Studio / Date - 9/27/2024 - for the oneSafe6 SDK.
 * Last modified 27/09/2024 10:42
 */

package studio.lunabee.onesafe.feature.settings.panicwidget.model

import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString

data class PanicWidgetSettingsStrings(
    val description: LbcTextSpec,
    val buttonText: LbcTextSpec,
) {
    companion object {
        private val InstalledAndDisabled: PanicWidgetSettingsStrings = PanicWidgetSettingsStrings(
            description = LbcTextSpec.StringResource(OSString.panicdestruction_settings_destruction_installedAndDisabled),
            buttonText = LbcTextSpec.StringResource(OSString.panicdestruction_settings_action_installedAndDisabled),
        )
        private val Disabled: PanicWidgetSettingsStrings = PanicWidgetSettingsStrings(
            description = LbcTextSpec.StringResource(OSString.panicdestruction_settings_description_disabled),
            buttonText = LbcTextSpec.StringResource(OSString.panicdestruction_settings_action_disabled),
        )
        private val Enabled: PanicWidgetSettingsStrings = PanicWidgetSettingsStrings(
            description = LbcTextSpec.StringResource(OSString.panicdestruction_settings_description_enabled),
            buttonText = LbcTextSpec.StringResource(OSString.panicdestruction_settings_action_enabled),
        )

        fun fromPanicWidgetUiState(uiState: PanicWidgetSettingsUiState): PanicWidgetSettingsStrings {
            return when {
                uiState.isWidgetEnabled && uiState.isPanicDestructionEnabled -> Enabled
                uiState.isWidgetEnabled -> InstalledAndDisabled
                else -> Disabled
            }
        }
    }
}
