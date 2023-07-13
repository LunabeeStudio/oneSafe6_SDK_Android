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
 * Created by Lunabee Studio / Date - 7/5/2023 - for the oneSafe6 SDK.
 * Last modified 7/5/23, 3:30 PM
 */

package studio.lunabee.onesafe.messaging.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import studio.lunabee.onesafe.bubbles.ui.selectcontact.SelectContactDestination
import studio.lunabee.onesafe.bubbles.ui.selectcontact.SelectContactRoute
import studio.lunabee.onesafe.commonui.navigation.OSDestination
import studio.lunabee.onesafe.messaging.writemessage.composable.WriteMessageExitIcon
import studio.lunabee.onesafe.messaging.writemessage.destination.WriteMessageDestination
import studio.lunabee.onesafe.messaging.writemessage.screen.WriteMessageRoute
import studio.lunabee.onesafe.messaging.writemessage.viewmodel.MessagingViewModel

fun NavGraphBuilder.messagingNavGraph(
    navigateBack: () -> Unit,
    navController: NavController,
) {
    navigation(startDestination = SelectContactDestination.route, route = MessagingDestination.route) {
        composable(
            route = SelectContactDestination.route,
        ) { backStackEntry ->
            messagingViewModel(backStackEntry, navController)
            SelectContactRoute(
                navigateToWriteMessage = { contactId ->
                    navController.navigate(WriteMessageDestination.getRoute(contactId))
                },
                navigateBack = navigateBack,
            )
        }

        composable(
            route = WriteMessageDestination.route,
        ) { backStackEntry ->
            messagingViewModel(backStackEntry, navController)
            val context = LocalContext.current
            WriteMessageRoute(
                onChangeRecipient = null,
                sendMessage = { encMessage ->
                    // TODO Temporary action used to test oneSafeK feature.
                    val myClipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val myClip: ClipData = ClipData.newPlainText("copy", encMessage)
                    myClipboard.setPrimaryClip(myClip)
                },
                contactIdFlow = backStackEntry.savedStateHandle.getStateFlow(WriteMessageDestination.ContactIdArgs, null),
                exitIcon = WriteMessageExitIcon.WriteMessageBackIcon(
                    onClick = navigateBack,
                ),
            )
        }
    }
}

object MessagingDestination : OSDestination {
    override val route: String = "messaging"
}

@Composable
private fun messagingViewModel(backStackEntry: NavBackStackEntry, navController: NavController): MessagingViewModel {
    val viewModelStoreOwner = remember(backStackEntry) { navController.getBackStackEntry(MessagingDestination.route) }
    return hiltViewModel(viewModelStoreOwner)
}
