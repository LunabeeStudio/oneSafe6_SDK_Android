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
 * Created by Lunabee Studio / Date - 11/15/2023 - for the oneSafe6 SDK.
 * Last modified 15/11/2023 14:11
 */

package studio.lunabee.onesafe.ime.ui.tutorial

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSCard
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.OSRegularSpacer
import studio.lunabee.onesafe.atom.button.OSIconButton
import studio.lunabee.onesafe.atom.button.defaults.OSIconButtonDefaults
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem

@Composable
fun ImeTutorialCard(
    description: LbcTextSpec,
    modifier: Modifier = Modifier,
    title: LbcTextSpec,
    onClose: () -> Unit,
) {
    OSCard(
        modifier = modifier,
    ) {
        OSRegularSpacer()
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OSText(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = OSDimens.SystemSpacing.Regular),
            )
            OSIconButton(
                image = OSImageSpec.Drawable(OSDrawable.ic_close),
                onClick = onClose,
                buttonSize = OSDimens.SystemButtonDimension.FloatingAction,
                contentDescription = LbcTextSpec.StringResource(OSString.common_close),
                colors = OSIconButtonDefaults.iconButtonColors(
                    containerColor = LocalDesignSystem.current.bubblesSecondaryContainer(),
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    state = OSActionState.Enabled,
                ),
                modifier = Modifier.padding(horizontal = OSDimens.SystemSpacing.Regular),
            )
        }
        OSRegularSpacer()
        OSText(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = OSDimens.SystemSpacing.Regular),
        )
        OSRegularSpacer()
    }
}
