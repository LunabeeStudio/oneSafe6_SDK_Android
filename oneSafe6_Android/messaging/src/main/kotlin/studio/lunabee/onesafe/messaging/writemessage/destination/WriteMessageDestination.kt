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
 * Last modified 6/12/23, 5:47 PM
 */

package studio.lunabee.onesafe.messaging.writemessage.destination

import android.app.ActivityManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import studio.lunabee.bubbles.domain.model.MessageSharingMode
import studio.lunabee.messaging.domain.model.DecryptResult
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.bubbles.ui.extension.getDeepLinkFromMessage
import studio.lunabee.onesafe.commonui.OSDestination
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.extension.getTextSharingIntent
import studio.lunabee.onesafe.messaging.MessagingConstants
import studio.lunabee.onesafe.messaging.extension.getFileSharingIntent
import studio.lunabee.onesafe.messaging.utils.hasExternalActivityVisible
import studio.lunabee.onesafe.messaging.writemessage.model.SentMessageData
import studio.lunabee.onesafe.messaging.writemessage.screen.WriteMessageNavScope
import studio.lunabee.onesafe.messaging.writemessage.screen.WriteMessageRoute
import studio.lunabee.onesafe.messaging.writemessage.viewmodel.WriteMessageViewModel
import java.util.UUID

object WriteMessageDestination : OSDestination {

    const val ContactIdArg: String = "ContactIdArg"
    const val ErrorArg: String = "ErrorArg"
    const val Path: String = "write_message"
    override val route: String = "$Path?" +
        "$ContactIdArg={$ContactIdArg}" +
        "&$ErrorArg={$ErrorArg}"

    fun getRouteFromDecryptResult(
        decryptResult: DecryptResult,
    ): String = Uri
        .Builder()
        .apply {
            path(Path)
            appendQueryParameter(ContactIdArg, decryptResult.contactId.uuidString())
            decryptResult.error?.let { appendQueryParameter(ErrorArg, it.name) }
        }.build()
        .toString()

    fun getRouteFromContactId(
        contactId: UUID,
    ): String = Uri
        .Builder()
        .apply {
            path(Path)
            appendQueryParameter(ContactIdArg, contactId.toString())
        }.build()
        .toString()
}

context(WriteMessageNavScope)
fun NavGraphBuilder.writeMessageScreen(
    createMessagingViewModel: @Composable (NavBackStackEntry) -> Unit,
) {
    composable(
        route = WriteMessageDestination.route,
        arguments = listOf(
            navArgument(WriteMessageDestination.ContactIdArg) {
                type = NavType.StringType
            },
            navArgument(WriteMessageDestination.ErrorArg) {
                type = NavType.StringType
                nullable = true
            },
        ),
    ) { backStackEntry ->
        val context = LocalContext.current
        val viewModel: WriteMessageViewModel = hiltViewModel()
        var sentMessageDataUnderSharing: SentMessageData? by remember { mutableStateOf(null) }
        var rawMessageSent: String? by remember { mutableStateOf(null) }
        val clipboardManager: ClipboardManager = LocalClipboardManager.current

        // Always save the message when navigating to another activity to share the current message
        LifecycleEventEffect(event = Lifecycle.Event.ON_STOP) {
            if (hasExternalActivityVisible(context.getSystemService(ActivityManager::class.java))) {
                sentMessageDataUnderSharing?.let(viewModel::saveEncryptedMessage)
                sentMessageDataUnderSharing = null
                rawMessageSent = null
            }
        }

        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
            onResult = {
                val primaryClip = clipboardManager.getText()
                rawMessageSent?.let { message ->
                    if (message == primaryClip?.text) {
                        sentMessageDataUnderSharing?.let(viewModel::saveEncryptedMessage)
                    } else {
                        sentMessageDataUnderSharing?.let(viewModel::askForSafeConfirmation)
                    }
                }
                sentMessageDataUnderSharing = null
                rawMessageSent = null
                viewModel.consumeArchive()
            },
        )

        createMessagingViewModel(backStackEntry)

        WriteMessageRoute(
            onChangeRecipient = null,
            sendMessage = { sentMessageData, messageToSend, sharingMode ->
                sentMessageDataUnderSharing = sentMessageData
                rawMessageSent = messageToSend
                when (sharingMode) {
                    MessageSharingMode.Deeplink,
                    MessageSharingMode.CypherText,
                    -> context.getTextSharingIntent(messageToSend).let(launcher::launch)
                    MessageSharingMode.Archive -> {
                        viewModel.createAndShareArchive(messageToSend) { file ->
                            val intent = context.getFileSharingIntent(file, MessagingConstants.MessageArchiveMimeType)
                            launcher.launch(intent)
                        }
                    }
                }
            },
            contactIdFlow = backStackEntry.savedStateHandle.getStateFlow(WriteMessageDestination.ContactIdArg, null),
            sendIcon = OSImageSpec.Drawable(OSDrawable.ic_share),
            resendMessage = { messageToSend, sharingMode ->
                when (sharingMode) {
                    MessageSharingMode.Deeplink ->
                        messageToSend.getDeepLinkFromMessage(sharingMode).let {
                            context
                                .getTextSharingIntent(it)
                                .let(launcher::launch)
                        }
                    MessageSharingMode.CypherText ->
                        context.getTextSharingIntent(messageToSend).let(launcher::launch)
                    MessageSharingMode.Archive -> {
                        viewModel.createAndShareArchive(messageToSend) { file ->
                            val intent = context.getFileSharingIntent(file, MessagingConstants.MessageArchiveMimeType)
                            launcher.launch(intent)
                        }
                    }
                }
            },
            viewModel = viewModel,
        )
    }
}
