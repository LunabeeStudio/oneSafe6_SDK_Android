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
 * Created by Lunabee Studio / Date - 9/4/2023 - for the oneSafe6 SDK.
 * Last modified 04/09/2023 09:36
 */

package studio.lunabee.onesafe.bubbles.ui.home.model

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSSmallSpacer
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.bubbles.ui.composables.NotificationIndicator
import studio.lunabee.onesafe.molecule.tabs.TabsData
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalColorPalette
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem
import studio.lunabee.onesafe.ui.theme.OSTypography

class BubblesTabsData(
    val title: LbcTextSpec,
    val contentDescription: LbcTextSpec?,
    private val hasNotification: Boolean,
) : TabsData(
        title,
        contentDescription,
    ) {
    @Composable
    override fun Composable(index: Int, selectedTabIndex: Int, isEnabled: Boolean) {
        val titleDescription = contentDescription?.string
        val textModifier = if (titleDescription != null) {
            Modifier.semantics { contentDescription = titleDescription }
        } else {
            Modifier
        }
        Row(
            modifier = Modifier.padding(OSDimens.SystemSpacing.Regular),
        ) {
            OSText(
                modifier = textModifier,
                text = title,
                style = OSTypography.Typography.labelLarge,
                color = when {
                    isEnabled -> Color.Unspecified
                    index == selectedTabIndex -> LocalDesignSystem.current.tabPrimaryDisabledColor
                    else -> LocalColorPalette.current.neutral30
                },
            )
            if (hasNotification) {
                OSSmallSpacer()
                NotificationIndicator()
            }
        }
    }
}
