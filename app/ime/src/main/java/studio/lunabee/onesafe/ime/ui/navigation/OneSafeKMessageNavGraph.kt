/*
 * Copyright (c) 2023 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 5/30/2023 - for the oneSafe6 SDK.
 * Last modified 5/30/23, 2:18 PM
 */

package studio.lunabee.onesafe.ime.ui.navigation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import studio.lunabee.onesafe.bubbles.ui.selectcontact.ChangeContactDestination
import studio.lunabee.onesafe.bubbles.ui.selectcontact.SelectContactDestination
import studio.lunabee.onesafe.bubbles.ui.selectcontact.SelectContactRoute
import studio.lunabee.onesafe.messaging.writemessage.composable.WriteMessageExitIcon
import studio.lunabee.onesafe.messaging.writemessage.destination.WriteMessageDestination
import studio.lunabee.onesafe.messaging.writemessage.screen.WriteMessageRoute

@Composable
fun OneSafeKMessageNavGraph(
    navigateBackToMainApp: () -> Unit,
    navController: NavHostController = rememberNavController(),
) {
    // TODO : Discuss with product team if we need to change to an animated nav graph.
    NavHost(
        navController = navController,
        startDestination = SelectContactDestination.route,
    ) {
        composable(
            route = SelectContactDestination.route,
        ) {
            SelectContactRoute(
                navigateToWriteMessage = { contactId ->
                    navController.navigate(WriteMessageDestination.getRoute(contactId))
                },
                navigateBack = navigateBackToMainApp,
            )
        }

        composable(
            route = WriteMessageDestination.route,
        ) { backStackEntry ->
            val context = LocalContext.current
            WriteMessageRoute(
                onChangeRecipient = {
                    navController.navigate(ChangeContactDestination.route)
                },
                sendMessage = { encMessage ->
                    // TODO Temporary action used to test oneSafeK feature.
                    val myClipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val myClip: ClipData = ClipData.newPlainText("copy", encMessage)
                    myClipboard.setPrimaryClip(myClip)
                },
                contactIdFlow = backStackEntry.savedStateHandle.getStateFlow(WriteMessageDestination.ContactIdArgs, null),
                exitIcon = WriteMessageExitIcon.WriteMessageBackIcon(
                    onClick = { navController.popBackStack() },
                ),
            )
        }

        composable(
            route = ChangeContactDestination.route,
        ) {
            SelectContactRoute(
                navigateBack = { navController.popBackStack() },
                navigateToWriteMessage = { contactId ->
                    navController.getBackStackEntry(WriteMessageDestination.route)
                        .savedStateHandle[WriteMessageDestination.ContactIdArgs] = contactId.toString()
                    navController.popBackStack()
                },
            )
        }
    }
}
