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
 * Created by Lunabee Studio / Date - 9/27/2023 - for the oneSafe6 SDK.
 * Last modified 27/09/2023 17:08
 */

package studio.lunabee.onesafe.ime

import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import studio.lunabee.onesafe.ui.theme.OSKColorPalette
import studio.lunabee.onesafe.ui.theme.OSTheme

@Composable
fun ImeOSTheme(
    content: @Composable () -> Unit,
) {
    OSTheme(
        isMaterialYouSettingsEnabled = false,
        colorPalette = OSKColorPalette,
        darkColorScheme = darkColorScheme(
            primary = OSKColorPalette.Primary01,
            onPrimary = OSKColorPalette.Primary30,
            secondary = OSKColorPalette.Primary10,
            primaryContainer = OSKColorPalette.Primary85,
            onPrimaryContainer = OSKColorPalette.Primary10,
            secondaryContainer = OSKColorPalette.Primary30,
            onSecondaryContainer = OSKColorPalette.Primary10,
            surface = OSKColorPalette.Neutral90,
            onSurface = OSKColorPalette.Neutral10,
            background = OSKColorPalette.Neutral90,
            onBackground = OSKColorPalette.Neutral10,
            surfaceVariant = OSKColorPalette.Primary75,
            error = OSKColorPalette.Alert35,
            onError = OSKColorPalette.Neutral00,
            errorContainer = OSKColorPalette.Alert05,
            onErrorContainer = OSKColorPalette.Alert35,
        ),
        content = content,
    )
}
