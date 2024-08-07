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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import studio.lunabee.bubbles.domain.model.MessageSharingMode
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.doubleratchet.model.createRandomUUID
import studio.lunabee.messaging.domain.model.MessageDirection
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.atom.button.OSIconButton
import studio.lunabee.onesafe.atom.button.defaults.OSIconButtonDefaults
import studio.lunabee.onesafe.bubbles.ui.extension.getDeepLinkFromMessage
import studio.lunabee.onesafe.commonui.ErrorNameProvider
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSNameProvider
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.dialog.DefaultAlertDialog
import studio.lunabee.onesafe.commonui.dialog.ImeDialog
import studio.lunabee.onesafe.commonui.localprovider.LocalIsOneSafeK
import studio.lunabee.onesafe.commonui.snackbar.SnackbarState
import studio.lunabee.onesafe.extension.landscapeSystemBarsPadding
import studio.lunabee.onesafe.extension.loremIpsum
import studio.lunabee.onesafe.messaging.writemessage.composable.ComposeMessageCard
import studio.lunabee.onesafe.messaging.writemessage.composable.ConversationDayHeader
import studio.lunabee.onesafe.messaging.writemessage.composable.ConversationNotReadyCard
import studio.lunabee.onesafe.messaging.writemessage.composable.DropDownMenuMessageLongPress
import studio.lunabee.onesafe.messaging.writemessage.composable.MessageLongPress
import studio.lunabee.onesafe.messaging.writemessage.composable.NoPreviewComposeMessageCard
import studio.lunabee.onesafe.messaging.writemessage.composable.topbar.ContactActionMenu
import studio.lunabee.onesafe.messaging.writemessage.composable.topbar.WriteMessageTopBar
import studio.lunabee.onesafe.messaging.writemessage.destination.WriteMessageDestination
import studio.lunabee.onesafe.messaging.writemessage.factory.WriteMessageFactory
import studio.lunabee.onesafe.messaging.writemessage.model.BubblesWritingMessage
import studio.lunabee.onesafe.messaging.writemessage.model.ConversationMoreOptionsSnackbarState
import studio.lunabee.onesafe.messaging.writemessage.model.ConversationUiData
import studio.lunabee.onesafe.messaging.writemessage.model.SentMessageData
import studio.lunabee.onesafe.messaging.writemessage.viewmodel.WriteMessageViewModel
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem
import studio.lunabee.onesafe.ui.theme.OSTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview
import java.time.Instant
import java.util.UUID

context(WriteMessageNavScope)
@Composable
fun WriteMessageRoute(
    onChangeRecipient: (() -> Unit)?,
    sendMessage: (data: SentMessageData, messageToSend: String, sharingMode: MessageSharingMode) -> Unit,
    resendMessage: (String, MessageSharingMode) -> Unit,
    contactIdFlow: StateFlow<String?>,
    sendIcon: OSImageSpec,
    viewModel: WriteMessageViewModel = hiltViewModel(),
    hideKeyboard: (() -> Unit)?,
) {
    val updateContactId by contactIdFlow.collectAsStateWithLifecycle()
    updateContactId?.let {
        viewModel.savedStateHandle[WriteMessageDestination.ContactIdArg] = updateContactId
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dialogState by viewModel.dialogState.collectAsStateWithLifecycle()
    val conversation: LazyPagingItems<ConversationUiData> = viewModel.conversation.collectAsLazyPagingItems()
    val coroutineScope = rememberCoroutineScope()
    val isOneSafeK = LocalIsOneSafeK.current
    val oneSafeKSnackbarHostState = remember { SnackbarHostState() }
    val viewModelSnackBarHostState = remember { SnackbarHostState() }
    var snackbarState: ConversationMoreOptionsSnackbarState? by remember { mutableStateOf(null) }
    val viewModelSnackbarState: SnackbarState? by viewModel.snackbarState.collectAsStateWithLifecycle(initialValue = null)
    val composeMessageFocusRequester = remember { FocusRequester() }

    val viewModelSnackbarStateVisual = viewModelSnackbarState?.snackbarVisuals
    LaunchedEffect(viewModelSnackbarStateVisual) {
        viewModelSnackbarStateVisual?.let {
            viewModelSnackBarHostState.showSnackbar(it)
            viewModel.resetSnackbarState()
        }
    }

    when (val state = uiState) {
        WriteMessageUiState.Initializing -> Box(modifier = Modifier.fillMaxSize())
        is WriteMessageUiState.Data -> {
            val messageLongPress: MessageLongPress by remember(isOneSafeK) {
                mutableStateOf(
                    if (isOneSafeK) {
                        object : MessageLongPress() {
                            override fun onLongClick(id: UUID) {
                                deeplinkBubblesWriteMessage?.let { deeplink ->
                                    snackbarState = ConversationMoreOptionsSnackbarState {
                                        deeplink(viewModel.contactId.value!!.uuid)
                                    }
                                }
                            }
                        }
                    } else {
                        DropDownMenuMessageLongPress(
                            onResendClick = { sentMessageId ->
                                coroutineScope.launch {
                                    viewModel.getSentMessage(DoubleRatchetUUID(sentMessageId))?.let {
                                        resendMessage(it, state.messageSharingMode)
                                    }
                                }
                            },
                            onDeleteMessageClick = {
                                viewModel.deleteMessage(DoubleRatchetUUID(it))
                            },
                        )
                    },
                )
            }
            if (isOneSafeK) {
                dialogState?.let { dialog ->
                    dialog.ImeDialog {
                        composeMessageFocusRequester.requestFocus()
                    }
                    LaunchedEffect(dialog, hideKeyboard) {
                        hideKeyboard?.invoke()
                    }
                }
                snackbarState?.SnackBar(oneSafeKSnackbarHostState)
            } else {
                dialogState?.DefaultAlertDialog()
            }

            Box(
                modifier = Modifier.fillMaxSize(),
            ) {
                if (isOneSafeK) {
                    SnackbarHost(
                        hostState = oneSafeKSnackbarHostState,
                        modifier = Modifier
                            .zIndex(UiConstants.SnackBar.ZIndex)
                            .align(Alignment.BottomCenter),
                    )
                }

                SnackbarHost(
                    hostState = viewModelSnackBarHostState,
                    modifier = Modifier
                        .imePadding()
                        .zIndex(UiConstants.SnackBar.ZIndex)
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding(),
                )

                WriteMessageScreen(
                    nameProvider = state.nameProvider,
                    message = state.message,
                    onContactNameClick = onChangeRecipient ?: { navigateToContactDetail(state.contactId.uuid) },
                    onResendInvitationClick = {
                        navigationToInvitation(viewModel.contactId.value!!.uuid)
                    },
                    onPlainMessageChange = viewModel::onPlainMessageChange,
                    sendMessage = { // TODO <bubbles> fix flooding send button trigger many share sheets
                        coroutineScope.launch {
                            val sentMessageData = viewModel.getSentMessageData(content = state.message.plainMessage.text)
                            sentMessageData?.let {
                                sendMessage(
                                    sentMessageData,
                                    sentMessageData.encMessage.getDeepLinkFromMessage(state.messageSharingMode),
                                    state.messageSharingMode,
                                )
                            }
                        }
                    },
                    conversation = conversation,
                    onBackClick = navigateBack,
                    sendIcon = sendIcon,
                    isConversationReady = state.isConversationReady,
                    onPreviewClick = {
                        viewModel.displayPreviewInfo()
                    },
                    onDeleteAllMessagesClick = viewModel::displayRemoveConversationDialog,
                    isOneSafeK = isOneSafeK,
                    messageLongPress = messageLongPress,
                    focusRequester = composeMessageFocusRequester,
                    canSend = !state.isCorrupted,
                )
            }
        }
    }
}

@Composable
fun WriteMessageScreen(
    nameProvider: OSNameProvider,
    message: BubblesWritingMessage,
    onContactNameClick: () -> Unit,
    onResendInvitationClick: () -> Unit,
    onPlainMessageChange: (TextFieldValue) -> Unit,
    sendMessage: () -> Unit,
    conversation: LazyPagingItems<ConversationUiData>,
    onBackClick: () -> Unit,
    sendIcon: OSImageSpec,
    isConversationReady: Boolean,
    onPreviewClick: () -> Unit,
    onDeleteAllMessagesClick: () -> Unit,
    isOneSafeK: Boolean,
    messageLongPress: MessageLongPress,
    focusRequester: FocusRequester,
    canSend: Boolean,
) {
    val focusManager = LocalFocusManager.current
    val lazyListState = rememberLazyListState()
    var isConversationHidden: Boolean by rememberSaveable { mutableStateOf(false) }

    OSScreen(
        testTag = UiConstants.TestTag.Screen.WriteMessageScreen,
        background = if (isOneSafeK) {
            LocalDesignSystem.current.bubblesBackGround()
        } else {
            LocalDesignSystem.current.backgroundGradient()
        },
        applySystemBarPadding = !LocalIsOneSafeK.current,
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .imePadding(),
        ) {
            WriteMessageTopBar(
                contactNameProvider = nameProvider,
                onContactNameClick = {
                    focusManager.clearFocus()
                    onContactNameClick()
                },
                modifier = Modifier
                    .landscapeSystemBarsPadding()
                    .padding(
                        horizontal = OSDimens.SystemSpacing.Regular,
                        vertical = OSDimens.SystemSpacing.Small,
                    ),
                leadingSlot = {
                    LeadingSlot(
                        isOneSafeK = isOneSafeK,
                        onBackClick = onBackClick,
                    )
                },
                trailingSlot = {
                    if (isOneSafeK) {
                        ImeTrailingSlot(
                            onHideConversationClick = { isConversationHidden = !isConversationHidden },
                            isConversationHidden = isConversationHidden,
                        )
                    } else {
                        TrailingSlot(
                            onDeleteAllMessagesClick = onDeleteAllMessagesClick,
                            onHideConversationClick = { isConversationHidden = !isConversationHidden },
                            isConversationHidden = isConversationHidden,
                        )
                    }
                },
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.TopEnd,
            ) {
                when {
                    !isConversationReady -> {
                        ConversationNotReadyCard(
                            contactName = nameProvider.name.string,
                            onResendInvitationClick = onResendInvitationClick,
                        )
                    }
                    isConversationHidden || conversation.itemCount == 0 -> {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = OSDimens.SystemSpacing.Regular),
                            contentAlignment = Alignment.BottomCenter,
                        ) {
                            ConversationDayHeader(
                                text = LbcTextSpec.StringResource(OSString.oneSafeK_messageDate_today),
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
                                contactNameProvider = nameProvider,
                                context = context,
                                messageLongPress = messageLongPress,
                            )
                        }
                    }
                }
            }
            if (isConversationReady) {
                OSTheme(
                    isSystemInDarkTheme = true,
                    isMaterialYouSettingsEnabled = LocalDesignSystem.current.isMaterialYouEnabled,
                ) {
                    LaunchedEffect(key1 = Unit) {
                        focusRequester.requestFocus()
                    }
                    if (message.preview != null) {
                        ComposeMessageCard(
                            plainMessage = message.plainMessage,
                            encryptedMessage = message.preview,
                            onPlainMessageChange = onPlainMessageChange,
                            onClickOnSend = sendMessage,
                            sendIcon = sendIcon,
                            onPreviewClick = onPreviewClick,
                            focusRequester = focusRequester,
                            canSend = canSend,
                        )
                    } else {
                        NoPreviewComposeMessageCard(
                            plainMessage = message.plainMessage,
                            onPlainMessageChange = onPlainMessageChange,
                            onClickOnSend = sendMessage,
                            sendIcon = sendIcon,
                            focusRequester = focusRequester,
                            canSend = canSend,
                        )
                    }
                }
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

@Composable
private fun LeadingSlot(
    isOneSafeK: Boolean,
    onBackClick: () -> Unit,
) {
    if (isOneSafeK) {
        OSIconButton(
            image = OSImageSpec.Drawable(OSDrawable.ic_close),
            onClick = onBackClick,
            buttonSize = OSDimens.SystemButtonDimension.NavBarAction,
            contentDescription = LbcTextSpec.StringResource(OSString.common_accessibility_back),
            colors = OSIconButtonDefaults.iconButtonColors(
                containerColor = LocalDesignSystem.current.bubblesSecondaryContainer(),
                contentColor = MaterialTheme.colorScheme.onSurface,
                state = OSActionState.Enabled,
            ),
        )
    } else {
        OSIconButton(
            image = OSImageSpec.Drawable(OSDrawable.ic_back),
            onClick = onBackClick,
            buttonSize = OSDimens.SystemButtonDimension.NavBarAction,
            contentDescription = LbcTextSpec.StringResource(OSString.common_accessibility_back),
            colors = OSIconButtonDefaults.iconButtonColors(
                containerColor = LocalDesignSystem.current.bubblesSecondaryContainer(),
                contentColor = MaterialTheme.colorScheme.onSurface,
                state = OSActionState.Enabled,
            ),
        )
    }
}

@Composable
private fun TrailingSlot(
    onDeleteAllMessagesClick: () -> Unit,
    onHideConversationClick: () -> Unit,
    isConversationHidden: Boolean,
) {
    var isActionMenuExpanded: Boolean by rememberSaveable { mutableStateOf(false) }
    Box {
        OSIconButton(
            image = OSImageSpec.Drawable(OSDrawable.ic_menu),
            onClick = { isActionMenuExpanded = true },
            contentDescription = LbcTextSpec.StringResource(OSString.common_accessibility_moreAction),
            buttonSize = OSDimens.SystemButtonDimension.NavBarAction,
            colors = OSIconButtonDefaults.iconButtonColors(
                containerColor = LocalDesignSystem.current.bubblesSecondaryContainer(),
                contentColor = MaterialTheme.colorScheme.onSurface,
                state = OSActionState.Enabled,
            ),
        )
        ContactActionMenu(
            isMenuExpended = isActionMenuExpanded,
            onDismiss = { isActionMenuExpanded = false },
            onDeleteMessages = {
                isActionMenuExpanded = false
                onDeleteAllMessagesClick()
            },
            onHideConversation = {
                isActionMenuExpanded = false
                onHideConversationClick()
            },
            isConversationHidden = isConversationHidden,
            offset = DpOffset(0.dp, OSDimens.SystemSpacing.ExtraSmall),
        )
    }
}

@Composable
private fun ImeTrailingSlot(
    onHideConversationClick: () -> Unit,
    isConversationHidden: Boolean,
) {
    val (image, contentDescription) = if (isConversationHidden) {
        OSImageSpec.Drawable(OSDrawable.ic_visibility_on) to
            LbcTextSpec.StringResource(OSString.bubbles_writeMessageScreen_hideConversation)
    } else {
        OSImageSpec.Drawable(OSDrawable.ic_visibility_off) to
            LbcTextSpec.StringResource(OSString.bubbles_writeMessageScreen_showConversation)
    }

    OSIconButton(
        image = image,
        onClick = onHideConversationClick,
        contentDescription = contentDescription,
        buttonSize = OSDimens.SystemButtonDimension.NavBarAction,
        colors = OSIconButtonDefaults.iconButtonColors(
            containerColor = LocalDesignSystem.current.bubblesSecondaryContainer(),
            contentColor = MaterialTheme.colorScheme.onSurface,
            state = OSActionState.Enabled,
        ),
    )
}

interface WriteMessageNavScope {
    val navigationToInvitation: (UUID) -> Unit
    val navigateToContactDetail: (UUID) -> Unit
    val navigateBack: () -> Unit
    val deeplinkBubblesWriteMessage: ((contactId: UUID) -> Unit)?
}

@OsDefaultPreview
@Composable
fun WriteMessageScreenPreview() {
    OSTheme {
        val pagingItems: LazyPagingItems<ConversationUiData> = MutableStateFlow(
            PagingData.from(
                listOf<ConversationUiData>(
                    ConversationUiData.Message(
                        id = createRandomUUID(),
                        text = LbcTextSpec.Raw("hello"),
                        direction = MessageDirection.RECEIVED,
                        sendAt = Instant.ofEpochSecond(0),
                        channelName = loremIpsum(1),
                        type = ConversationUiData.MessageType.Message,
                        hasCorruptedData = false,
                    ),
                    ConversationUiData.Message(
                        id = createRandomUUID(),
                        text = LbcTextSpec.Raw("hello hello"),
                        direction = MessageDirection.SENT,
                        sendAt = Instant.ofEpochSecond(0),
                        channelName = loremIpsum(1),
                        type = ConversationUiData.MessageType.Message,
                        hasCorruptedData = false,
                    ),
                ),
            ),
        ).collectAsLazyPagingItems()

        WriteMessageScreen(
            nameProvider = OSNameProvider.fromName("A", false),
            message = BubblesWritingMessage(TextFieldValue(loremIpsum(10), TextRange(10)), loremIpsum(10)),
            onContactNameClick = {},
            onResendInvitationClick = {},
            onPlainMessageChange = {},
            sendMessage = {},
            conversation = pagingItems,
            onBackClick = {},
            sendIcon = OSImageSpec.Drawable(OSDrawable.ic_send),
            isConversationReady = true,
            onPreviewClick = {},
            onDeleteAllMessagesClick = {},
            isOneSafeK = false,
            messageLongPress = object : MessageLongPress() {
                override fun onLongClick(id: UUID) {}
            },
            focusRequester = remember { FocusRequester() },
            canSend = true,
        )
    }
}

@OsDefaultPreview
@Composable
fun ImeWriteMessageScreenPreview() {
    OSTheme {
        val pagingItems: LazyPagingItems<ConversationUiData> = MutableStateFlow(
            PagingData.from(
                listOf<ConversationUiData>(
                    ConversationUiData.Message(
                        id = createRandomUUID(),
                        text = LbcTextSpec.Raw("hello"),
                        direction = MessageDirection.RECEIVED,
                        sendAt = Instant.ofEpochSecond(0),
                        channelName = loremIpsum(1),
                        type = ConversationUiData.MessageType.Message,
                        hasCorruptedData = false,
                    ),
                    ConversationUiData.Message(
                        id = createRandomUUID(),
                        text = LbcTextSpec.StringResource(OSString.bubbles_writeMessageScreen_corruptedMessage),
                        direction = MessageDirection.SENT,
                        sendAt = null,
                        channelName = null,
                        type = ConversationUiData.MessageType.Message,
                        hasCorruptedData = true,
                    ),
                    ConversationUiData.Message(
                        id = createRandomUUID(),
                        text = LbcTextSpec.Raw("hello hello"),
                        direction = MessageDirection.SENT,
                        sendAt = Instant.ofEpochSecond(0),
                        channelName = loremIpsum(1),
                        type = ConversationUiData.MessageType.Message,
                        hasCorruptedData = false,
                    ),
                ),
            ),
        ).collectAsLazyPagingItems()

        WriteMessageScreen(
            nameProvider = OSNameProvider.fromName("A", false),
            message = BubblesWritingMessage(TextFieldValue(loremIpsum(10), TextRange(10)), loremIpsum(10)),
            onContactNameClick = {},
            onResendInvitationClick = {},
            onPlainMessageChange = {},
            sendMessage = {},
            conversation = pagingItems,
            onBackClick = {},
            sendIcon = OSImageSpec.Drawable(OSDrawable.ic_send),
            isConversationReady = true,
            onPreviewClick = {},
            onDeleteAllMessagesClick = {},
            isOneSafeK = false,
            messageLongPress = object : MessageLongPress() {
                override fun onLongClick(id: UUID) {}
            },
            focusRequester = remember { FocusRequester() },
            canSend = true,
        )
    }
}

@OsDefaultPreview
@Composable
fun ImeWriteMessageScreenCorruptedPreview() {
    OSTheme {
        val pagingItems = flowOf(PagingData.empty<ConversationUiData>()).collectAsLazyPagingItems()
        WriteMessageScreen(
            nameProvider = ErrorNameProvider,
            message = BubblesWritingMessage(TextFieldValue(), ""),
            onContactNameClick = {},
            onResendInvitationClick = {},
            onPlainMessageChange = {},
            sendMessage = {},
            conversation = pagingItems,
            onBackClick = {},
            sendIcon = OSImageSpec.Drawable(OSDrawable.ic_send),
            isConversationReady = true,
            onPreviewClick = {},
            onDeleteAllMessagesClick = {},
            isOneSafeK = false,
            messageLongPress = object : MessageLongPress() {
                override fun onLongClick(id: UUID) {}
            },
            focusRequester = remember { FocusRequester() },
            canSend = false,
        )
    }
}
