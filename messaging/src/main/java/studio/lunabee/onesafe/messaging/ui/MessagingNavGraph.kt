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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import studio.lunabee.onesafe.bubbles.ui.barcode.ScanBarcodeNavScope
import studio.lunabee.onesafe.bubbles.ui.barcode.scanBarcodeScreen
import studio.lunabee.onesafe.bubbles.ui.contact.detail.ContactDetailNavScope
import studio.lunabee.onesafe.bubbles.ui.contact.detail.contactDetailScreen
import studio.lunabee.onesafe.bubbles.ui.contact.form.edit.EditContactNavScope
import studio.lunabee.onesafe.bubbles.ui.contact.form.edit.editContactScreen
import studio.lunabee.onesafe.bubbles.ui.contact.form.frominvitation.CreateContactFromInvitationNavScope
import studio.lunabee.onesafe.bubbles.ui.contact.form.frominvitation.createContactFromInvitationScreen
import studio.lunabee.onesafe.bubbles.ui.contact.form.fromscratch.CreateContactFromScratchNavScope
import studio.lunabee.onesafe.bubbles.ui.contact.form.fromscratch.createContactFromScratchScreen
import studio.lunabee.onesafe.bubbles.ui.decryptmessage.DecryptMessageNavScope
import studio.lunabee.onesafe.bubbles.ui.decryptmessage.decryptMessageScreen
import studio.lunabee.onesafe.bubbles.ui.home.BubblesHomeDestination
import studio.lunabee.onesafe.bubbles.ui.home.BubblesHomeNavScope
import studio.lunabee.onesafe.bubbles.ui.home.bubblesHomeScreen
import studio.lunabee.onesafe.bubbles.ui.invitation.InvitationNavScope
import studio.lunabee.onesafe.bubbles.ui.invitation.invitationScreen
import studio.lunabee.onesafe.bubbles.ui.invitationresponse.InvitationResponseNavScope
import studio.lunabee.onesafe.bubbles.ui.invitationresponse.invitationResponseScreen
import studio.lunabee.onesafe.bubbles.ui.welcome.OnBoardingBubblesNavScope
import studio.lunabee.onesafe.bubbles.ui.welcome.onBoardingBubblesScreen
import studio.lunabee.onesafe.commonui.OSDestination
import studio.lunabee.onesafe.messaging.writemessage.destination.writeMessageScreen
import studio.lunabee.onesafe.messaging.writemessage.screen.WriteMessageNavScope
import studio.lunabee.onesafe.messaging.writemessage.viewmodel.MessagingViewModel

interface MessagingGraphNavScope :
    BubblesHomeNavScope,
    ContactDetailNavScope,
    InvitationNavScope,
    InvitationResponseNavScope,
    EditContactNavScope,
    CreateContactFromInvitationNavScope,
    ScanBarcodeNavScope,
    CreateContactFromScratchNavScope,
    OnBoardingBubblesNavScope,
    DecryptMessageNavScope,
    WriteMessageNavScope

context(MessagingGraphNavScope)
fun NavGraphBuilder.messagingNavGraph(
    navController: NavController,
) {
    navigation(startDestination = BubblesHomeDestination.route, route = MessagingDestination.route) {
        bubblesHomeScreen(
            createMessagingViewModel = { messagingViewModel(it, navController) },
        )
        writeMessageScreen(
            createMessagingViewModel = { messagingViewModel(it, navController) },
        )
        contactDetailScreen()
        invitationScreen()
        invitationResponseScreen()
        createContactFromScratchScreen()
        editContactScreen()
        createContactFromInvitationScreen()
        scanBarcodeScreen()
        onBoardingBubblesScreen()
        decryptMessageScreen()
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
