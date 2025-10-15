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

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import studio.lunabee.onesafe.ui.theme.OSKColorPalette
import studio.lunabee.onesafe.ui.theme.OSTheme

@Composable
fun ImeOSTheme(
    isNightTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorPalette = OSKColorPalette
    OSTheme(
        isSystemInDarkTheme = isNightTheme,
        isMaterialYouSettingsEnabled = false,
        colorPalette = colorPalette,
        darkColorScheme = darkColorScheme(
            primary = OSKColorPalette.primary01,
            onPrimary = OSKColorPalette.primary30,
            secondary = OSKColorPalette.primary10,
            primaryContainer = OSKColorPalette.primary85,
            onPrimaryContainer = OSKColorPalette.primary10,
            secondaryContainer = OSKColorPalette.primary30,
            onSecondaryContainer = OSKColorPalette.primary10,
            surface = OSKColorPalette.neutral90,
            onSurface = OSKColorPalette.neutral10,
            background = OSKColorPalette.neutral90,
            onBackground = OSKColorPalette.neutral10,
            surfaceVariant = OSKColorPalette.primary75,
            error = OSKColorPalette.alert35,
            onError = OSKColorPalette.neutral00,
            errorContainer = OSKColorPalette.alert05,
            onErrorContainer = OSKColorPalette.alert35,
        ),
        lightColorScheme = lightColorScheme(
            primary = colorPalette.primary30,
            onPrimary = colorPalette.primary01,
            secondary = colorPalette.primary30,
            primaryContainer = colorPalette.primary03,
            onPrimaryContainer = colorPalette.primary30,
            secondaryContainer = colorPalette.primary05,
            onSecondaryContainer = colorPalette.primary40,
            surface = colorPalette.primary01,
            onSurface = colorPalette.neutral80,
            background = colorPalette.primary01,
            onBackground = colorPalette.neutral80,
            surfaceVariant = colorPalette.primary01,
            error = colorPalette.alert35,
            onError = colorPalette.neutral00,
            errorContainer = colorPalette.alert05,
            onErrorContainer = colorPalette.alert35,
        ),
        content = content,
    )
}
