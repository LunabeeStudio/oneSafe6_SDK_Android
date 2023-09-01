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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.animation.OSTopBarAnimatedVisibility
import studio.lunabee.onesafe.animation.rememberOSTopBarVisibilityNestedScrollConnection
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.atom.lazyVerticalOSRegularSpacer
import studio.lunabee.onesafe.bubbles.ui.contact.composables.DeeplinkSwitchRow
import studio.lunabee.onesafe.commonui.R
import studio.lunabee.onesafe.commonui.action.TopAppBarOptionNavBack
import studio.lunabee.onesafe.commonui.action.topAppBarOptionEdit
import studio.lunabee.onesafe.commonui.dialog.DefaultAlertDialog
import studio.lunabee.onesafe.messaging.domain.model.ConversationState
import studio.lunabee.onesafe.molecule.OSTopAppBar
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem
import studio.lunabee.onesafe.ui.theme.OSUserTheme
import java.util.UUID

@Composable
fun ContactDetailRoute(
    navigateBack: () -> Unit,
    navigateToConversation: (UUID) -> Unit,
    navigateToInvitationScreen: (UUID) -> Unit,
    navigateToResponseScreen: (UUID) -> Unit,
    navigateToScanBarcodeScreen: () -> Unit,
    navigateBackToBubbles: () -> Unit,
    navigateToContactEdition: (UUID) -> Unit,
    viewModel: ContactDetailViewModel = hiltViewModel(),
) {
    val dialogState by viewModel.dialogState.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    dialogState?.DefaultAlertDialog()
    when (val safeUiState = uiState) {
        is ContactDetailUiState.Data -> {
            ContactDetailScreen(
                onBackClick = navigateBack,
                onConversationClick = { navigateToConversation(viewModel.contactId) },
                uiState = safeUiState,
                onRemoveClick = viewModel::deleteContact,
                onIsUsingDeepLinkChange = viewModel::updateIsUsingDeeplink,
                conversationState = safeUiState.conversationState,
                onResendInvitationClick = { navigateToInvitationScreen(viewModel.contactId) },
                onResendResponseClick = { navigateToResponseScreen(viewModel.contactId) },
                onScanResponseClick = navigateToScanBarcodeScreen,
                onEditClick = { navigateToContactEdition(viewModel.contactId) },
            )
        }
        is ContactDetailUiState.Idle -> OSScreen(
            testTag = "",
            background = LocalDesignSystem.current.bubblesBackGround(),
        ) { Box(modifier = Modifier.fillMaxSize()) }
        is ContactDetailUiState.Exit -> {
            LaunchedEffect(Unit) { navigateBackToBubbles() }
            OSScreen(
                testTag = "",
                background = LocalDesignSystem.current.bubblesBackGround(),
            ) { Box(modifier = Modifier.fillMaxSize()) }
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
    onIsUsingDeepLinkChange: (Boolean) -> Unit,
    onScanResponseClick: () -> Unit,
    conversationState: ConversationState,
) {
    val lazyListState: LazyListState = rememberLazyListState()
    val nestedScrollConnection = rememberOSTopBarVisibilityNestedScrollConnection(lazyListState)
    OSUserTheme(customPrimaryColor = uiState.color) {
        OSScreen(
            testTag = UiConstants.TestTag.Screen.ContactDetailScreen,
            background = LocalDesignSystem.current.bubblesBackGround(),
        ) {
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
                )
                lazyVerticalOSRegularSpacer()
                item {
                    DeeplinkSwitchRow(onValueChange = onIsUsingDeepLinkChange, isChecked = uiState.isDeeplinkActivated)
                }
                lazyVerticalOSRegularSpacer()
                ContactDetailScreenFactory.removeContactCard(onClick = onRemoveClick, lazyListScope = this)
            }
            OSTopBarAnimatedVisibility(visible = nestedScrollConnection.isTopBarVisible) {
                OSTopAppBar(
                    modifier = Modifier
                        .testTag(UiConstants.TestTag.Item.ItemDetailsTopBar)
                        .statusBarsPadding()
                        .align(Alignment.TopCenter),
                    options = listOfNotNull(
                        TopAppBarOptionNavBack(onBackClick),
                        topAppBarOptionEdit(
                            description = LbcTextSpec.StringResource(R.string.bubbles_contactDetail_editAction),
                            onEditItemClick = onEditClick,
                        ),
                    ),
                )
            }
        }
    }
}
