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
 * Last modified 12/07/2023 11:09
 */

package studio.lunabee.onesafe.bubbles.ui.contact.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.doubleratchet.model.createRandomUUID
import studio.lunabee.onesafe.animation.OSTopBarAnimatedVisibility
import studio.lunabee.onesafe.animation.rememberOSTopBarVisibilityNestedScrollConnection
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.atom.lazyVerticalOSRegularSpacer
import studio.lunabee.onesafe.bubbles.ui.contact.composables.ModeMessageShared
import studio.lunabee.onesafe.bubbles.ui.contact.detail.ContactDetailUiState.UIConversationState
import studio.lunabee.onesafe.bubbles.ui.contact.model.MessageSharingModeUi
import studio.lunabee.onesafe.commonui.OSNameProvider
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.action.topAppBarOptionEdit
import studio.lunabee.onesafe.commonui.action.topAppBarOptionNavBack
import studio.lunabee.onesafe.commonui.dialog.DefaultAlertDialog
import studio.lunabee.onesafe.extension.loremIpsum
import studio.lunabee.onesafe.molecule.OSTopAppBar
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme
import studio.lunabee.onesafe.ui.theme.OSUserTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview
import java.util.UUID

context(ContactDetailNavScope)
@Composable
fun ContactDetailRoute(
    viewModel: ContactDetailViewModel = hiltViewModel(),
) {
    val dialogState by viewModel.dialogState.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    dialogState?.DefaultAlertDialog()

    when (val safeUiState = uiState) {
        is ContactDetailUiState.Data -> {
            ContactDetailScreen(
                onBackClick = navigateBack,
                onConversationClick = { navigateToConversation(viewModel.contactId.uuid) },
                uiState = safeUiState,
                onRemoveClick = viewModel::deleteContact,
                onMessageSharingModeChange = viewModel::updateSharingModeUi,
                conversationState = safeUiState.conversationState,
                onResendInvitationClick = { navigateToInvitation(viewModel.contactId.uuid) },
                onResendResponseClick = { navigateToResponse(viewModel.contactId.uuid) },
                onScanResponseClick = navigateToScanBarcode,
                onEditClick = { navigateToContactEdition(viewModel.contactId.uuid) },
                onResetConversation = viewModel::resetContactConversation,
            )
        }
        is ContactDetailUiState.Idle -> OSScreen(testTag = "") {
            Box(modifier = Modifier.fillMaxSize())
        }

        is ContactDetailUiState.Exit -> {
            LaunchedEffect(Unit) { navigateBackToBubbles() }
            OSScreen(testTag = "") {
                Box(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
fun ContactDetailScreen(
    onBackClick: () -> Unit,
    onConversationClick: () -> Unit,
    onResendInvitationClick: () -> Unit,
    onResendResponseClick: () -> Unit,
    onRemoveClick: () -> Unit,
    onEditClick: () -> Unit,
    uiState: ContactDetailUiState.Data,
    onMessageSharingModeChange: (MessageSharingModeUi) -> Unit,
    onResetConversation: () -> Unit,
    onScanResponseClick: () -> Unit,
    conversationState: UIConversationState,
) {
    val lazyListState: LazyListState = rememberLazyListState()
    val nestedScrollConnection = rememberOSTopBarVisibilityNestedScrollConnection(lazyListState)
    var bottomSheetMessageSharingIsVisible by rememberSaveable { mutableStateOf(false) }

    OSUserTheme(customPrimaryColor = uiState.color) {
        OSScreen(testTag = UiConstants.TestTag.Screen.ContactDetailScreen) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                state = lazyListState,
                contentPadding = PaddingValues(
                    start = OSDimens.SystemSpacing.Regular,
                    end = OSDimens.SystemSpacing.Regular,
                    top = OSDimens.ItemTopBar.Height,
                ),
            ) {
                ContactDetailScreenFactory.title(nameProvider = uiState.nameProvider, lazyListScope = this)
                ContactDetailScreenFactory.conversationStateCard(
                    conversationState = conversationState,
                    contactName = uiState.nameProvider,
                    lazyListScope = this,
                )
                lazyVerticalOSRegularSpacer()
                ContactDetailScreenFactory.actionCard(
                    conversationState = conversationState,
                    onConversationClick = onConversationClick,
                    onResendInvitationClick = onResendInvitationClick,
                    onResendResponseClick = onResendResponseClick,
                    onScanResponseClick = onScanResponseClick,
                    lazyListScope = this,
                    onResetConversation = onResetConversation,
                )
                lazyVerticalOSRegularSpacer()
                item {
                    ModeMessageShared(
                        sharingModeUi = uiState.messageSharingModeUi,
                        onSharingMessageModeChange = onMessageSharingModeChange,
                        isVisible = bottomSheetMessageSharingIsVisible,
                        onVisibleChange = { bottomSheetMessageSharingIsVisible = !bottomSheetMessageSharingIsVisible },
                    )
                }
                lazyVerticalOSRegularSpacer()
                ContactDetailScreenFactory.removeContactCard(onClick = onRemoveClick, lazyListScope = this)
            }
            OSTopBarAnimatedVisibility(visible = nestedScrollConnection.isTopBarVisible) {
                val appBarOptions = buildList {
                    this += topAppBarOptionNavBack(onBackClick)
                    if (conversationState != UIConversationState.Indecipherable) {
                        this += topAppBarOptionEdit(
                            description = LbcTextSpec.StringResource(OSString.bubbles_contactDetail_editAction),
                            onEditItemClick = onEditClick,
                        )
                    }
                }
                OSTopAppBar(
                    modifier = Modifier
                        .testTag(UiConstants.TestTag.Item.ItemDetailsTopBar)
                        .statusBarsPadding()
                        .align(Alignment.TopCenter),
                    options = appBarOptions,
                )
            }
        }
    }
}

interface ContactDetailNavScope {
    val navigateBack: () -> Unit
    val navigateToConversation: (UUID) -> Unit
    val navigateToInvitation: (UUID) -> Unit
    val navigateToResponse: (UUID) -> Unit
    val navigateToScanBarcode: () -> Unit
    val navigateToContactEdition: (UUID) -> Unit
    val navigateBackToBubbles: () -> Unit
}

@Composable
@OsDefaultPreview
fun ContactDetailScreenDataPreview() {
    OSPreviewBackgroundTheme {
        ContactDetailScreen(
            onBackClick = {},
            onConversationClick = {},
            onResendInvitationClick = {},
            onResendResponseClick = {},
            onRemoveClick = {},
            onEditClick = {},
            uiState = ContactDetailUiState.Data(
                id = createRandomUUID(),
                nameProvider = OSNameProvider.fromName(loremIpsum(1), false),
                messageSharingModeUi = MessageSharingModeUi.Deeplinks,
                conversationState = UIConversationState.Running,
                color = null,
            ),
            onMessageSharingModeChange = {},
            onScanResponseClick = {},
            conversationState = UIConversationState.Running,
            onResetConversation = {},
        )
    }
}

@Composable
@OsDefaultPreview
fun ContactDetailScreenCorruptedPreview() {
    OSPreviewBackgroundTheme {
        ContactDetailScreen(
            onBackClick = {},
            onConversationClick = {},
            onResendInvitationClick = {},
            onResendResponseClick = {},
            onRemoveClick = {},
            onEditClick = {},
            uiState = ContactDetailUiState.Data(
                id = createRandomUUID(),
                nameProvider = OSNameProvider.fromName(loremIpsum(1), false),
                messageSharingModeUi = MessageSharingModeUi.Deeplinks,
                conversationState = UIConversationState.Running,
                color = null,
            ),
            onMessageSharingModeChange = {},
            onScanResponseClick = {},
            conversationState = UIConversationState.Indecipherable,
            onResetConversation = {},
        )
    }
}
