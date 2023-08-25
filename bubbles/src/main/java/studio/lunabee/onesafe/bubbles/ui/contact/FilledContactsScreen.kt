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
 * Created by Lunabee Studio / Date - 7/12/2023 - for the oneSafe6 SDK.
 * Last modified 12/07/2023 10:53
 */

package studio.lunabee.onesafe.bubbles.ui.contact

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSCard
import studio.lunabee.onesafe.atom.OSClickableRow
import studio.lunabee.onesafe.atom.OSIconDecorationButton
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.button.defaults.OSTextButtonDefaults
import studio.lunabee.onesafe.atom.lazyVerticalOSRegularSpacer
import studio.lunabee.onesafe.bubbles.ui.model.BubblesContactInfo
import studio.lunabee.onesafe.bubbles.ui.onesafek.SelectContactFactory
import studio.lunabee.onesafe.commonui.R
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem
import java.util.UUID

@Composable
fun FilledContactsScreen(
    onAddContactClick: () -> Unit,
    onScanClick: () -> Unit,
    contacts: List<BubblesContactInfo>,
    onContactClick: (UUID) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(OSDimens.SystemSpacing.Regular),
    ) {
        item {
            OSCard(
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                OSClickableRow(
                    text = LbcTextSpec.StringResource(R.string.bubbles_inviteContact),
                    onClick = onAddContactClick,
                    buttonColors = OSTextButtonDefaults.secondaryTextButtonColors(state = OSActionState.Enabled),
                    leadingIcon = { OSIconDecorationButton(image = OSImageSpec.Drawable(drawable = R.drawable.ic_add)) },
                    contentPadding = LocalDesignSystem.current.getRowClickablePaddingValuesDependingOnIndex(
                        index = 0,
                        elementsCount = 2,
                    ),
                )
                OSClickableRow(
                    text = LbcTextSpec.StringResource(R.string.bubbles_scanQRCode),
                    onClick = onScanClick,
                    leadingIcon = { OSIconDecorationButton(image = OSImageSpec.Drawable(drawable = R.drawable.ic_qr_scanner)) },
                    contentPadding = LocalDesignSystem.current.getRowClickablePaddingValuesDependingOnIndex(
                        index = 1,
                        elementsCount = 2,
                    ),
                )
            }
        }
        lazyVerticalOSRegularSpacer()
        SelectContactFactory.addContacts(
            contacts = contacts,
            onClick = onContactClick,
            lazyListScope = this,
        )
    }
}