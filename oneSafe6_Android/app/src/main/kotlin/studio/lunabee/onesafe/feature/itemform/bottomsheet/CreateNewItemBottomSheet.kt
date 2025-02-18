@file:OptIn(ExperimentalMaterial3Api::class)

package studio.lunabee.onesafe.feature.itemform.bottomsheet

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.animation.OSAnimatedNullableVisibility
import studio.lunabee.onesafe.atom.OSRegularDivider
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.common.composable.OSCameraPicker
import studio.lunabee.onesafe.common.composable.rememberOSCameraPicker
import studio.lunabee.onesafe.commonui.utils.FileDetails
import studio.lunabee.onesafe.common.utils.clipboard.OSClipboardFilter
import studio.lunabee.onesafe.common.utils.clipboard.OSClipboardManager
import studio.lunabee.onesafe.common.utils.clipboard.rememberOSClipboardManager
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.bottomsheet.BottomSheetHolder
import studio.lunabee.onesafe.commonui.bottomsheet.BottomSheetHolderColumnContent
import studio.lunabee.onesafe.commonui.dialog.DefaultAlertDialog
import studio.lunabee.onesafe.commonui.dialog.DialogState
import studio.lunabee.onesafe.commonui.dialog.rememberDialogState
import studio.lunabee.onesafe.domain.Constant
import studio.lunabee.onesafe.feature.camera.model.CameraData
import studio.lunabee.onesafe.feature.camera.model.CaptureConfig
import studio.lunabee.onesafe.feature.camera.model.OSMediaType
import studio.lunabee.onesafe.feature.dialog.FeatureComingDialogState
import studio.lunabee.onesafe.feature.home.model.ItemCreationEntry
import studio.lunabee.onesafe.feature.home.model.ItemCreationEntryFile
import studio.lunabee.onesafe.feature.home.model.ItemCreationEntryWithTemplate
import studio.lunabee.onesafe.feature.itemform.manager.InAppMediaCapture
import studio.lunabee.onesafe.feature.itemform.model.FileToLargeDialogState
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem
import studio.lunabee.onesafe.ui.theme.OSPreviewOnSurfaceTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateNewItemBottomSheet(
    isVisible: Boolean,
    onBottomSheetClosed: () -> Unit,
    onItemWithTemplateClicked: (template: ItemCreationEntryWithTemplate) -> Unit,
    onFileSelected: (List<Uri>) -> Unit,
    cameraData: CameraData,
    onImageCaptureFromCameraResult: (CameraData) -> Unit,
    itemCreationTemplateSections: LinkedHashSet<LinkedHashSet<ItemCreationEntry>> = defaultSections,
) {
    val context: Context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val cameraPicker = rememberOSCameraPicker(
        onCancel = onBottomSheetClosed,
        onImageCaptureFromCamera = onImageCaptureFromCameraResult,
        cameraData = cameraData,
        snackbarHostState = snackbarHostState,
        captureConfig = CaptureConfig.FieldFile,
    )

    var dialogState by rememberDialogState()
    dialogState?.DefaultAlertDialog()

    val checkFileAndSelect: (List<Uri>) -> Unit = { uriList ->
        val fileDetailsList = uriList.map {
            FileDetails.fromUri(it, context)
        }
        if (fileDetailsList.any { (it?.size ?: 0) > Constant.FileMaxSizeBytes }) {
            dialogState = FileToLargeDialogState { dialogState = null }
        } else {
            onFileSelected(uriList)
        }
    }

    val pickFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uriList ->
            if (uriList.isNotEmpty()) {
                checkFileAndSelect(uriList)
            } else {
                onBottomSheetClosed()
            }
        },
    )

    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uriList ->
            if (uriList.isNotEmpty()) {
                checkFileAndSelect(uriList)
            } else {
                onBottomSheetClosed()
            }
        },
    )

    val scrollState = rememberScrollState()
    BottomSheetHolder(
        isVisible = isVisible,
        onBottomSheetClosed = {
            onBottomSheetClosed()
            snackbarHostState.currentSnackbarData?.dismiss()
        },
        skipPartiallyExpanded = true,
        snackbarHost = { sheetState ->
            SnackbarHostBottomSheet(snackbarHostState, sheetState, scrollState)
        },
    ) { _, paddingValues ->
        BottomSheetHolderColumnContent(
            paddingValues = paddingValues,
            modifier = Modifier
                .testTag(tag = UiConstants.TestTag.BottomSheet.CreateItemBottomSheet),
            scrollState = scrollState,
        ) {
            CreateNewItemBottomSheetContent(
                onItemWithTemplateClicked = onItemWithTemplateClicked,
                itemCreationTemplateSections = itemCreationTemplateSections,
                cameraPicker = cameraPicker,
                onClickFileExplorer = pickFileLauncher::launch,
                onClickGallery = pickMediaLauncher::launch,
            ) { dialogState = it }
        }
    }
}

@Composable
private fun CreateNewItemBottomSheetContent(
    onItemWithTemplateClicked: (template: ItemCreationEntryWithTemplate) -> Unit,
    itemCreationTemplateSections: LinkedHashSet<LinkedHashSet<ItemCreationEntry>>,
    cameraPicker: OSCameraPicker,
    onClickFileExplorer: (String) -> Unit,
    onClickGallery: (PickVisualMediaRequest) -> Unit,
    setDialogState: (DialogState?) -> Unit,
) {
    val context: Context = LocalContext.current
    val osUrlClipboardManager: OSClipboardManager = rememberOSClipboardManager(osClipboardFilter = OSClipboardFilter.Url)
    val clipboardContent: String? by osUrlClipboardManager.clipboardContent.collectAsStateWithLifecycle()
    osUrlClipboardManager.HandleClipboardContent()

    OSText(
        text = LbcTextSpec.StringResource(id = OSString.breadcrumb_menu_title),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier
            .padding(horizontal = OSDimens.SystemSpacing.Regular)
            .padding(top = OSDimens.SystemSpacing.Regular),
    )

    OSAnimatedNullableVisibility(
        value = clipboardContent,
        modifier = Modifier
            .fillMaxWidth(),
        enter = expandVertically(),
        exit = shrinkVertically(),
    ) { urlFromClipboard ->
        Column {
            val websiteFromClipboard = ItemCreationEntryWithTemplate.WebsiteFromClipboard(urlFromClipboard)
            websiteFromClipboard.actionButton(
                LocalDesignSystem.current.getRowClickablePaddingValuesDependingOnIndex(index = 0, elementsCount = 1),
                onClick = { onItemWithTemplateClicked(websiteFromClipboard) },
            ).Composable()
            OSRegularDivider(Modifier.padding(horizontal = OSDimens.SystemSpacing.Regular))
        }
    }

    itemCreationTemplateSections.forEachIndexed { groupIndex, creationTemplates ->
        creationTemplates.sortedBy { it.text.string(context) }.forEachIndexed { index, creationEntry ->
            val padding = LocalDesignSystem.current.getRowClickablePaddingValuesDependingOnIndex(
                index = index,
                elementsCount = creationTemplates.size,
            )
            creationEntry.actionButton(padding) {
                when {
                    creationEntry.state == ItemCreationEntry.ItemCreationSortState.ComingSoon -> {
                        setDialogState(FeatureComingDialogState { setDialogState(null) })
                    }
                    creationEntry is ItemCreationEntryFile -> {
                        when (creationEntry) {
                            ItemCreationEntryFile.FileExplorer -> {
                                onClickFileExplorer(AppConstants.FileProvider.LauncherAllFileFilter)
                            }
                            ItemCreationEntryFile.Gallery -> {
                                val request = PickVisualMediaRequest.Builder().build()
                                onClickGallery(request)
                            }
                            ItemCreationEntryFile.Camera -> {
                                cameraPicker.onFileFromCameraRequested()
                            }
                        }
                    }
                    creationEntry is ItemCreationEntryWithTemplate -> {
                        onItemWithTemplateClicked(creationEntry)
                    }
                }
            }.Composable()
        }

        if (groupIndex != itemCreationTemplateSections.indices.last) {
            OSRegularDivider(Modifier.padding(horizontal = OSDimens.SystemSpacing.Regular))
        }
    }
}

private val defaultSections: LinkedHashSet<LinkedHashSet<ItemCreationEntry>> = linkedSetOf(
    linkedSetOf(
        ItemCreationEntryWithTemplate.Application,
        ItemCreationEntryWithTemplate.CreditCard,
        ItemCreationEntryWithTemplate.Folder,
        ItemCreationEntryWithTemplate.Website,
        ItemCreationEntryWithTemplate.Note,
    ),
    linkedSetOf(
        ItemCreationEntryFile.FileExplorer,
        ItemCreationEntryFile.Gallery,
        ItemCreationEntryFile.Camera,
    ),
    linkedSetOf(
        ItemCreationEntryWithTemplate.Custom,
    ),
)

@Composable
@OsDefaultPreview
private fun CreateNewItemBottomSheetPreview() {
    OSPreviewOnSurfaceTheme {
        Column {
            CreateNewItemBottomSheetContent(
                onItemWithTemplateClicked = {},
                itemCreationTemplateSections = defaultSections,
                cameraPicker = rememberOSCameraPicker(
                    onCancel = {},
                    onImageCaptureFromCamera = {},
                    cameraData = CameraData.InApp(InAppMediaCapture(null, null, OSMediaType.PHOTO)),
                    snackbarHostState = SnackbarHostState(),
                    captureConfig = CaptureConfig.ItemIcon,
                ),
                onClickFileExplorer = {},
                onClickGallery = {},
            ) {}
        }
    }
}
