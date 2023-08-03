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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.bubbles.ui.app.BubbleDestination
import studio.lunabee.onesafe.bubbles.ui.app.bubbleHomeGraph
import studio.lunabee.onesafe.bubbles.ui.barcode.ScanBarcodeDestination
import studio.lunabee.onesafe.bubbles.ui.barcode.scanBarcodeDestination
import studio.lunabee.onesafe.bubbles.ui.contact.detail.ContactDetailDestination
import studio.lunabee.onesafe.bubbles.ui.contact.detail.contactDetailGraph
import studio.lunabee.onesafe.bubbles.ui.contact.frominvitation.CreateContactFromInvitationDestination
import studio.lunabee.onesafe.bubbles.ui.contact.frominvitation.createContactFromInvitationGraph
import studio.lunabee.onesafe.bubbles.ui.contact.fromscratch.CreateContactFromScratchDestination
import studio.lunabee.onesafe.bubbles.ui.contact.fromscratch.createContactFromScratchDestination
import studio.lunabee.onesafe.bubbles.ui.decryptmessage.DecryptMessageDestination
import studio.lunabee.onesafe.bubbles.ui.decryptmessage.DecryptMessageRoute
import studio.lunabee.onesafe.bubbles.ui.invitation.InvitationDestination
import studio.lunabee.onesafe.bubbles.ui.invitation.invitationGraph
import studio.lunabee.onesafe.bubbles.ui.invitationresponse.InvitationResponseDestination
import studio.lunabee.onesafe.bubbles.ui.invitationresponse.invitationResponseGraph
import studio.lunabee.onesafe.bubbles.ui.welcome.OnBoardingBubblesDestination
import studio.lunabee.onesafe.bubbles.ui.welcome.onBoardingBubblesGraph
import studio.lunabee.onesafe.commonui.R
import studio.lunabee.onesafe.commonui.extension.getTextSharingIntent
import studio.lunabee.onesafe.commonui.navigation.OSDestination
import studio.lunabee.onesafe.messaging.writemessage.composable.WriteMessageExitIcon
import studio.lunabee.onesafe.messaging.writemessage.destination.WriteMessageDestination
import studio.lunabee.onesafe.messaging.writemessage.screen.WriteMessageRoute
import studio.lunabee.onesafe.messaging.writemessage.viewmodel.MessagingViewModel

fun NavGraphBuilder.messagingNavGraph(
    navigateBack: () -> Unit,
    navController: NavController,
    navigateToOneSafeKSettings: () -> Unit,
) {
    navigation(startDestination = BubbleDestination.route, route = MessagingDestination.route) {
        bubbleHomeGraph(
            navigateBack = navigateBack,
            navigateToContact = { contactId -> navController.navigate(ContactDetailDestination.getRoute(contactId)) },
            navigateToConversation = { contactId -> navController.navigate(WriteMessageDestination.getRoute(contactId)) },
            navigateToCreateContact = { navController.navigate(CreateContactFromScratchDestination.route) },
            navigateToQrScan = { navController.navigate(ScanBarcodeDestination.route) },
            navigateToSettings = navigateToOneSafeKSettings,
            createMessagingViewModel = { messagingViewModel(it, navController) },
            navigateToDecryptMessage = { navController.navigate(DecryptMessageDestination.route) },
        )

        contactDetailGraph(
            navigateBack = navigateBack,
            navigateToConversation = { contactId -> navController.navigate(WriteMessageDestination.getRoute(contactId)) },
            navigateToInvitationScreen = { contactId -> navController.navigate(InvitationDestination.getRoute(contactId)) },
            navigateToResponseScreen = { contactId -> navController.navigate(InvitationResponseDestination.getRoute(contactId)) },
            navigateToScanBarcodeScreen = { navController.navigate(ScanBarcodeDestination.route) },
        )

        invitationGraph(
            navigateBack = navigateBack,
            navigateToQrScan = { navController.navigate(ScanBarcodeDestination.route) },
            navigateToBubbleScreen = {
                navController.navigate(BubbleDestination.route) { popUpTo(BubbleDestination.route) { inclusive = true } }
            },
        )

        invitationResponseGraph(
            navigateBack = navigateBack,
            navigateToConversation = { contactId ->
                navController.navigate(WriteMessageDestination.getRoute(contactId))
            },
        )

        createContactFromScratchDestination(
            navigateBack = navigateBack,
            navigateToInvitationScreen = {
                navController.navigate(InvitationDestination.getRoute(it)) {
                    popUpTo(BubbleDestination.route) { inclusive = false }
                }
            },
        )

        createContactFromInvitationGraph(
            navigateBack = navigateBack,
            navigateToInvitationResponseScreen = {
                // pop self
                navController.popBackStack()
                // make sure we have bubble home in stack (deeplink case)
                navController.navigate(BubbleDestination.route) {
                    launchSingleTop = true
                }
                // don't use popUpTo so we handle both deeplink and standard nav
                navController.navigate(InvitationResponseDestination.getRoute(it))
            },
        )

        scanBarcodeDestination(
            navigateBack = navigateBack,
            navigateToCreateContact = {
                navController.navigate(CreateContactFromInvitationDestination.getRoute(it)) {
                    popUpTo(BubbleDestination.route) { inclusive = false }
                }
            },
            navigateToConversation = { contactId ->
                navController.navigate(WriteMessageDestination.getRoute(contactId)) {
                    popUpTo(BubbleDestination.route) { inclusive = false }
                }
            },
        )

        onBoardingBubblesGraph(
            navigateBack = navigateBack,
            navigateToBubbleHome = {
                navController.navigate(BubbleDestination.route) { popUpTo(OnBoardingBubblesDestination.route) { inclusive = true } }
            },
        )

        composable(
            route = DecryptMessageDestination.route,
        ) {
            DecryptMessageRoute(
                navigateBack = navigateBack,
                navigateToCreateContactFromInvitation = { message ->
                    navController.navigate(CreateContactFromInvitationDestination.getRoute(message)) {
                        popUpTo(BubbleDestination.route) { inclusive = false }
                    }
                },
                navigateToConversation = { contactId ->
                    navController.navigate(WriteMessageDestination.getRoute(contactId)) {
                        popUpTo(BubbleDestination.route) { inclusive = false }
                    }
                },
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
                    val intent = context.getTextSharingIntent(encMessage)
                    context.startActivity(intent)
                },
                contactIdFlow = backStackEntry.savedStateHandle.getStateFlow(WriteMessageDestination.ContactIdArgs, null),
                exitIcon = WriteMessageExitIcon.WriteMessageBackIcon(
                    onClick = navigateBack,
                ),
                navigationToInvitation = { navController.navigate(InvitationDestination.getRoute(it)) },
                sendIcon = OSImageSpec.Drawable(R.drawable.ic_share),
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
    val messagingViewModel = hiltViewModel<MessagingViewModel>(viewModelStoreOwner)
    LaunchedEffect(Unit) {
        backStackEntry.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            messagingViewModel.observeQueue()
        }
    }
    return messagingViewModel
}
