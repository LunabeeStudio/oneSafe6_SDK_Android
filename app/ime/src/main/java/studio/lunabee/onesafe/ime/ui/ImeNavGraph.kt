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
 * Last modified 7/5/23, 4:34 PM
 */

package studio.lunabee.onesafe.ime.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import dagger.Lazy
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.bubbles.ui.onesafek.ChangeContactDestination
import studio.lunabee.onesafe.bubbles.ui.onesafek.SelectContactDestination
import studio.lunabee.onesafe.bubbles.ui.onesafek.SelectContactRoute
import studio.lunabee.onesafe.commonui.R
import studio.lunabee.onesafe.commonui.animation.slideHorizontalEnterTransition
import studio.lunabee.onesafe.commonui.animation.slideHorizontalExitTransition
import studio.lunabee.onesafe.commonui.animation.slideHorizontalPopEnterTransition
import studio.lunabee.onesafe.commonui.animation.slideHorizontalPopExitTransition
import studio.lunabee.onesafe.commonui.login.screen.KeyboardLoginRoute
import studio.lunabee.onesafe.commonui.navigation.LoginDestination
import studio.lunabee.onesafe.ime.viewmodel.LoginViewModelFactory
import studio.lunabee.onesafe.ime.viewmodel.SelectContactViewModelFactory
import studio.lunabee.onesafe.ime.viewmodel.WriteMessageViewModelFactory
import studio.lunabee.onesafe.messaging.writemessage.destination.WriteMessageDestination
import studio.lunabee.onesafe.messaging.writemessage.screen.WriteMessageRoute

@Composable
fun ImeNavGraph(
    navController: NavHostController,
    loginViewModelFactory: Lazy<LoginViewModelFactory>,
    selectContactViewModelFactory: Lazy<SelectContactViewModelFactory>,
    writeMessageViewModelFactory: Lazy<WriteMessageViewModelFactory>,
    onLoginSuccess: () -> Unit,
    dismissUi: () -> Unit,
    sendMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = LoginDestination.route,
        enterTransition = slideHorizontalEnterTransition,
        exitTransition = slideHorizontalExitTransition,
        popEnterTransition = slideHorizontalPopEnterTransition,
        popExitTransition = slideHorizontalPopExitTransition,
        modifier = modifier,
    ) {
        composable(
            route = LoginDestination.route,
        ) { backStackEntry ->
            KeyboardLoginRoute(
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
                onClose = dismissUi,
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
                exitIcon = R.drawable.ic_close,
            )
        }

        composable(
            route = WriteMessageDestination.route,
        ) { backStackEntry ->
            WriteMessageRoute(
                onChangeRecipient = { navController.navigate(ChangeContactDestination.route) },
                sendMessage = sendMessage,
                viewModel = viewModel(
                    viewModelStoreOwner = backStackEntry,
                    factory = writeMessageViewModelFactory.get(),
                ),
                contactIdFlow = backStackEntry.savedStateHandle.getStateFlow(WriteMessageDestination.ContactIdArgs, null),
                navigationToInvitation = {
                    // NO OP
                },
                sendIcon = OSImageSpec.Drawable(studio.lunabee.onesafe.messaging.R.drawable.ic_send),
                onBackClick = dismissUi,
                navigateToContactDetail = {},
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
