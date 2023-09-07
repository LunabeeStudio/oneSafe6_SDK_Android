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
 * Created by Lunabee Studio / Date - 8/31/2023 - for the oneSafe6 SDK.
 * Last modified 8/31/23, 1:33 PM
 */

package studio.lunabee.onesafe.ime.ui

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import studio.lunabee.onesafe.commonui.R
import studio.lunabee.onesafe.extension.loremIpsum
import studio.lunabee.onesafe.ime.model.ImeClient
import studio.lunabee.onesafe.ime.ui.res.ImeDimens
import studio.lunabee.onesafe.ime.ui.res.ImeShape
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewOnSurfaceTheme

@Composable
fun ImeOSTopBar(
    imeClient: ImeClient?,
    isCryptoDataReady: Boolean,
    onLogoClick: () -> Unit,
    isDark: Boolean,
    onLockClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val osStatus = if (isCryptoDataReady) {
            OSKeyboardStatus.LoggedIn
        } else {
            OSKeyboardStatus.LoggedOut
        }
        osStatus.Logo(
            modifier = Modifier
                .clip(ImeShape.Key)
                .clickable { onLogoClick() }
                .padding(vertical = ImeDimens.LogoVerticalPadding, horizontal = ImeDimens.LogoHorizontalPadding),
            isDark = isDark,
        )
        imeClient?.let {
            it.Logo(
                Modifier.padding(end = OSDimens.SystemSpacing.Small),
            )
            it.Name()
        }
        Spacer(Modifier.weight(1f))
        if (isCryptoDataReady) {
            IconButton(onClick = onLockClick) {
                Icon(
                    painter = painterResource(R.drawable.ic_unlock),
                    contentDescription = stringResource(id = R.string.oneSafeK_imeTopBar_lock_contentDescription),
                )
            }
        } else {
            IconButton(onClick = onLockClick) {
                Icon(
                    painter = painterResource(R.drawable.ic_lock),
                    contentDescription = stringResource(id = R.string.oneSafeK_imeTopBar_unlock_contentDescription),
                )
            }
        }
    }
}

@Composable
@Preview(name = "Light mode")
private fun ImeOSTopBarLightPreview() {
    ImeOSTopBarPreview(false)
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark mode")
private fun ImeOSTopBarDarkPreview() {
    ImeOSTopBarPreview(true)
}

@Composable
private fun ImeOSTopBarPreview(isDark: Boolean) {
    OSPreviewOnSurfaceTheme {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            arrayOf(true, false).forEach { isCryptoDataReady ->
                ImeOSTopBar(
                    imeClient = ImeClient("", loremIpsum(1), null),
                    isCryptoDataReady = isCryptoDataReady,
                    onLogoClick = {},
                    isDark = isDark,
                    onLockClick = {},
                )
            }
        }
    }
}
