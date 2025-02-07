package studio.lunabee.onesafe.feature.itemform.viewmodel.delegate.creation

import android.content.Context
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import com.lunabee.lbcore.model.LBResult
import com.lunabee.lbextensions.enumValueOfOrNull
import com.lunabee.lblogger.LBLogger
import com.lunabee.lblogger.e
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import studio.lunabee.onesafe.domain.common.FieldIdProvider
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.domain.utils.FileHelper.clearExtension
import studio.lunabee.onesafe.feature.camera.model.CameraData
import studio.lunabee.onesafe.feature.home.model.ItemCreationEntryWithTemplate
import studio.lunabee.onesafe.feature.itemform.destination.ItemCreationDestination
import studio.lunabee.onesafe.feature.itemform.manager.ItemEditionDataManagerDefault
import studio.lunabee.onesafe.feature.itemform.manager.ItemEditionFileFieldManager
import studio.lunabee.onesafe.feature.itemform.manager.ResizeImageManager
import studio.lunabee.onesafe.feature.itemform.manager.UrlMetadataManager
import studio.lunabee.onesafe.feature.itemform.model.ItemCreationTemplateUiField
import studio.lunabee.onesafe.feature.itemform.model.ItemDataForm
import studio.lunabee.onesafe.feature.itemform.model.uifield.file.FileUiField
import studio.lunabee.onesafe.feature.itemform.model.uifield.file.ThumbnailState
import studio.lunabee.onesafe.feature.itemform.viewmodel.delegate.ItemFormInitialInfo
import studio.lunabee.onesafe.feature.itemform.viewmodel.delegate.PopulateScreenDelegate
import javax.inject.Inject

private val logger = LBLogger.get<PopulateScreenFromTemplateDelegate>()

class PopulateScreenFromTemplateDelegate @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val itemEditionFileManager: ItemEditionFileFieldManager,
    private val itemEditionDataManager: ItemEditionDataManagerDefault,
    private val fieldIdProvider: FieldIdProvider,
    @ApplicationContext private val context: Context,
    private val resizeImageManager: ResizeImageManager,
    private val urlMetadataManager: UrlMetadataManager,
) : PopulateScreenDelegate {

    private val itemTemplate: ItemCreationEntryWithTemplate.Template =
        enumValueOfOrNull<ItemCreationEntryWithTemplate.Template>(
            savedStateHandle.get<String>(ItemCreationDestination.ItemTypeArg),
        ) ?: ItemCreationEntryWithTemplate.Template.Custom

    override suspend fun getInitialInfo(): LBResult<ItemFormInitialInfo> {
        val fileUri = savedStateHandle.get<String>(ItemCreationDestination.FileUri)?.let {
            Json.decodeFromString<Array<String>>(it).map(Uri::parse)
        } ?: listOf()
        val cameraData = savedStateHandle.get<String>(ItemCreationDestination.CameraData)?.let {
            Json.decodeFromString<CameraData>(it)
        }

        val (fileUiField: List<FileUiField>, fileUiFieldError: Throwable?) = if (cameraData != null) {
            val result = itemEditionFileManager.manageMediaCaptured(numberOfImage = 0, cameraData = cameraData)
            listOfNotNull(result.data) to (result as? LBResult.Failure)?.throwable
        } else {
            fileUri.mapNotNull(itemEditionFileManager::createItemFileField) to null
        }
        val uiFields = ItemCreationTemplateUiField.fromItemCreationTemplate(
            fieldIdProvider = fieldIdProvider,
            itemCreationTemplate = itemTemplate,
        )

        val urlFromClipboard: String? = savedStateHandle[ItemCreationDestination.UrlFromClipboard]
        urlFromClipboard?.let { safeUrlFromClipboard ->
            uiFields.firstOrNull { it.safeItemFieldKind == SafeItemFieldKind.Url }?.initValues(false, safeUrlFromClipboard)
            urlMetadataManager.fetchUrlMetadataIfNeeded(safeUrlFromClipboard)
        }

        val fields = uiFields + fileUiField
        val name = fileUiField.firstOrNull()?.fieldDescription?.value?.string(context)?.clearExtension()

        val data = ItemFormInitialInfo(
            name = name.orEmpty(),
            icon = null,
            color = savedStateHandle.get<Int>(ItemCreationDestination.ItemParentColorArg)?.takeIf {
                it != -1
            }?.let { Color(it) },
            fields = fields,
            isFromCamera = cameraData != null,
        )

        return if (fileUiFieldError == null) {
            LBResult.Success(data)
        } else {
            logger.e(fileUiFieldError)
            LBResult.Failure(fileUiFieldError, data)
        }
    }

    override fun CoroutineScope.loadInitialInfo(info: ItemFormInitialInfo): Job = launch {
        val thumbnail = info.fields
            .asSequence()
            .filterIsInstance<FileUiField>()
            .firstOrNull()
            ?.thumbnailFlow
            ?.filterIsInstance<ThumbnailState.Finished>()
            ?.first()
            ?.thumbnail
            ?.let {
                resizeImageManager.resizeImage(context, it)
            }

        // Generate color from file or get parent color
        val extractedColor = thumbnail?.let { itemEditionDataManager.getColorFromImage(it) } ?: savedStateHandle.get<Int>(
            ItemCreationDestination.ItemParentColorArg,
        )?.takeIf { it != -1 }?.let(::Color)

        itemEditionDataManager.setInitialIconNameAndColor(
            icon = thumbnail,
            color = extractedColor?.let { ItemDataForm(extractedColor, false) },
            name = info.name,
        )
    }
}
