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
 * Created by Lunabee Studio / Date - 9/26/2024 - for the oneSafe6 SDK.
 * Last modified 26/09/2024 16:28
 */

package studio.lunabee.onesafe.feature.settings.settingcard.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.settings.SettingsCard
import studio.lunabee.onesafe.feature.settings.settingcard.action.PanicWidgetSettingsAction

@Composable
fun WidgetPanicModeSettingsCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    isWidgetEnabled: Boolean,
    isPanicModeEnabled: Boolean,
) {
    SettingsCard(
        title = LbcTextSpec.StringResource(OSString.settings_panicDestruction_section_title),
        modifier = modifier,
        actions = listOf(
            PanicWidgetSettingsAction(
                onClick = onClick,
                isWidgetEnabled = isWidgetEnabled,
                isPanicModeEnabled = isPanicModeEnabled,
            ),
        ),
    )
}
