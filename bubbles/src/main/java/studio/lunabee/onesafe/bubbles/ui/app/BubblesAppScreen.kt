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
 * Created by Lunabee Studio / Date - 7/11/2023 - for the oneSafe6 SDK.
 * Last modified 11/07/2023 11:02
 */

package studio.lunabee.onesafe.bubbles.ui.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.bubbles.ui.contact.FilledContactsScreen
import studio.lunabee.onesafe.bubbles.ui.contact.detail.EmptyContactsScreen
import studio.lunabee.onesafe.bubbles.ui.conversation.AppEmptyConversationScreen
import studio.lunabee.onesafe.bubbles.ui.conversation.AppFilledConversationScreen
import studio.lunabee.onesafe.bubbles.ui.model.BubblesConversationInfo
import studio.lunabee.onesafe.commonui.R
import studio.lunabee.onesafe.commonui.action.TopAppBarOptionNavBack
import studio.lunabee.onesafe.molecule.OSTabs
import studio.lunabee.onesafe.molecule.OSTopAppBar
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem
import java.util.UUID

@Composable
fun BubblesAppRoute(
    navigateBack: () -> Unit,
    navigateToContact: (contactId: UUID) -> Unit,
    navigateToConversation: (contactId: UUID) -> Unit,
    navigateToQrScan: () -> Unit,
    navigateToCreateContact: () -> Unit,
    navigateToSettings: () -> Unit,
    navigateToDecryptMessage: () -> Unit,
    viewModel: BubblesAppScreenViewModel = hiltViewModel(),
) {
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()
    val conversation: List<BubblesConversationInfo>? by viewModel.conversation.collectAsStateWithLifecycle()
    val tabs = remember { BubblesTab.values().toList() }
    var selectedTab by rememberSaveable(contacts) {
        mutableStateOf(if (contacts?.isEmpty() == true) tabs[1] else tabs[0])
    }

    OSScreen(
        testTag = UiConstants.TestTag.Screen.BubbleScreen,
        background = LocalDesignSystem.current.bubblesBackGround(),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            OSTopAppBar(
                title = LbcTextSpec.StringResource(R.string.bubbles_title),
                options = listOf(TopAppBarOptionNavBack(navigateBack)),
            )
            OSTabs(
                titles = BubblesTab.values().map { it.title to it.title },
                selectedTabIndex = tabs.indexOf(selectedTab),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = OSDimens.SystemSpacing.Small),
                onTabSelected = { idx -> selectedTab = (tabs.elementAt(idx)) },
            )
            when (selectedTab) {
                BubblesTab.Conversation -> {
                    if (contacts?.isEmpty() == true) {
                        AppEmptyConversationScreen()
                    } else {
                        AppFilledConversationScreen(
                            contacts = conversation ?: listOf(),
                            onConversationClick = navigateToConversation,
                            onDecryptClick = navigateToDecryptMessage,
                            onSettingClick = navigateToSettings,
                            isOSKShown = viewModel.osFeatureFlags.oneSafeK(),
                        )
                    }
                }
                BubblesTab.Contacts -> {
                    if (contacts?.isEmpty() == true) {
                        EmptyContactsScreen(
                            onAddContactClick = navigateToCreateContact,
                            onScanClick = navigateToQrScan,
                        )
                    } else {
                        FilledContactsScreen(
                            onAddContactClick = navigateToCreateContact,
                            onScanClick = navigateToQrScan,
                            contacts = contacts ?: listOf(),
                            onContactClick = navigateToContact,
                        )
                    }
                }
            }
        }
    }
}

enum class BubblesTab(val title: LbcTextSpec) {
    Conversation(LbcTextSpec.StringResource(R.string.bubbles_conversations)),
    Contacts(LbcTextSpec.StringResource(R.string.bubbles_contacts)),
}
