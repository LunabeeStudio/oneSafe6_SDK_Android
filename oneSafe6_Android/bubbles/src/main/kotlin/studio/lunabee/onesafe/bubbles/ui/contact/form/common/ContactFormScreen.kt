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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.atom.lazyVerticalOSRegularSpacer
import studio.lunabee.onesafe.bubbles.ui.contact.composables.ModeMessageShared
import studio.lunabee.onesafe.bubbles.ui.contact.model.MessageSharingModeUi
import studio.lunabee.onesafe.commonui.action.topAppBarOptionNavBack
import studio.lunabee.onesafe.model.OSItemIllustration
import studio.lunabee.onesafe.molecule.OSTopAppBar
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun ContactFormScreen(
    type: ContactFormType,
    onBackClick: () -> Unit,
    icon: OSItemIllustration,
    onInviteClick: () -> Unit,
    onSharingMessageModeChange: (MessageSharingModeUi) -> Unit,
    sharingMessageMode: MessageSharingModeUi,
    contactName: String,
    onContactNameChange: (String) -> Unit,
) {
    var bottomSheetMessageSharingIsVisible by rememberSaveable { mutableStateOf(false) }

    OSScreen(testTag = UiConstants.TestTag.Screen.CreateContactScreen) {
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
                    ModeMessageShared(
                        sharingModeUi = sharingMessageMode,
                        onSharingMessageModeChange = onSharingMessageModeChange,
                        isVisible = bottomSheetMessageSharingIsVisible,
                        onVisibleChange = { bottomSheetMessageSharingIsVisible = !bottomSheetMessageSharingIsVisible },
                    )
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

@OsDefaultPreview
@Composable
fun ContactFormScreenPreview() {
    OSTheme {
        ContactFormScreen(
            type = ContactFormType.FromScratch,
            onBackClick = {},
            icon = OSItemIllustration.Text(LbcTextSpec.Raw("Toto"), color = null),
            onInviteClick = {},
            onSharingMessageModeChange = {},
            sharingMessageMode = MessageSharingModeUi.Deeplinks,
            contactName = "",
            onContactNameChange = {},
        )
    }
}
