/*
 * Copyright (c) 2024-2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 8/7/2024 - for the oneSafe6 SDK.
 * Last modified 06/08/2024 17:05
 */

package studio.lunabee.onesafe.messaging.senditem

import android.app.ActivityManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.onesafe.animation.OSTopBarAnimatedVisibility
import studio.lunabee.onesafe.animation.rememberOSTopBarVisibilityNestedScrollConnection
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.atom.button.OSFilledButton
import studio.lunabee.onesafe.atom.lazyVerticalOSRegularSpacer
import studio.lunabee.onesafe.bubbles.ui.contact.ContactScreenFactory
import studio.lunabee.onesafe.bubbles.ui.model.BubblesConversationInfo
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.action.topAppBarOptionNavBack
import studio.lunabee.onesafe.commonui.dialog.DefaultAlertDialog
import studio.lunabee.onesafe.commonui.extension.markdown
import studio.lunabee.onesafe.commonui.snackbar.ActionSnackbarVisuals
import studio.lunabee.onesafe.messaging.MessagingConstants
import studio.lunabee.onesafe.messaging.extension.getFileSharingIntent
import studio.lunabee.onesafe.messaging.senditem.model.FileShareData
import studio.lunabee.onesafe.messaging.senditem.model.SharedItemInfo
import studio.lunabee.onesafe.messaging.utils.hasExternalActivityVisible
import studio.lunabee.onesafe.molecule.OSTopAppBar
import studio.lunabee.onesafe.organism.card.OSMessageCard
import studio.lunabee.onesafe.organism.card.OSMessageCardAttributes
import studio.lunabee.onesafe.organism.card.OSMessageCardStyle
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens

context(SendItemBubblesSelectContactNavScope)
@Composable
fun SendItemBubblesSelectContactRoute(
    viewModel: SendItemBubblesSelectContactViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val dialogState by viewModel.dialogState.collectAsStateWithLifecycle()
    val conversations: List<BubblesConversationInfo>? by viewModel.conversation.collectAsStateWithLifecycle()
    val fileToShare: FileShareData? by viewModel.fileToShare.collectAsStateWithLifecycle()
    val sharedItemInfo: SharedItemInfo by viewModel.sharedItemInfo.collectAsStateWithLifecycle()

    dialogState?.DefaultAlertDialog()

    // Always save the message when navigating to another activity to share the current message
    LifecycleEventEffect(event = Lifecycle.Event.ON_STOP) {
        if (hasExternalActivityVisible(context.getSystemService(ActivityManager::class.java))) {
            fileToShare?.let {
                viewModel.saveMessage(it)
                showSnackbar(
                    ActionSnackbarVisuals(
                        action = { navigateToWriteMessage(it.contactId.uuid) },
                        onDismiss = {},
                        message = LbcTextSpec.StringResource(OSString.bubbles_shareItem_successMessage).string(context),
                        actionLabel = LbcTextSpec.StringResource(OSString.common_see).string(context),
                        duration = SnackbarDuration.Indefinite,
                        withDismissAction = false,
                    ),
                )
            }
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {
            viewModel.consumeArchive()
        },
    )

    LaunchedEffect(fileToShare) {
        fileToShare?.let {
            val intent = context.getFileSharingIntent(it.file, MessagingConstants.MessageArchiveMimeType)
            launcher.launch(intent)
        }
    }

    if (!conversations.isNullOrEmpty()) {
        FilledSendItemBubblesSelectContactScreen(
            conversations = conversations,
            onConversationClick = {
                viewModel.getMessageToSend(contactId = it)
            },
            onBackClick = navigateBack,
            sharedItemInfo = sharedItemInfo,
        )
    } else {
        OSScreen(testTag = UiConstants.TestTag.Screen.SendItemBubblesScreen) {
            Column {
                OSTopAppBar(
                    title = LbcTextSpec.StringResource(OSString.common_share),
                    options = listOf(topAppBarOptionNavBack(navigateBack)),
                    modifier = Modifier.statusBarsPadding(),
                )
                OSMessageCard(
                    title = LbcTextSpec.StringResource(OSString.home_bubblesCard_title),
                    description = LbcTextSpec.StringResource(OSString.home_bubblesCard_message),
                    attributes = OSMessageCardAttributes()
                        .style(OSMessageCardStyle.Default),
                    modifier = Modifier.padding(OSDimens.SystemSpacing.Regular),
                    action = {
                        OSFilledButton(
                            text = LbcTextSpec.StringResource(OSString.home_bubblesCard_button),
                            onClick = sendItemBubblesNavigateToBubblesHome,
                            modifier = Modifier.padding(bottom = OSDimens.SystemSpacing.Small),
                        )
                    },
                )
            }
        }
    }
}

@Composable
fun FilledSendItemBubblesSelectContactScreen(
    conversations: List<BubblesConversationInfo>?,
    sharedItemInfo: SharedItemInfo,
    onConversationClick: (contactId: DoubleRatchetUUID) -> Unit,
    onBackClick: () -> Unit,
) {
    val lazyListState: LazyListState = rememberLazyListState()
    val nestedScrollConnection = rememberOSTopBarVisibilityNestedScrollConnection(lazyListState)

    OSScreen(testTag = UiConstants.TestTag.Screen.SendItemBubblesScreen) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection),
            contentPadding = PaddingValues(
                top = OSDimens.ItemTopBar.Height,
                bottom = OSDimens.SystemSpacing.Regular,
                start = OSDimens.SystemSpacing.Regular,
                end = OSDimens.SystemSpacing.Regular,
            ),
        ) {
            lazyVerticalOSRegularSpacer()
            item {
                OSMessageCard(
                    description = sharedItemInfo.getDescription().markdown(),
                    modifier = Modifier.animateContentSize(),
                )
            }
            lazyVerticalOSRegularSpacer()
            conversations?.let {
                ContactScreenFactory.addConversations(
                    conversations = conversations,
                    onClick = onConversationClick,
                )
            }
            lazyVerticalOSRegularSpacer()
        }

        OSTopBarAnimatedVisibility(visible = nestedScrollConnection.isTopBarVisible) {
            OSTopAppBar(
                title = LbcTextSpec.StringResource(OSString.common_share),
                options = listOf(
                    topAppBarOptionNavBack(onBackClick),
                ),
                modifier = Modifier
                    .statusBarsPadding(),
            )
        }
    }
}
