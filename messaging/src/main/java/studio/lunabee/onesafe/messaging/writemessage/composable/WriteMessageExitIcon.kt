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
 * Created by Lunabee Studio / Date - 6/14/2023 - for the oneSafe6 SDK.
 * Last modified 6/14/23, 2:49 PM
 */

package studio.lunabee.onesafe.messaging.writemessage.composable

import androidx.compose.runtime.Composable
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.button.OSIconButton
import studio.lunabee.onesafe.atom.button.defaults.OSIconButtonDefaults
import studio.lunabee.onesafe.commonui.ResourcesLibrary
import studio.lunabee.onesafe.messaging.R
import studio.lunabee.onesafe.ui.res.OSDimens

interface WriteMessageExitIcon {
    @Composable
    fun Content()

    class WriteMessageCloseIcon(
        private val onClick: () -> Unit,
    ) : WriteMessageExitIcon {
        @Composable
        override fun Content() {
            OSIconButton(
                image = OSImageSpec.Drawable(ResourcesLibrary.icClose),
                onClick = onClick,
                buttonSize = OSDimens.SystemButtonDimension.NavBarAction,
                contentDescription = LbcTextSpec.StringResource(R.string.common_accessibility_back),
                colors = OSIconButtonDefaults.secondaryIconButtonColors(),
            )
        }
    }

    class WriteMessageBackIcon(
        private val onClick: () -> Unit,
    ) : WriteMessageExitIcon {
        @Composable
        override fun Content() {
            OSIconButton(
                image = OSImageSpec.Drawable(ResourcesLibrary.icBack),
                onClick = onClick,
                buttonSize = OSDimens.SystemButtonDimension.NavBarAction,
                contentDescription = LbcTextSpec.StringResource(R.string.common_accessibility_back),
                colors = OSIconButtonDefaults.secondaryIconButtonColors(),
            )
        }
    }
}
