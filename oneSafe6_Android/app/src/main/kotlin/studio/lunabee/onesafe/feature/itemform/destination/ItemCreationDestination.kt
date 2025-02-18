package studio.lunabee.onesafe.feature.itemform.destination

import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import studio.lunabee.onesafe.commonui.OSDestination
import studio.lunabee.onesafe.feature.camera.model.CameraData
import studio.lunabee.onesafe.feature.home.model.ItemCreationEntryWithTemplate
import studio.lunabee.onesafe.feature.itemform.screen.ItemCreationRoute
import java.util.UUID

object ItemCreationDestination : OSDestination {
    const val ItemTypeArg: String = "itemTypeArgument"
    const val CameraData: String = "cameraData"
    const val FileUri: String = "fileUri"
    const val UrlFromClipboard: String = "UrlFromClipboard"
    const val ItemParentIdArg: String = "ItemParentIdArgument"
    const val ItemParentColorArg: String = "ItemParentColorArg"
    const val path: String = "itemCreation"

    override val route: String = "$path?" +
        "$ItemTypeArg={$ItemTypeArg}" +
        "&$CameraData={$CameraData}" +
        "&$ItemParentIdArg={$ItemParentIdArg}" +
        "&$ItemParentColorArg={$ItemParentColorArg}" +
        "&$FileUri={$FileUri}" +
        "&$UrlFromClipboard={$UrlFromClipboard}"

    fun getRouteFromTemplate(
        itemType: ItemCreationEntryWithTemplate.Template,
        itemParentId: UUID?,
        color: Color?,
        urlFromClipboard: String?,
    ): String {
        return Uri.Builder().apply {
            path(path)
            appendQueryParameter(ItemTypeArg, itemType.name)
            itemParentId?.let { appendQueryParameter(ItemParentIdArg, itemParentId.toString()) }
            color?.let { appendQueryParameter(ItemParentColorArg, color.toArgb().toString()) }
            urlFromClipboard?.let { appendQueryParameter(UrlFromClipboard, urlFromClipboard) }
        }.build().toString()
    }

    fun getRouteFromFileUri(
        uriList: List<Uri>,
        itemParentId: UUID?,
        color: Color?,
    ): String {
        return Uri.Builder().apply {
            path(path)
            appendQueryParameter(FileUri, Json.encodeToString(uriList.map { it.toString() }))
            itemParentId?.let { appendQueryParameter(ItemParentIdArg, itemParentId.toString()) }
            color?.let { appendQueryParameter(ItemParentColorArg, color.toArgb().toString()) }
        }.build().toString()
    }

    fun getRouteFromCamera(
        itemParentId: UUID?,
        color: Color?,
        cameraData: CameraData,
    ): String {
        return Uri.Builder().apply {
            path(path)
            appendQueryParameter(CameraData, Json.encodeToString(cameraData))
            itemParentId?.let { appendQueryParameter(ItemParentIdArg, itemParentId.toString()) }
            color?.let { appendQueryParameter(ItemParentColorArg, color.toArgb().toString()) }
        }.build().toString()
    }
}

fun NavGraphBuilder.itemCreationGraph(
    navigateBack: () -> Unit,
    navigateToItemDetails: (safeItemId: UUID) -> Unit,
) {
    composable(
        route = ItemCreationDestination.route,
        arguments = listOf(
            navArgument(ItemCreationDestination.ItemTypeArg) {
                type = NavType.StringType
                nullable = true
            },
            navArgument(ItemCreationDestination.FileUri) {
                type = NavType.StringType
                nullable = true
            },
            navArgument(ItemCreationDestination.CameraData) {
                type = NavType.StringType
                nullable = true
            },
            navArgument(ItemCreationDestination.ItemParentIdArg) {
                type = NavType.StringType
                nullable = true
            },
            navArgument(ItemCreationDestination.ItemParentColorArg) {
                type = NavType.IntType
                defaultValue = -1
            },
        ),
    ) {
        ItemCreationRoute(
            navigateBack = navigateBack,
            navigateToItemDetails = navigateToItemDetails,
        )
    }
}
