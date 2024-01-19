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
 * Created by Lunabee Studio / Date - 8/23/2023 - for the oneSafe6 SDK.
 * Last modified 22/08/2023 15:21
 */

package studio.lunabee.onesafe.bubbles.ui.contact.form.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.atom.lazyVerticalOSRegularSpacer
import studio.lunabee.onesafe.bubbles.ui.contact.composables.DeeplinkSwitchRow
import studio.lunabee.onesafe.commonui.action.topAppBarOptionNavBack
import studio.lunabee.onesafe.model.OSItemIllustration
import studio.lunabee.onesafe.molecule.OSTopAppBar
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem

@Composable
fun ContactFormScreen(
    type: ContactFormType,
    onBackClick: () -> Unit,
    icon: OSItemIllustration,
    onInviteClick: () -> Unit,
    onDeeplinkChange: (Boolean) -> Unit,
    isDeeplinkChecked: Boolean,
    contactName: String,
    onContactNameChange: (String) -> Unit,
) {
    OSScreen(
        testTag = UiConstants.TestTag.Screen.CreateContactScreen,
        background = LocalDesignSystem.current.bubblesBackGround(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
        ) {
            OSTopAppBar(
                title = type.title,
                options = listOf(topAppBarOptionNavBack(onBackClick)),
            )
            LazyColumn(
                contentPadding = PaddingValues(OSDimens.SystemSpacing.Regular),
                modifier = Modifier.fillMaxSize(),
            ) {
                lazyVerticalOSRegularSpacer()
                CreateContactScreenFactory.header(iconProvider = icon, lazyListScope = this)
                lazyVerticalOSRegularSpacer()
                CreateContactScreenFactory.nameTextField(
                    contactName = contactName,
                    onContactNameChange = onContactNameChange,
                    lazyListScope = this,
                )
                lazyVerticalOSRegularSpacer()
                item {
                    DeeplinkSwitchRow(onValueChange = onDeeplinkChange, isChecked = isDeeplinkChecked)
                }
                lazyVerticalOSRegularSpacer()
                CreateContactScreenFactory.inviteButton(
                    lazyListScope = this,
                    onClick = onInviteClick,
                    label = type.buttonString,
                    isEnabled = contactName.isNotEmpty(),
                )
            }
        }
    }
}
