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
 * Created by Lunabee Studio / Date - 7/3/2023 - for the oneSafe6 SDK.
 * Last modified 7/3/23, 10:35 AM
 */

package studio.lunabee.onesafe.ime.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.R
import studio.lunabee.onesafe.ui.theme.LocalColorPalette
import studio.lunabee.onesafe.utils.OsDefaultPreview
import studio.lunabee.onesafe.ime.R as imeR

enum class OSKeyboardStatus {
    LoggedIn {
        override val contentDescription: LbcTextSpec = LbcTextSpec.StringResource(R.string.oneSafeK_ime_status_login_description)
        override val logo: Int = imeR.drawable.onesafek_logo_login
        override val lightTint: @Composable () -> Color = { MaterialTheme.colorScheme.primary }
        override val darkTint: @Composable () -> Color = { MaterialTheme.colorScheme.primary }
    },

    LoggedOut {
        override val contentDescription: LbcTextSpec = LbcTextSpec.StringResource(R.string.oneSafeK_ime_status_logout_description)
        override val logo: Int = imeR.drawable.onesafek_logo_logout
        override val lightTint: @Composable () -> Color = { LocalColorPalette.current.Primary60 }
        override val darkTint: @Composable () -> Color = { LocalColorPalette.current.Primary60 }
    },

    Love {
        override val contentDescription: LbcTextSpec = LbcTextSpec.StringResource(R.string.oneSafeK_ime_status_love_description)
        override val logo: Int = imeR.drawable.onesafek_logo_love
        override val lightTint: @Composable () -> Color = { LocalColorPalette.current.Success40 }
        override val darkTint: @Composable () -> Color = { LocalColorPalette.current.Success20 }
    },

    Secured {
        override val contentDescription: LbcTextSpec = LbcTextSpec.StringResource(R.string.oneSafeK_ime_status_secured_description)
        override val logo: Int = imeR.drawable.onesafek_logo_secured
        override val lightTint: @Composable () -> Color = { LocalColorPalette.current.Alert35 }
        override val darkTint: @Composable () -> Color = { LocalColorPalette.current.Alert20 }
    },
    ;

    @Composable
    fun Logo(modifier: Modifier = Modifier, isDark: Boolean = false) {
        Icon(
            painter = painterResource(logo),
            contentDescription = contentDescription.string,
            modifier = modifier,
            tint = if (isDark) darkTint() else lightTint(),
        )
    }

    abstract val contentDescription: LbcTextSpec

    @get:DrawableRes
    abstract val logo: Int
    abstract val lightTint: @Composable () -> Color
    abstract val darkTint: @Composable () -> Color
}

@Composable
@OsDefaultPreview
private fun OSKeyboardStatusLogoPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OSKeyboardStatus.entries.forEach {
            it.Logo()
        }
    }
}
