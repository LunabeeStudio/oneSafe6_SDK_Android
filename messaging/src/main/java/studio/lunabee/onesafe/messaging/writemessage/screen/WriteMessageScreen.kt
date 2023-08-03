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

package studio.lunabee.onesafe.messaging.writemessage.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.atom.button.OSIconButton
import studio.lunabee.onesafe.atom.button.defaults.OSIconButtonDefaults
import studio.lunabee.onesafe.bubbles.ui.extension.getDeepLinkFromMessage
import studio.lunabee.onesafe.bubbles.ui.model.BubblesContactInfo
import studio.lunabee.onesafe.commonui.OSNameProvider
import studio.lunabee.onesafe.commonui.R
import studio.lunabee.onesafe.commonui.dialog.DefaultAlertDialog
import studio.lunabee.onesafe.commonui.localprovider.LocalKeyboardUiHeight
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.extension.landscapeSystemBarsPadding
import studio.lunabee.onesafe.extension.loremIpsum
import studio.lunabee.onesafe.messaging.domain.model.MessageDirection
import studio.lunabee.onesafe.messaging.writemessage.composable.ComposeMessageCard
import studio.lunabee.onesafe.messaging.writemessage.composable.ConversationDayHeader
import studio.lunabee.onesafe.messaging.writemessage.composable.ConversationNotReadyCard
import studio.lunabee.onesafe.messaging.writemessage.composable.WriteMessageExitIcon
import studio.lunabee.onesafe.messaging.writemessage.composable.WriteMessageTopBar
import studio.lunabee.onesafe.messaging.writemessage.destination.WriteMessageDestination
import studio.lunabee.onesafe.messaging.writemessage.factory.WriteMessageFactory
import studio.lunabee.onesafe.messaging.writemessage.model.ConversationUiData
import studio.lunabee.onesafe.messaging.writemessage.viewmodel.WriteMessageViewModel
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSTheme
import java.time.Instant
import java.util.UUID
import kotlin.random.Random

@Composable
fun WriteMessageRoute(
    onChangeRecipient: (() -> Unit)?,
    sendMessage: (String) -> Unit,
    navigationToInvitation: (UUID) -> Unit,
    exitIcon: WriteMessageExitIcon,
    viewModel: WriteMessageViewModel = hiltViewModel(),
    contactIdFlow: StateFlow<String?>,
    sendIcon: OSImageSpec,
) {
    val updateContactId by contactIdFlow.collectAsStateWithLifecycle()
    updateContactId?.let {
        viewModel.savedStateHandle[WriteMessageDestination.ContactIdArgs] = updateContactId
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dialogState by viewModel.dialogState.collectAsStateWithLifecycle()
    val conversation: LazyPagingItems<ConversationUiData> = viewModel.conversation.collectAsLazyPagingItems()
    val coroutineScope = rememberCoroutineScope()

    dialogState?.DefaultAlertDialog()
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        uiState.currentContact?.let { contact ->
            val context = LocalContext.current
            WriteMessageScreen(
                contact = contact,
                plainMessage = uiState.plainMessage,
                encryptedMessage = uiState.encryptedPreview,
                onChangeRecipient = onChangeRecipient,
                onPlainMessageChange = viewModel::onPlainMessageChange,
                sendMessage = {
                    coroutineScope.launch {
                        val messageToSend = viewModel.encryptAndSaveMessage(
                            content = uiState.plainMessage,
                            context = context,
                        )
                        messageToSend?.let {
                            sendMessage(it.getDeepLinkFromMessage(uiState.isUsingDeepLink))
                            viewModel.onPlainMessageChange("")
                        }
                    }
                },
                exitIcon = exitIcon,
                conversation = conversation,
                conversationError = uiState.conversationError,
                onResendInvitationClick = {
                    navigationToInvitation(UUID.fromString(viewModel.contactId.value))
                },
                sendIcon = sendIcon,
                onPreviewClick = {
                    viewModel.displayPreviewInfo()
                },
            )
        }
    }
}

@Composable
fun WriteMessageScreen(
    contact: BubblesContactInfo,
    plainMessage: String,
    encryptedMessage: String,
    onChangeRecipient: (() -> Unit)?,
    onResendInvitationClick: () -> Unit,
    onPlainMessageChange: (String) -> Unit,
    sendMessage: () -> Unit,
    conversation: LazyPagingItems<ConversationUiData>,
    exitIcon: WriteMessageExitIcon,
    sendIcon: OSImageSpec,
    conversationError: OSError?,
    onPreviewClick: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val embeddedKeyboardHeight: Dp = LocalKeyboardUiHeight.current
    val lazyListState = rememberLazyListState()
    var isConversationHidden: Boolean by rememberSaveable { mutableStateOf(false) }

    OSScreen(
        testTag = UiConstants.TestTag.Screen.OneSafeKSelectContactScreen,
        applySystemBarPadding = false,
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .then(
                    if (embeddedKeyboardHeight != 0.dp) {
                        Modifier.padding(bottom = embeddedKeyboardHeight)
                    } else {
                        Modifier.imePadding()
                    },
                ),
        ) {
            WriteMessageTopBar(
                exitIcon = exitIcon,
                contactNameProvider = contact.nameProvider,
                canChangeRecipient = onChangeRecipient != null,
                onClickOnChange = {
                    focusManager.clearFocus()
                    onChangeRecipient?.invoke()
                },
                modifier = Modifier
                    .statusBarsPadding()
                    .landscapeSystemBarsPadding()
                    .padding(
                        horizontal = OSDimens.SystemSpacing.Regular,
                        vertical = OSDimens.SystemSpacing.Small,
                    ),
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.TopEnd,
                ) {
                    when {
                        conversationError != null -> {
                            ConversationNotReadyCard(
                                contactName = contact.nameProvider.name.string,
                                onResendInvitationClick = onResendInvitationClick,
                            )
                        }
                        isConversationHidden || conversation.itemCount == 0 -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(bottom = OSDimens.SystemSpacing.Regular),
                                contentAlignment = Alignment.BottomCenter,
                            ) {
                                ConversationDayHeader(
                                    text = LbcTextSpec.StringResource(R.string.oneSafeK_messageDate_today),
                                )
                            }
                        }
                        else -> {
                            val context = LocalContext.current
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .landscapeSystemBarsPadding(),
                                contentPadding = generateLazyColumnPaddingValue(conversation.itemCount, isConversationHidden),
                                reverseLayout = true,
                                verticalArrangement = generateLazyColumnVerticalArrangement(
                                    conversation.itemCount,
                                    isConversationHidden,
                                ),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                state = lazyListState,
                            ) {
                                WriteMessageFactory.addPagingConversation(
                                    lazyListScope = this,
                                    conversation = conversation,
                                    contactNameProvider = contact.nameProvider,
                                    context = context,
                                )
                            }
                        }
                    }
                    if (conversationError == null) {
                        OSIconButton(
                            image = if (isConversationHidden) {
                                OSImageSpec.Drawable(R.drawable.ic_visibility_on)
                            } else {
                                OSImageSpec.Drawable(R.drawable.ic_visibility_off)
                            },
                            onClick = { isConversationHidden = !isConversationHidden },
                            buttonSize = OSDimens.SystemButtonDimension.Small,
                            contentDescription = null, // TODO @fngbala-luna
                            colors = OSIconButtonDefaults.primaryIconButtonColors(),
                            modifier = Modifier
                                .padding(end = OSDimens.SystemSpacing.Regular, top = OSDimens.SystemSpacing.Small)
                                .landscapeSystemBarsPadding()
                                .shadow(elevation = OSDimens.Elevation.FloatingButton, shape = CircleShape),
                        )
                    }
                }
            }
            if (conversationError == null) {
                ComposeMessageCard(
                    plainMessage = plainMessage,
                    encryptedMessage = encryptedMessage,
                    onPlainMessageChange = onPlainMessageChange,
                    onClickOnSend = sendMessage,
                    sendIcon = sendIcon,
                    onPreviewClick = onPreviewClick,
                )
            }
        }
    }
}

private fun generateLazyColumnPaddingValue(
    itemCount: Int,
    conversationIsHidden: Boolean,
): PaddingValues = PaddingValues(
    start = OSDimens.SystemSpacing.Regular,
    end = OSDimens.SystemSpacing.Regular,
    top = if (itemCount == 0) {
        OSDimens.SystemSpacing.ExtraLarge
    } else {
        OSDimens.SystemSpacing.Regular
    },
    bottom = if (conversationIsHidden) {
        OSDimens.SystemSpacing.None
    } else {
        OSDimens.SystemSpacing.Regular
    },
)

private fun generateLazyColumnVerticalArrangement(
    itemCount: Int,
    conversationIsHidden: Boolean,
): Arrangement.Vertical =
    when {
        conversationIsHidden -> Arrangement.Bottom
        itemCount == 0 -> Arrangement.SpaceBetween
        else -> Arrangement.spacedBy(
            space = OSDimens.SystemSpacing.Small,
            alignment = Alignment.Bottom,
        )
    }

@Preview
@Composable
fun WriteMessageScreenPreview() {
    OSTheme {
        val pagingItems: LazyPagingItems<ConversationUiData> = MutableStateFlow(
            PagingData.from(
                listOf<ConversationUiData>(
                    ConversationUiData.PlainMessageData(
                        id = "contact_${Random.nextInt()}",
                        text = LbcTextSpec.Raw("hello"),
                        direction = MessageDirection.RECEIVED,
                        sendAt = Instant.ofEpochSecond(0),
                        channelName = loremIpsum(1),
                    ),
                    ConversationUiData.PlainMessageData(
                        id = "contact_${Random.nextInt()}",
                        text = LbcTextSpec.Raw("hello hello"),
                        direction = MessageDirection.SENT,
                        sendAt = Instant.ofEpochSecond(0),
                        channelName = loremIpsum(1),
                    ),
                ),
            ),
        ).collectAsLazyPagingItems()

        WriteMessageScreen(
            contact = BubblesContactInfo(UUID.randomUUID(), OSNameProvider.fromName("A", false)),
            plainMessage = loremIpsum(10),
            encryptedMessage = loremIpsum(10),
            onChangeRecipient = {},
            onPlainMessageChange = {},
            sendMessage = {},
            conversation = pagingItems,
            exitIcon = WriteMessageExitIcon.WriteMessageCloseIcon {},
            conversationError = null,
            onResendInvitationClick = {},
            sendIcon = OSImageSpec.Drawable(studio.lunabee.onesafe.messaging.R.drawable.ic_send),
            onPreviewClick = {},
        )
    }
}
