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
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import dagger.Lazy
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.animation.slideHorizontalEnterTransition
import studio.lunabee.onesafe.commonui.animation.slideHorizontalExitTransition
import studio.lunabee.onesafe.commonui.animation.slideHorizontalPopEnterTransition
import studio.lunabee.onesafe.commonui.animation.slideHorizontalPopExitTransition
import studio.lunabee.onesafe.ime.ImeDeeplinkHelper
import studio.lunabee.onesafe.ime.ui.contact.ChangeContactDestination
import studio.lunabee.onesafe.ime.ui.contact.ImeContactRoute
import studio.lunabee.onesafe.ime.ui.contact.SelectContactDestination
import studio.lunabee.onesafe.ime.viewmodel.ImeLoginViewModelFactory
import studio.lunabee.onesafe.ime.viewmodel.SelectContactViewModelFactory
import studio.lunabee.onesafe.ime.viewmodel.WriteMessageViewModelFactory
import studio.lunabee.onesafe.login.screen.LoginDestination
import studio.lunabee.onesafe.messaging.writemessage.destination.WriteMessageDestination
import studio.lunabee.onesafe.messaging.writemessage.screen.WriteMessageRoute
import studio.lunabee.onesafe.messaging.writemessage.viewmodel.WriteMessageViewModel

internal const val ImeNavGraphRoute: String = "ime_nav_host"

// TODO <oSK> extract NavGraphBuilder extensions

@Composable
fun ImeNavGraph(
    navController: NavHostController,
    imeLoginViewModelFactory: Lazy<ImeLoginViewModelFactory>,
    selectContactViewModelFactory: Lazy<SelectContactViewModelFactory>,
    writeMessageViewModelFactory: Lazy<WriteMessageViewModelFactory>,
    onLoginSuccess: () -> Unit,
    dismissUi: () -> Unit,
    sendMessage: (String) -> Unit,
    hasDoneOnBoardingBubbles: Boolean,
    modifier: Modifier = Modifier,
    hideKeyboard: () -> Unit,
) {
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = LoginDestination.route,
        enterTransition = slideHorizontalEnterTransition,
        exitTransition = slideHorizontalExitTransition,
        popEnterTransition = slideHorizontalPopEnterTransition,
        popExitTransition = slideHorizontalPopExitTransition,
        route = ImeNavGraphRoute,
        modifier = modifier,
    ) {
        composable(
            route = LoginDestination.route,
        ) { backStackEntry ->
            ImeLoginRoute(
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
                    factory = imeLoginViewModelFactory.get(),
                ),
            )
        }

        composable(
            route = SelectContactDestination.route,
        ) { backStackEntry ->
            ImeContactRoute(
                navigateBack = dismissUi,
                navigateToWriteMessage = {
                    navController.navigate(WriteMessageDestination.getRouteFromContactId(it))
                },
                viewModel = viewModel(
                    viewModelStoreOwner = backStackEntry,
                    factory = selectContactViewModelFactory.get(),
                ),
                exitIcon = OSDrawable.ic_close,
                deeplinkBubblesHomeContact = {
                    dismissUi()
                    if (hasDoneOnBoardingBubbles) {
                        ImeDeeplinkHelper.deeplinkBubblesHomeContact(context)
                    } else {
                        ImeDeeplinkHelper.deeplinkBubblesOnboarding(context)
                    }
                },
                deeplinkBubblesWriteMessage = { contactId ->
                    dismissUi()
                    ImeDeeplinkHelper.deeplinkBubblesWriteMessage(context, contactId)
                },
            )
        }

        composable(
            route = WriteMessageDestination.Route,
        ) { backStackEntry ->
            with(ImeWriteMessageNav(dismissUi, context)) {
                val viewModel: WriteMessageViewModel = viewModel(
                    viewModelStoreOwner = backStackEntry,
                    factory = writeMessageViewModelFactory.get(),
                )

                WriteMessageRoute(
                    onChangeRecipient = { navController.navigate(ChangeContactDestination.route) },
                    sendMessage = { sentMessageData, messageToSend ->
                        viewModel.saveEncryptedMessage(sentMessageData)
                        sendMessage(messageToSend)
                    },
                    resendMessage = sendMessage,
                    viewModel = viewModel,
                    contactIdFlow = backStackEntry.savedStateHandle.getStateFlow(WriteMessageDestination.ContactIdArg, null),
                    sendIcon = OSImageSpec.Drawable(OSDrawable.ic_send),
                    hideKeyboard = hideKeyboard,
                )
            }
        }

        composable(
            route = ChangeContactDestination.route,
        ) { backStackEntry ->
            val viewModelStoreOwner = remember(backStackEntry) {
                navController.getBackStackEntry(SelectContactDestination.route)
            }
            ImeContactRoute(
                navigateBack = { navController.popBackStack() },
                navigateToWriteMessage = { contactId ->
                    navController.getBackStackEntry(WriteMessageDestination.Route)
                        .savedStateHandle[WriteMessageDestination.ContactIdArg] = contactId.toString()
                    navController.popBackStack()
                },
                viewModel = viewModel(
                    viewModelStoreOwner = viewModelStoreOwner,
                    factory = selectContactViewModelFactory.get(),
                ),
                exitIcon = OSDrawable.ic_back,
                deeplinkBubblesHomeContact = {
                    dismissUi()
                    ImeDeeplinkHelper.deeplinkBubblesHomeContact(context)
                },
                deeplinkBubblesWriteMessage = { contactId ->
                    dismissUi()
                    ImeDeeplinkHelper.deeplinkBubblesWriteMessage(context, contactId)
                },
            )
        }
    }
}
