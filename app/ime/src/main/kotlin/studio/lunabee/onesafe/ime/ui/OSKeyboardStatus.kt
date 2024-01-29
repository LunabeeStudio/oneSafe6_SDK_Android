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
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.ime.ImeOSTheme
import studio.lunabee.onesafe.ui.theme.OSKColorPalette
import studio.lunabee.onesafe.ime.R as imeR

enum class OSKeyboardStatus {
    LoggedIn {
        override val contentDescription: LbcTextSpec = LbcTextSpec.StringResource(OSString.oneSafeK_ime_status_login_description)
        override val logo: Int = imeR.drawable.onesafek_logo_login
        override val tint: Color
            @Composable get() = if (LocalKeyboardIsNightMode.current) OSKColorPalette.Primary20 else OSKColorPalette.Primary30
    },
    LoggedOut {
        override val contentDescription: LbcTextSpec = LbcTextSpec.StringResource(OSString.oneSafeK_ime_status_logout_description)
        override val logo: Int = imeR.drawable.onesafek_logo_logout
        override val tint: Color
            @Composable get() = if (LocalKeyboardIsNightMode.current) OSKColorPalette.Primary10 else OSKColorPalette.Primary60
    },
    ;

    @Composable
    fun Logo(modifier: Modifier = Modifier) {
        key(logo) { // FIXME this key seems to fix a weird issue/optim of partially drawn logo problem
            Icon(
                painter = painterResource(logo),
                contentDescription = contentDescription.string,
                modifier = modifier,
                tint = tint,
            )
        }
    }

    abstract val contentDescription: LbcTextSpec
    abstract val tint: Color
        @Composable get

    @get:DrawableRes
    abstract val logo: Int
}

@Composable
@Preview
private fun OSKeyboardStatusLogoPreview() {
    ImeOSTheme {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OSKeyboardStatus.entries.forEach { status ->
                booleanArrayOf(true, false).forEach { isNight ->
                    CompositionLocalProvider(
                        LocalKeyboardIsNightMode provides isNight,
                    ) {
                        Surface(color = if (isNight) Color.DarkGray else Color.White) {
                            status.Logo()
                        }
                    }
                }
            }
        }
    }
}
