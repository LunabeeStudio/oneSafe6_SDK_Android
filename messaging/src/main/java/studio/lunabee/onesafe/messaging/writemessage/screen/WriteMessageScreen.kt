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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
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
import kotlinx.coroutines.launch
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.atom.button.OSIconButton
import studio.lunabee.onesafe.atom.button.defaults.OSIconButtonDefaults
import studio.lunabee.onesafe.bubbles.ui.extension.getDeepLinkFromMessage
import studio.lunabee.onesafe.bubbles.ui.model.UIBubblesContactInfo
import studio.lunabee.onesafe.commonui.OSNameProvider
import studio.lunabee.onesafe.commonui.R
import studio.lunabee.onesafe.commonui.dialog.DefaultAlertDialog
import studio.lunabee.onesafe.commonui.dialog.ImeDialog
import studio.lunabee.onesafe.commonui.localprovider.LocalIsOneSafeK
import studio.lunabee.onesafe.commonui.snackbar.SnackbarState
import studio.lunabee.onesafe.extension.landscapeSystemBarsPadding
import studio.lunabee.onesafe.extension.loremIpsum
import studio.lunabee.onesafe.messaging.domain.model.ConversationState
import studio.lunabee.onesafe.messaging.domain.model.MessageDirection
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

// TODO oSK separate WriteMessage IME and OS

context(WriteMessageNavScope)
@Composable
fun WriteMessageRoute(
    onChangeRecipient: (() -> Unit)?,
    sendMessage: (data: SentMessageData, messageToSend: String) -> Unit,
    resendMessage: (String) -> Unit,
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
    val isMaterialYouEnabled by viewModel.isMaterialYouSettingsEnabled.collectAsStateWithLifecycle(initialValue = false)
    val isPreviewEnabled by viewModel.isPreviewEnabled.collectAsStateWithLifecycle(initialValue = false)
    val isOneSafeK = LocalIsOneSafeK.current
    val oneSafeKsnackBarHostState = remember { SnackbarHostState() }
    val viewModelSnackBarHostState = remember { SnackbarHostState() }
    var snackbarState: ConversationMoreOptionsSnackbarState? by remember { mutableStateOf(null) }
    val viewModelSnackbarState: SnackbarState? by viewModel.snackbarState.collectAsStateWithLifecycle(initialValue = null)

    val viewModelSnackbarStateVisual = viewModelSnackbarState?.snackbarVisuals
    LaunchedEffect(viewModelSnackbarStateVisual) {
        viewModelSnackbarStateVisual?.let {
            viewModelSnackBarHostState.showSnackbar(it)
            viewModel.resetSnackbarState()
        }
    }

    val messageLongPress: MessageLongPress
    if (isOneSafeK) {
        dialogState?.let { dialog ->
            dialog.ImeDialog()
            LaunchedEffect(dialog, hideKeyboard) {
                hideKeyboard?.invoke()
            }
        }
        snackbarState?.SnackBar(oneSafeKsnackBarHostState)
        messageLongPress = object : MessageLongPress() {
            override fun onLongClick(id: UUID) {
                deeplinkBubblesWriteMessage?.let { deeplink ->
                    snackbarState = ConversationMoreOptionsSnackbarState {
                        deeplink(viewModel.contactId.value!!)
                    }
                }
            }
        }
    } else {
        dialogState?.DefaultAlertDialog()
        messageLongPress = DropDownMenuMessageLongPress(
            onResendClick = { sentMessageId ->
                coroutineScope.launch {
                    viewModel.getSentMessage(sentMessageId)?.let {
                        resendMessage(it.getDeepLinkFromMessage(uiState.isUsingDeepLink))
                    }
                }
            },
            onDeleteMessageClick = viewModel::deleteMessage,
        )
    }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        if (isOneSafeK) {
            SnackbarHost(
                hostState = oneSafeKsnackBarHostState,
                modifier = Modifier
                    .zIndex(UiConstants.SnackBar.ZIndex)
                    .align(Alignment.BottomCenter),
            )
        }

        SnackbarHost(
            hostState = viewModelSnackBarHostState,
            modifier = Modifier
                .zIndex(UiConstants.SnackBar.ZIndex)
                .align(Alignment.BottomCenter)
                .navigationBarsPadding(),
        )

        uiState.currentContact?.let { contact ->
            WriteMessageScreen(
                contact = contact,
                plainMessage = uiState.plainMessage,
                encryptedMessage = uiState.encryptedPreview,
                onContactNameClick = onChangeRecipient ?: { navigateToContactDetail(contact.id) },
                onResendInvitationClick = {
                    navigationToInvitation(viewModel.contactId.value!!)
                },
                onPlainMessageChange = viewModel::onPlainMessageChange,
                sendMessage = {
                    coroutineScope.launch {
                        val sentMessageData = viewModel.encryptMessage(content = uiState.plainMessage)
                        sentMessageData?.let { sentMessageData ->
                            sendMessage(
                                sentMessageData,
                                sentMessageData.encMessage.getDeepLinkFromMessage(uiState.isUsingDeepLink),
                            )
                        }
                    }
                },
                conversation = conversation,
                onBackClick = navigateBack,
                sendIcon = sendIcon,
                isMaterialYouEnabled = isMaterialYouEnabled,
                isConversationReady = uiState.isConversationReady,
                isPreviewEnabled = isPreviewEnabled,
                onPreviewClick = {
                    viewModel.displayPreviewInfo()
                },
                onDeleteAllMessagesClick = viewModel::displayRemoveConversationDialog,
                isOneSafeK = isOneSafeK,
                messageLongPress = messageLongPress,
            )
        }
    }
}

@Composable
fun WriteMessageScreen(
    contact: UIBubblesContactInfo,
    plainMessage: String,
    encryptedMessage: String,
    onContactNameClick: () -> Unit,
    onResendInvitationClick: () -> Unit,
    onPlainMessageChange: (String) -> Unit,
    sendMessage: () -> Unit,
    conversation: LazyPagingItems<ConversationUiData>,
    onBackClick: () -> Unit,
    sendIcon: OSImageSpec,
    isMaterialYouEnabled: Boolean,
    isConversationReady: Boolean,
    isPreviewEnabled: Boolean,
    onPreviewClick: () -> Unit,
    onDeleteAllMessagesClick: () -> Unit,
    isOneSafeK: Boolean,
    messageLongPress: MessageLongPress,
) {
    val focusManager = LocalFocusManager.current
    val lazyListState = rememberLazyListState()
    var isConversationHidden: Boolean by rememberSaveable { mutableStateOf(false) }
    OSScreen(
        testTag = UiConstants.TestTag.Screen.WriteMessageScreen,
        background = LocalDesignSystem.current.bubblesBackGround(),
        applySystemBarPadding = !LocalIsOneSafeK.current,
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .imePadding(),
        ) {
            WriteMessageTopBar(
                contactNameProvider = contact.nameProvider,
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
                                messageLongPress = messageLongPress,
                            )
                        }
                    }
                }
            }
            if (isConversationReady) {
                OSTheme(
                    isSystemInDarkTheme = true,
                    isMaterialYouSettingsEnabled = isMaterialYouEnabled,
                ) {
                    if (isPreviewEnabled) {
                        ComposeMessageCard(
                            plainMessage = plainMessage,
                            encryptedMessage = encryptedMessage,
                            onPlainMessageChange = onPlainMessageChange,
                            onClickOnSend = sendMessage,
                            sendIcon = sendIcon,
                            onPreviewClick = onPreviewClick,
                        )
                    } else {
                        NoPreviewComposeMessageCard(
                            plainMessage = plainMessage,
                            onPlainMessageChange = onPlainMessageChange,
                            onClickOnSend = sendMessage,
                            sendIcon = sendIcon,
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
            image = OSImageSpec.Drawable(R.drawable.ic_close),
            onClick = onBackClick,
            buttonSize = OSDimens.SystemButtonDimension.NavBarAction,
            contentDescription = LbcTextSpec.StringResource(R.string.common_accessibility_back),
            colors = OSIconButtonDefaults.iconButtonColors(
                containerColor = LocalDesignSystem.current.bubblesSecondaryContainer(),
                contentColor = MaterialTheme.colorScheme.onSurface,
                state = OSActionState.Enabled,
            ),
        )
    } else {
        OSIconButton(
            image = OSImageSpec.Drawable(R.drawable.ic_back),
            onClick = onBackClick,
            buttonSize = OSDimens.SystemButtonDimension.NavBarAction,
            contentDescription = LbcTextSpec.StringResource(R.string.common_accessibility_back),
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
            image = OSImageSpec.Drawable(R.drawable.ic_more),
            onClick = { isActionMenuExpanded = true },
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
    val image = if (isConversationHidden) {
        OSImageSpec.Drawable(R.drawable.ic_visibility_on)
    } else {
        OSImageSpec.Drawable(R.drawable.ic_visibility_off)
    }

    OSIconButton(
        image = image,
        onClick = onHideConversationClick,
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
                        id = UUID.randomUUID(),
                        text = LbcTextSpec.Raw("hello"),
                        direction = MessageDirection.RECEIVED,
                        sendAt = Instant.ofEpochSecond(0),
                        channelName = loremIpsum(1),
                        type = ConversationUiData.MessageType.Message,
                    ),
                    ConversationUiData.Message(
                        id = UUID.randomUUID(),
                        text = LbcTextSpec.Raw("hello hello"),
                        direction = MessageDirection.SENT,
                        sendAt = Instant.ofEpochSecond(0),
                        channelName = loremIpsum(1),
                        type = ConversationUiData.MessageType.Message,
                    ),
                ),
            ),
        ).collectAsLazyPagingItems()

        WriteMessageScreen(
            contact = UIBubblesContactInfo(UUID.randomUUID(), OSNameProvider.fromName("A", false), ConversationState.FullySetup),
            plainMessage = loremIpsum(10),
            encryptedMessage = loremIpsum(10),
            onContactNameClick = {},
            onResendInvitationClick = {},
            onPlainMessageChange = {},
            sendMessage = {},
            conversation = pagingItems,
            onBackClick = {},
            sendIcon = OSImageSpec.Drawable(R.drawable.ic_send),
            isMaterialYouEnabled = false,
            isConversationReady = true,
            isPreviewEnabled = true,
            onPreviewClick = {},
            onDeleteAllMessagesClick = {},
            isOneSafeK = false,
            messageLongPress = object : MessageLongPress() {
                override fun onLongClick(id: UUID) {}
            },
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
                        id = UUID.randomUUID(),
                        text = LbcTextSpec.Raw("hello"),
                        direction = MessageDirection.RECEIVED,
                        sendAt = Instant.ofEpochSecond(0),
                        channelName = loremIpsum(1),
                        type = ConversationUiData.MessageType.Message,
                    ),
                    ConversationUiData.Message(
                        id = UUID.randomUUID(),
                        text = LbcTextSpec.Raw("hello hello"),
                        direction = MessageDirection.SENT,
                        sendAt = Instant.ofEpochSecond(0),
                        channelName = loremIpsum(1),
                        type = ConversationUiData.MessageType.Message,
                    ),
                ),
            ),
        ).collectAsLazyPagingItems()

        WriteMessageScreen(
            contact = UIBubblesContactInfo(UUID.randomUUID(), OSNameProvider.fromName("A", false), ConversationState.FullySetup),
            plainMessage = loremIpsum(10),
            encryptedMessage = loremIpsum(10),
            onContactNameClick = {},
            onResendInvitationClick = {},
            onPlainMessageChange = {},
            sendMessage = {},
            conversation = pagingItems,
            onBackClick = {},
            sendIcon = OSImageSpec.Drawable(R.drawable.ic_send),
            isMaterialYouEnabled = false,
            isConversationReady = true,
            isPreviewEnabled = true,
            onPreviewClick = {},
            onDeleteAllMessagesClick = {},
            isOneSafeK = false,
            messageLongPress = object : MessageLongPress() {
                override fun onLongClick(id: UUID) {}
            },
        )
    }
}
