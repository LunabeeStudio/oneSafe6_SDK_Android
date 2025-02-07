package studio.lunabee.onesafe.feature.breadcrumb

import android.net.Uri
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import studio.lunabee.onesafe.bubbles.ui.welcome.OnBoardingBubblesDestination
import studio.lunabee.onesafe.commonui.utils.safeNavigate
import studio.lunabee.onesafe.feature.camera.model.CameraData
import studio.lunabee.onesafe.feature.fileviewer.FileViewerScreenDestination
import studio.lunabee.onesafe.feature.home.model.ItemCreationEntryWithTemplate
import studio.lunabee.onesafe.feature.importbackup.selectfile.ImportFileDestination
import studio.lunabee.onesafe.feature.itemfielddetail.ItemFieldDetailsScreenDestination
import studio.lunabee.onesafe.feature.itemform.destination.ItemCreationDestination
import studio.lunabee.onesafe.feature.itemform.destination.ItemEditionDestination
import studio.lunabee.onesafe.feature.move.movehost.MoveHostDestination
import studio.lunabee.onesafe.feature.settings.SettingsDestination
import studio.lunabee.onesafe.feature.settings.bubbles.BubblesSettingsDestination
import studio.lunabee.onesafe.feature.share.ShareNavGraphDestination
import studio.lunabee.onesafe.feature.verifypassword.VerifyPasswordNavGraphDestination
import studio.lunabee.onesafe.importexport.settings.AutoBackupSettingsNavGraphDestination
import studio.lunabee.onesafe.messaging.senditem.SendItemBubblesSelectContactDestination
import studio.lunabee.onesafe.messaging.ui.MessagingDestination
import studio.lunabee.onesafe.messaging.writemessage.destination.WriteMessageDestination
import studio.lunabee.onesafe.navigation.graph.BreadcrumbOnCompositionNav
import java.util.UUID

@Stable
class BreadcrumbNavigation(
    mainNavController: NavController,
    val navigateBack: () -> Unit,
    val onCompositionNav: () -> BreadcrumbOnCompositionNav?,
) {
    val navigateToSettings: () -> Unit = { mainNavController.safeNavigate(SettingsDestination.getRoute()) }
    val navigateToEditItem: (itemId: UUID) -> Unit = { itemId ->
        mainNavController.safeNavigate(route = ItemEditionDestination.getRoute(itemId = itemId))
    }
    val navigateToFullScreenField: (itemId: UUID, fieldId: UUID) -> Unit = { itemId, fieldId ->
        mainNavController.safeNavigate(
            route = ItemFieldDetailsScreenDestination.getRoute(
                fieldId = fieldId,
                itemId = itemId,
            ),
        )
    }
    val navigateToMove: (itemId: UUID) -> Unit = { itemId: UUID ->
        mainNavController.safeNavigate(MoveHostDestination.getRoute(itemId))
    }
    val navigateToShare: (itemId: UUID, includeChildren: Boolean) -> Unit = { itemId: UUID, includeChildren: Boolean ->
        mainNavController.safeNavigate(ShareNavGraphDestination.getRoute(itemId, includeChildren))
    }
    val navigateToItemCreationFromTemplate: (
        template: ItemCreationEntryWithTemplate.Template,
        itemParentId: UUID?,
        color: Color?,
        clipboardContent: String?,
    ) -> Unit =
        { template, itemParentId, color, clipboardContent ->
            mainNavController.safeNavigate(
                route = ItemCreationDestination.getRouteFromTemplate(
                    itemType = template,
                    itemParentId = itemParentId,
                    color = color,
                    urlFromClipboard = clipboardContent,
                ),
            )
        }

    val navigateToItemCreationFromFileUrl: (List<Uri>, UUID?, Color?) -> Unit = { uriList, itemParentId, color ->
        mainNavController.navigate(
            route = ItemCreationDestination.getRouteFromFileUri(
                uriList = uriList,
                itemParentId = itemParentId,
                color = color,
            ),
        )
    }

    val navigateToItemCreationFromCamera: (
        itemParentId: UUID?,
        color: Color?,
        cameraData: CameraData,
    ) -> Unit = { itemParentId, color, cameraData ->
        mainNavController.navigate(
            route = ItemCreationDestination.getRouteFromCamera(
                itemParentId = itemParentId,
                color = color,
                cameraData = cameraData,
            ),
        )
    }

    val navigateToVerifyPassword: () -> Unit = { mainNavController.safeNavigate(VerifyPasswordNavGraphDestination.route) }

    val navigateToBubblesHome: () -> Unit = {
        mainNavController.safeNavigate(MessagingDestination.route)
    }

    val navigateToBubbles: () -> Unit = {
        mainNavController.safeNavigate(BubblesSettingsDestination.route)
    }

    val navigateToConversation: (contactId: UUID) -> Unit = { contactId ->
        mainNavController.safeNavigate(WriteMessageDestination.getRouteFromContactId(contactId))
    }

    val navigateToBubblesOnBoarding: () -> Unit = {
        mainNavController.safeNavigate(OnBoardingBubblesDestination.route)
    }

    val navigateToImportItems: () -> Unit = {
        mainNavController.safeNavigate(ImportFileDestination.getRoute(null, false))
    }

    val navigateToFileViewer: (UUID) -> Unit = { fieldId ->
        mainNavController.safeNavigate(FileViewerScreenDestination.getRoute(fieldId))
    }

    val navigateToBackupSettings: () -> Unit = {
        mainNavController.safeNavigate(AutoBackupSettingsNavGraphDestination.route)
    }

    val navigateToSendItemViaBubbles: (itemId: UUID, includeChildren: Boolean) -> Unit = { itemId: UUID, includeChildren: Boolean ->
        mainNavController.safeNavigate(SendItemBubblesSelectContactDestination.getRoute(itemId, includeChildren))
    }
}
