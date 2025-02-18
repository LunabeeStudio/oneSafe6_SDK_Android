package studio.lunabee.onesafe.navigation.graph

import androidx.compose.material3.SnackbarVisuals
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import studio.lunabee.messaging.domain.model.DecryptResult
import studio.lunabee.onesafe.bubbles.ui.barcode.ScanBarcodeDestination
import studio.lunabee.onesafe.bubbles.ui.contact.detail.ContactDetailDestination
import studio.lunabee.onesafe.bubbles.ui.contact.form.edit.EditContactDestination
import studio.lunabee.onesafe.bubbles.ui.contact.form.frominvitation.CreateContactFromInvitationDestination
import studio.lunabee.onesafe.bubbles.ui.contact.form.fromscratch.CreateContactFromScratchDestination
import studio.lunabee.onesafe.bubbles.ui.decryptmessage.DecryptMessageDestination
import studio.lunabee.onesafe.bubbles.ui.home.BubblesHomeDestination
import studio.lunabee.onesafe.bubbles.ui.invitation.InvitationDestination
import studio.lunabee.onesafe.bubbles.ui.invitationresponse.InvitationResponseDestination
import studio.lunabee.onesafe.bubbles.ui.welcome.OnBoardingBubblesDestination
import studio.lunabee.onesafe.commonui.utils.safeNavigate
import studio.lunabee.onesafe.feature.breadcrumb.BreadcrumbNavGraphDestination
import studio.lunabee.onesafe.feature.itemdetails.ItemDetailsDestination
import studio.lunabee.onesafe.feature.settings.bubbles.BubblesSettingsDestination
import studio.lunabee.onesafe.messaging.senditem.SendItemBubblesSelectContactDestination
import studio.lunabee.onesafe.messaging.ui.MessagingGraphNavScope
import studio.lunabee.onesafe.messaging.writemessage.destination.WriteMessageDestination
import java.util.UUID

class MainMessagingGraphNavigation(
    private val breadcrumbNavController: NavController?,
    private val mainNavController: NavController,
    private val setBreadcrumbOnCompositionNav: (BreadcrumbOnCompositionNav) -> Unit,
    override val navigateBack: () -> Unit,
    override val showSnackbar: (visuals: SnackbarVisuals) -> Unit,
) : MessagingGraphNavScope {
    override val navigateToBubblesSettings: () -> Unit = { mainNavController.safeNavigate(BubblesSettingsDestination.route) }
    override val navigateToConversation: (contactId: UUID) -> Unit = { contactId ->
        mainNavController.safeNavigate(WriteMessageDestination.getRouteFromContactId(contactId))
    }
    override val navigateToContactDetail: (contactId: UUID) -> Unit = { contactId ->
        mainNavController.safeNavigate(ContactDetailDestination.getRoute(contactId))
    }
    override val navigateToCreateContact: () -> Unit = { mainNavController.safeNavigate(CreateContactFromScratchDestination.route) }
    override val navigateToDecryptMessage: () -> Unit = { mainNavController.safeNavigate(DecryptMessageDestination.route) }
    override val navigateToInvitation: (contactId: UUID) -> Unit = { contactId ->
        mainNavController.safeNavigate(InvitationDestination.getRoute(contactId))
    }
    override val navigateToResponse: (contactId: UUID) -> Unit = { contactId ->
        mainNavController.safeNavigate(InvitationResponseDestination.getRoute(contactId))
    }
    override val navigateToScanBarcode: () -> Unit = { mainNavController.safeNavigate(ScanBarcodeDestination.route) }
    override val navigateToContactEdition: (contactId: UUID) -> Unit = { contactId ->
        mainNavController.safeNavigate(EditContactDestination.getRoute(contactId))
    }
    override val navigateBackToBubbles: () -> Unit = {
        mainNavController.popBackStack(BubblesHomeDestination.route, inclusive = false)
    }
    override val navigateToBubblesHome: () -> Unit = {
        mainNavController.safeNavigate(BubblesHomeDestination.route) {
            popUpTo(BubblesHomeDestination.route) {
                inclusive = true
            }
        }
    }
    override val navigateToInvitationResponse: (UUID) -> Unit = {
        // pop self
        mainNavController.popBackStack()
        // make sure we have bubbles home in stack (deeplink case)
        mainNavController.navigate(BubblesHomeDestination.route) {
            launchSingleTop = true
        }
        // don't use popUpTo so we handle both deeplink and standard nav
        mainNavController.navigate(InvitationResponseDestination.getRoute(it))
    }
    override val navigateToCreateContactPopToHome: (String) -> Unit = { message ->
        mainNavController.safeNavigate(CreateContactFromInvitationDestination.getRoute(message)) {
            popUpTo(BubblesHomeDestination.route) { inclusive = false }
        }
    }
    override val navigateToConversationPopToHome: (DecryptResult) -> Unit = { result ->
        mainNavController.safeNavigate(WriteMessageDestination.getRouteFromDecryptResult(result)) {
            popUpTo(BubblesHomeDestination.route) { inclusive = false }
        }
    }
    override val navigateToInvitationPopToHome: (UUID) -> Unit = {
        mainNavController.safeNavigate(InvitationDestination.getRoute(it)) {
            popUpTo(BubblesHomeDestination.route) { inclusive = false }
        }
    }
    override val navigateOnBoardingToBubblesHome: () -> Unit = {
        mainNavController.safeNavigate(BubblesHomeDestination.route) {
            popUpTo(OnBoardingBubblesDestination.route) {
                inclusive = true
            }
        }
    }
    override val navigateDecryptMessageToConversation: (DecryptResult) -> Unit = { result ->
        mainNavController.safeNavigate(WriteMessageDestination.getRouteFromDecryptResult(result)) {
            popUpTo(DecryptMessageDestination.route) {
                inclusive = true
            }
        }
    }

    override val navigationToInvitation: (UUID) -> Unit = {
        mainNavController.safeNavigate(InvitationDestination.getRoute(it))
    }
    override val deeplinkBubblesWriteMessage: ((contactId: UUID) -> Unit)? = null
    override val navigateToItemDetail: (UUID) -> Unit = {
        val route = ItemDetailsDestination.getRoute(it)
        if (breadcrumbNavController?.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
            breadcrumbNavController.safeNavigate(
                route = route,
                bypassLifecycleCheck = true,
            )
        } else {
            setBreadcrumbOnCompositionNav(BreadcrumbOnCompositionNav.Navigate(route))
        }
        mainNavController.popBackStack(BreadcrumbNavGraphDestination.route, inclusive = false)
    }
    override val sendItemBubblesNavigateToBubblesHome: () -> Unit = {
        mainNavController.safeNavigate(BubblesHomeDestination.route) {
            popUpTo(SendItemBubblesSelectContactDestination.route) { inclusive = true }
        }
    }
    override val navigateToWriteMessage: (contactId: UUID) -> Unit = {
        mainNavController.safeNavigate(WriteMessageDestination.getRouteFromContactId(contactId = it)) {
            popUpTo(SendItemBubblesSelectContactDestination.route) { inclusive = true }
        }
    }
}
