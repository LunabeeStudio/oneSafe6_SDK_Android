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

import android.net.Uri
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.commonui.R
import studio.lunabee.onesafe.commonui.extension.getTextSharingIntent
import studio.lunabee.onesafe.messaging.domain.model.DecryptResult
import studio.lunabee.onesafe.messaging.writemessage.model.SentMessageData
import studio.lunabee.onesafe.messaging.writemessage.screen.WriteMessageNavScope
import studio.lunabee.onesafe.messaging.writemessage.screen.WriteMessageRoute
import studio.lunabee.onesafe.messaging.writemessage.viewmodel.WriteMessageViewModel
import java.util.UUID

object WriteMessageDestination {

    const val ContactIdArg: String = "ContactIdArg"
    const val ErrorArg: String = "ErrorArg"
    const val path: String = "write_message"
    const val route: String = "$path?" +
        "$ContactIdArg={$ContactIdArg}" +
        "&$ErrorArg={$ErrorArg}"

    fun getRouteFromDecryptResult(
        decryptResult: DecryptResult,
    ): String {
        return Uri.Builder().apply {
            path(path)
            appendQueryParameter(ContactIdArg, decryptResult.contactId.toString())
            decryptResult.error?.let { appendQueryParameter(ErrorArg, it.name) }
        }.build().toString()
    }

    fun getRouteFromContactId(
        contactId: UUID,
    ): String {
        return Uri.Builder().apply {
            path(path)
            appendQueryParameter(ContactIdArg, contactId.toString())
        }.build().toString()
    }
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
        var currentSentMessageData: SentMessageData? by remember { mutableStateOf(null) }
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
            onResult = { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    currentSentMessageData?.let(viewModel::saveEncryptedMessage)
                }
                currentSentMessageData = null
            },
        )

        createMessagingViewModel(backStackEntry)
        WriteMessageRoute(
            onChangeRecipient = null,
            sendMessage = { sentMessageData, messageToSend ->
                currentSentMessageData = sentMessageData
                val intent = context.getTextSharingIntent(messageToSend)
                launcher.launch(intent)
            },
            contactIdFlow = backStackEntry.savedStateHandle.getStateFlow(WriteMessageDestination.ContactIdArg, null),
            sendIcon = OSImageSpec.Drawable(R.drawable.ic_share),
            hideKeyboard = null,
            resendMessage = { messageToSend ->
                val intent = context.getTextSharingIntent(messageToSend)
                context.startActivity(intent)
            },
            viewModel = viewModel,
        )
    }
}
