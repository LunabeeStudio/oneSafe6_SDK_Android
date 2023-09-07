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

package studio.lunabee.onesafe.bubbles.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.bubbles.ui.contact.EmptyContactsScreen
import studio.lunabee.onesafe.bubbles.ui.contact.FilledContactsScreen
import studio.lunabee.onesafe.bubbles.ui.conversation.AppEmptyConversationScreen
import studio.lunabee.onesafe.bubbles.ui.conversation.AppFilledConversationScreen
import studio.lunabee.onesafe.bubbles.ui.home.model.BubblesTabsData
import studio.lunabee.onesafe.bubbles.ui.model.BubblesConversationInfo
import studio.lunabee.onesafe.bubbles.ui.model.UIBubblesContactInfo
import studio.lunabee.onesafe.commonui.R
import studio.lunabee.onesafe.commonui.action.TopAppBarOptionNavBack
import studio.lunabee.onesafe.model.TopAppBarOptionTrailing
import studio.lunabee.onesafe.molecule.OSTopAppBar
import studio.lunabee.onesafe.molecule.tabs.OSTabs
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem
import studio.lunabee.onesafe.ui.theme.OSPreviewOnSurfaceTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview
import java.util.UUID

context(BubblesHomeNavScope)
@Composable
fun BubblesHomeRoute(
    viewModel: BubblesHomeScreenViewModel = hiltViewModel(),
) {
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()
    val conversation: List<BubblesConversationInfo>? by viewModel.conversation.collectAsStateWithLifecycle()
    BubblesHomeScreen(
        contacts = contacts,
        conversation = conversation,
        isOSKShown = viewModel.osFeatureFlags.oneSafeK(),
        initialTab = viewModel.initialTab,
    )
}

context(BubblesHomeNavScope)
@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun BubblesHomeScreen(
    contacts: List<UIBubblesContactInfo>?,
    conversation: List<BubblesConversationInfo>?,
    isOSKShown: Boolean,
    initialTab: BubblesHomeTab?,
) {
    var currentPage by rememberSaveable(contacts, initialTab) {
        val tabIdx = initialTab?.ordinal ?: if (contacts?.isEmpty() == true) {
            BubblesHomeTab.Contacts.ordinal
        } else {
            BubblesHomeTab.Conversations.ordinal
        }
        mutableIntStateOf(tabIdx)
    }
    val pagerState = rememberPagerState(
        initialPage = initialTab?.ordinal ?: 0,
        initialPageOffsetFraction = 0f,
        pageCount = { BubblesHomeTab.entries.size },
    )
    LaunchedEffect(currentPage) {
        pagerState.animateScrollToPage(currentPage)
    }
    LaunchedEffect(pagerState.currentPage) {
        currentPage = pagerState.currentPage
    }

    OSScreen(
        testTag = UiConstants.TestTag.Screen.BubblesHomeScreen,
        background = LocalDesignSystem.current.bubblesBackGround(),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            OSTopAppBar(
                title = LbcTextSpec.StringResource(R.string.bubbles_title),
                options = listOf(
                    TopAppBarOptionNavBack(navigateBack),
                    TopAppBarOptionTrailing.secondaryIconAction(
                        image = OSImageSpec.Drawable(R.drawable.ic_add),
                        onClick = navigateToCreateContact,
                        contentDescription = LbcTextSpec.StringResource(R.string.bubbles_inviteContact),
                    ),
                ),
            )
            OSTabs(
                data = BubblesHomeTab.entries.map { entry ->
                    BubblesTabsData(
                        title = entry.title,
                        contentDescription = null,
                        hasNotification = if (entry == BubblesHomeTab.Conversations) {
                            conversation?.any { conv -> conv.hasUnreadMessage } == true
                        } else {
                            false
                        },
                    )
                },
                selectedTabIndex = currentPage,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = OSDimens.SystemSpacing.Small),
                onTabSelected = { idx -> currentPage = idx },
            )
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { pageNumber ->
                when (pageNumber) {
                    BubblesHomeTab.Conversations.ordinal -> {
                        if (contacts?.isEmpty() == true) {
                            AppEmptyConversationScreen(
                                modifier = Modifier.testTag(UiConstants.TestTag.Screen.BubblesHomeScreenConversationTab),
                            )
                        } else {
                            AppFilledConversationScreen(
                                modifier = Modifier.testTag(UiConstants.TestTag.Screen.BubblesHomeScreenConversationTab),
                                contacts = conversation ?: listOf(),
                                onConversationClick = navigateToConversation,
                                onDecryptClick = navigateToDecryptMessage,
                                onSettingClick = navigateToBubblesSettings,
                                isOSKShown = isOSKShown,
                            )
                        }
                    }
                    BubblesHomeTab.Contacts.ordinal -> {
                        if (contacts?.isEmpty() == true) {
                            EmptyContactsScreen(
                                modifier = Modifier.testTag(UiConstants.TestTag.Screen.BubblesHomeScreenContactTab),
                                onAddContactClick = navigateToCreateContact,
                                onScanClick = navigateToScanBarcode,
                            )
                        } else {
                            FilledContactsScreen(
                                modifier = Modifier.testTag(UiConstants.TestTag.Screen.BubblesHomeScreenContactTab),
                                onAddContactClick = navigateToCreateContact,
                                onScanClick = navigateToScanBarcode,
                                contacts = contacts ?: listOf(),
                                onContactClick = navigateToContactDetail,
                            )
                        }
                    }
                }
            }
        }
    }
}

interface BubblesHomeNavScope {
    val navigateToBubblesSettings: () -> Unit
    val navigateToConversation: (contactId: UUID) -> Unit
    val navigateBack: () -> Unit
    val navigateToContactDetail: (contactId: UUID) -> Unit
    val navigateToCreateContact: () -> Unit
    val navigateToScanBarcode: () -> Unit
    val navigateToDecryptMessage: () -> Unit
}

@OsDefaultPreview
@Composable
private fun BubblesHomeScreenPreview() {
    OSPreviewOnSurfaceTheme {
        val contacts: List<UIBubblesContactInfo> = emptyList()
        val conversation: List<BubblesConversationInfo> = emptyList()
        val isOSKShown = true
        val initialTab: BubblesHomeTab = BubblesHomeTab.Contacts

        with(object : BubblesHomeNavScope {
            override val navigateToBubblesSettings: () -> Unit = {}
            override val navigateToConversation: (contactId: UUID) -> Unit = {}
            override val navigateBack: () -> Unit = {}
            override val navigateToContactDetail: (contactId: UUID) -> Unit = {}
            override val navigateToCreateContact: () -> Unit = {}
            override val navigateToScanBarcode: () -> Unit = {}
            override val navigateToDecryptMessage: () -> Unit = {}
        }) {
            BubblesHomeScreen(contacts, conversation, isOSKShown, initialTab)
        }
    }
}
