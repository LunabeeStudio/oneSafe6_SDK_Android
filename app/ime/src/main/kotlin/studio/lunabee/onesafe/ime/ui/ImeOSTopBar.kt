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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.extension.loremIpsum
import studio.lunabee.onesafe.ime.ImeOSTheme
import studio.lunabee.onesafe.ime.model.ImeClient
import studio.lunabee.onesafe.ime.ui.res.ImeDimens
import studio.lunabee.onesafe.ime.ui.res.ImeShape
import studio.lunabee.onesafe.ime.ui.tutorial.LockOskTutorialLayout
import studio.lunabee.onesafe.ime.ui.tutorial.OpenOskTutorialLayout
import studio.lunabee.onesafe.molecule.OSActionButton
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun ImeOSTopBar(
    imeClient: ImeClient?,
    isCryptoDataReady: Boolean,
    onLogoClick: () -> Unit,
    onLockClick: () -> Unit,
    displayOpenTutorial: Boolean,
    displayLockTutorial: Boolean,
    closeLockTutorial: () -> Unit,
    closeOpenTutorial: () -> Unit,
) {
    Column {
        AnimatedVisibility(
            visible = displayLockTutorial,
            enter = expandVertically(expandFrom = Alignment.Top),
            exit = shrinkVertically(shrinkTowards = Alignment.Top),
        ) {
            LockOskTutorialLayout(closeLockTutorial)
        }
        AnimatedVisibility(
            visible = displayOpenTutorial,
            enter = expandVertically(expandFrom = Alignment.Top),
            exit = shrinkVertically(shrinkTowards = Alignment.Top),
        ) {
            OpenOskTutorialLayout(closeOpenTutorial)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (isCryptoDataReady) {
                OSKeyboardStatus.LoggedIn
            } else {
                OSKeyboardStatus.LoggedOut
            }.Logo(
                modifier = Modifier
                    .clip(ImeShape.Key)
                    .clickable { onLogoClick() }
                    .padding(vertical = ImeDimens.LogoVerticalPadding, horizontal = ImeDimens.LogoHorizontalPadding),
            )
            imeClient?.let {
                it.Logo(Modifier.padding(end = OSDimens.SystemSpacing.Small))
                it.Name(modifier = Modifier.weight(1f))
            }
            Box(
                modifier = Modifier.width(IntrinsicSize.Max),
                contentAlignment = Alignment.CenterEnd,
            ) {
                if (isCryptoDataReady) {
                    OSActionButton(
                        onClick = onLockClick,
                        text = LbcTextSpec.StringResource(OSString.oneSafeK_lock),
                        contentPadding = PaddingValues(horizontal = OSDimens.SystemSpacing.Medium),
                        startIcon = OSImageSpec.Drawable(OSDrawable.ic_lock),
                    ).Composable(
                        modifier = Modifier.clip(ImeShape.Key),
                    )
                } else {
                    OSActionButton(
                        onClick = onLockClick,
                        text = LbcTextSpec.StringResource(OSString.oneSafeK_open),
                        contentPadding = PaddingValues(horizontal = OSDimens.SystemSpacing.Medium),
                        startIcon = OSImageSpec.Drawable(OSDrawable.ic_key),
                    ).Composable(
                        modifier = Modifier.clip(ImeShape.Key),
                    )
                }
            }
        }
    }
}

@Composable
@OsDefaultPreview
private fun ImeOSTopBarPreview() {
    ImeOSTheme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                arrayOf(true, false).forEach { isCryptoDataReady ->
                    ImeOSTopBar(
                        imeClient = ImeClient("", loremIpsum(1), null),
                        isCryptoDataReady = isCryptoDataReady,
                        onLogoClick = {},
                        onLockClick = {},
                        displayOpenTutorial = false,
                        displayLockTutorial = true,
                        closeLockTutorial = {},
                    ) {}
                }
            }
        }
    }
}
