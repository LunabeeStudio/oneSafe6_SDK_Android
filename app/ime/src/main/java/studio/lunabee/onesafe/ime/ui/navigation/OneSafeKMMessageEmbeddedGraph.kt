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
 * Created by Lunabee Studio / Date - 6/12/2023 - for the oneSafe6 SDK.
 * Last modified 6/12/23, 10:10 AM
 */

package studio.lunabee.onesafe.ime.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import dagger.Lazy
import studio.lunabee.onesafe.commonui.login.screen.LoginRoute
import studio.lunabee.onesafe.ime.ui.navigation.login.LoginDestination
import studio.lunabee.onesafe.bubbles.ui.selectcontact.ChangeContactDestination
import studio.lunabee.onesafe.bubbles.ui.selectcontact.SelectContactDestination
import studio.lunabee.onesafe.bubbles.ui.selectcontact.SelectContactRoute
import studio.lunabee.onesafe.messaging.writemessage.composable.WriteMessageExitIcon
import studio.lunabee.onesafe.messaging.writemessage.destination.WriteMessageDestination
import studio.lunabee.onesafe.messaging.writemessage.screen.WriteMessageRoute
import studio.lunabee.onesafe.viewmodel.LoginViewModelFactory
import studio.lunabee.onesafe.viewmodel.SelectContactViewModelFactory
import studio.lunabee.onesafe.viewmodel.WriteMessageViewModelFactory

@Composable
fun OneSafeKMMessageEmbeddedGraph(
    navController: NavHostController,
    loginViewModelFactory: Lazy<LoginViewModelFactory>,
    selectContactViewModelFactory: Lazy<SelectContactViewModelFactory>,
    writeMessageViewModelFactory: Lazy<WriteMessageViewModelFactory>,
    onLoginSuccess: () -> Unit,
    dismissUi: () -> Unit,
    sendMessage: (String) -> Unit,
    exit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = LoginDestination.route,
        modifier = modifier,
    ) {
        composable(
            route = LoginDestination.route,
        ) { backStackEntry ->
            LoginRoute(
                onSuccess = {
                    onLoginSuccess()
                    navController.navigate(
                        SelectContactDestination.route,
                        navOptions {
                            popUpTo(LoginDestination.route) {
                                inclusive = true
                            }
                        },
                    )
                },
                viewModel = viewModel(
                    viewModelStoreOwner = backStackEntry,
                    factory = loginViewModelFactory.get(),
                ),
            )
        }

        composable(
            route = SelectContactDestination.route,
        ) { backStackEntry ->
            SelectContactRoute(
                navigateBack = dismissUi,
                navigateToWriteMessage = {
                    navController.navigate(WriteMessageDestination.getRoute(it))
                },
                viewModel = viewModel(
                    viewModelStoreOwner = backStackEntry,
                    factory = selectContactViewModelFactory.get(),
                ),
            )
        }

        composable(
            route = WriteMessageDestination.route,
        ) { backStackEntry ->
            WriteMessageRoute(
                onChangeRecipient = { navController.navigate(ChangeContactDestination.route) },
                sendMessage = sendMessage,
                exitIcon = WriteMessageExitIcon.WriteMessageCloseIcon(
                    onClick = exit,
                ),
                viewModel = viewModel(
                    viewModelStoreOwner = backStackEntry,
                    factory = writeMessageViewModelFactory.get(),
                ),
                contactIdFlow = backStackEntry.savedStateHandle.getStateFlow(WriteMessageDestination.ContactIdArgs, null),
            )
        }

        composable(
            route = ChangeContactDestination.route,
        ) { backStackEntry ->
            val viewModelStoreOwner = remember(backStackEntry) {
                navController.getBackStackEntry(SelectContactDestination.route)
            }
            SelectContactRoute(
                navigateBack = { navController.popBackStack() },
                navigateToWriteMessage = { contactId ->
                    navController.getBackStackEntry(WriteMessageDestination.route)
                        .savedStateHandle[WriteMessageDestination.ContactIdArgs] = contactId.toString()
                    navController.popBackStack()
                },
                viewModel = viewModel(
                    viewModelStoreOwner = viewModelStoreOwner,
                    factory = selectContactViewModelFactory.get(),
                ),
            )
        }
    }
}
