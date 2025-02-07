package studio.lunabee.onesafe.feature.itemform.screen

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lunabee.lbloading.LoadingBackHandler
import studio.lunabee.compose.accessibility.state.AccessibilityState
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.accessibility.rememberOSAccessibilityState
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.common.composable.rememberOSCameraPicker
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.dialog.DefaultAlertDialog
import studio.lunabee.onesafe.commonui.dialog.DialogState
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.extension.loremIpsum
import studio.lunabee.onesafe.extension.loremIpsumSpec
import studio.lunabee.onesafe.feature.camera.model.CaptureConfig
import studio.lunabee.onesafe.feature.dialog.ExitConfirmationDialogState
import studio.lunabee.onesafe.feature.itemform.bottomsheet.color.ColorPickerBottomSheet
import studio.lunabee.onesafe.feature.itemform.bottomsheet.image.ItemIconPickerBottomSheet
import studio.lunabee.onesafe.feature.itemform.bottomsheet.newfield.AddNewFieldBottomSheet
import studio.lunabee.onesafe.feature.itemform.bottomsheet.newfile.AddNewFileBottomSheet
import studio.lunabee.onesafe.feature.itemform.composable.FieldsLayout
import studio.lunabee.onesafe.feature.itemform.composable.ItemFormActionList
import studio.lunabee.onesafe.feature.itemform.composable.ItemFormHeader
import studio.lunabee.onesafe.feature.itemform.composable.ItemFormTopAppBar
import studio.lunabee.onesafe.feature.itemform.model.ItemFormAction
import studio.lunabee.onesafe.feature.itemform.model.ItemFormActionsHolder
import studio.lunabee.onesafe.feature.itemform.model.ReorderOption
import studio.lunabee.onesafe.feature.itemform.model.uifield.UiField
import studio.lunabee.onesafe.feature.itemform.model.uifield.text.impl.NameTextUiField
import studio.lunabee.onesafe.feature.itemform.viewmodel.ItemFormViewModel
import studio.lunabee.onesafe.feature.itemform.viewmodel.impl.ItemCreationViewModel
import studio.lunabee.onesafe.feature.itemform.viewmodel.impl.ItemEditionViewModel
import studio.lunabee.onesafe.feature.itemform.viewmodel.state.ItemFormState
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme
import studio.lunabee.onesafe.ui.theme.OSUserTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview
import java.util.UUID

@Composable
fun ItemEditionRoute(
    navigateBack: () -> Unit,
) {
    ItemFormRoute(
        navigateBack = navigateBack,
        navigateToItemDetails = { navigateBack() }, // Edition can only navigate back
        viewModel = hiltViewModel<ItemEditionViewModel>(),
        screenTitle = LbcTextSpec.StringResource(OSString.common_edit),
    )
}

@Composable
fun ItemCreationRoute(
    navigateBack: () -> Unit,
    navigateToItemDetails: (safeItemId: UUID) -> Unit,
) {
    ItemFormRoute(
        navigateBack = navigateBack,
        navigateToItemDetails = navigateToItemDetails,
        viewModel = hiltViewModel<ItemCreationViewModel>(),
        screenTitle = LbcTextSpec.StringResource(OSString.safeItemDetail_newItem_title),
    )
}

@Composable
fun ItemFormRoute(
    navigateBack: () -> Unit,
    navigateToItemDetails: (safeItemId: UUID) -> Unit,
    viewModel: ItemFormViewModel,
    screenTitle: LbcTextSpec,
    accessibilityState: AccessibilityState = rememberOSAccessibilityState(),
) {
    val isTouchExplorationEnabled = rememberOSAccessibilityState().isTouchExplorationEnabled
    val context = LocalContext.current

    // Trigger for quitting screen
    val itemFormState by viewModel.itemFormState.collectAsStateWithLifecycle()

    // Bottom sheets visibility boolean
    var isColorPickerVisible by rememberSaveable { mutableStateOf(value = false) }
    var isItemIconPickerVisible by rememberSaveable { mutableStateOf(value = false) }
    var isNewFieldCreatorVisible by rememberSaveable { mutableStateOf(value = false) }
    var isNewFileCreatorVisible by rememberSaveable { mutableStateOf(value = false) }
    var reorderOption: ReorderOption by rememberSaveable { mutableStateOf(ReorderOption.TextFields) }

    val uiFields by viewModel.uiFields.collectAsStateWithLifecycle()

    // Fields and fieldsMapped to be saved (String instead of StringRes)
    val uiFieldToSave = uiFields.mapIndexed { index, uiField -> uiField.getItemFieldData(index = index) }

    val itemIconLoading by viewModel.itemIconLoading.collectAsStateWithLifecycle()

    // Dialog states from viewModel and only view
    val viewModelDialogState: DialogState? by viewModel.dialogState.collectAsStateWithLifecycle(null)
    var dialogState: DialogState? by remember { mutableStateOf(value = null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarState by viewModel.snackbarData.collectAsStateWithLifecycle(null)
    snackbarState?.LaunchedSnackbarEffect(snackbarHostState) {
        viewModel.consumeSnackBar()
    }

    val color by viewModel.colorPreview.collectAsStateWithLifecycle()
    val currentThemeColor = color ?: MaterialTheme.colorScheme.primary
    val itemIcon = viewModel.itemIcon.collectAsStateWithLifecycle().value
    val emojiAsItemIcon by viewModel.emojiAsItemIcon.collectAsStateWithLifecycle()

    val onBackClick: () -> Unit = {
        if (viewModel.isSaveEnabled() == OSActionState.Enabled) {
            dialogState = ExitConfirmationDialogState.getState(
                dismiss = { dialogState = null },
                navigateBack = navigateBack,
            )
        } else {
            navigateBack()
        }
    }

    LoadingBackHandler(onBack = onBackClick)
    OSUserTheme(customPrimaryColor = currentThemeColor) {
        // Dialogs
        viewModelDialogState?.DefaultAlertDialog()
        dialogState?.DefaultAlertDialog()

        // Redirection
        when (val state = itemFormState) {
            ItemFormState.Initializing -> {
                /* no-op but render fields */
            }
            is ItemFormState.Idle -> {
                val cameraPickerForField = rememberOSCameraPicker(
                    onCancel = { /* no-op */ },
                    onImageCaptureFromCamera = { cameraData -> viewModel.manageMediaCaptureFromCameraForField(cameraData) },
                    cameraData = state.cameraDataForField,
                    snackbarHostState = snackbarHostState,
                    captureConfig = CaptureConfig.FieldFile,
                )
                state.onCaptureAnother?.let { onCaptureAnother ->
                    cameraPickerForField.onFileFromCameraRequested()
                    onCaptureAnother()
                }

                // Bottom sheets
                AddNewFieldBottomSheet(
                    isVisible = isNewFieldCreatorVisible,
                    onBottomSheetClosed = { isNewFieldCreatorVisible = false },
                    onNewFieldRequested = viewModel::addField,
                )

                ColorPickerBottomSheet(
                    isVisible = isColorPickerVisible,
                    onBottomSheetClosed = {
                        isColorPickerVisible = false
                        viewModel.setColorSelected(null)
                    },
                    setColorSelectedByUser = viewModel::setColorSelected,
                    onValidate = viewModel::saveSelectedColor,
                    currentThemeColor = currentThemeColor,
                )

                AddNewFileBottomSheet(
                    isVisible = isNewFileCreatorVisible,
                    onBottomSheetClosed = { isNewFileCreatorVisible = false },
                    onFileSelected = viewModel::addFileField,
                    onImageCaptureFromCamera = { cameraData ->
                        isNewFileCreatorVisible = false
                        viewModel.manageMediaCaptureFromCameraForField(cameraData)
                    },
                    cameraData = state.cameraDataForField,
                )

                ItemIconPickerBottomSheet(
                    isVisible = isItemIconPickerVisible,
                    onBottomSheetClosed = { isItemIconPickerVisible = false },
                    hasImageToDisplay = itemIcon != null,
                    removeImageSelected = { viewModel.removeItemIconSelected() },
                    onImageCaptureFromCamera = { cameraData ->
                        isItemIconPickerVisible = false
                        viewModel.onItemIconCaptureFromCamera(cameraData)
                    },
                    onIconPickedByUser = { viewModel.onItemIconPickedByUser(it) },
                    cameraData = state.cameraDataForIcon,
                    onFetchFromUrl = viewModel::forceFetchDataFromUrl,
                    canFetchFromUrl = viewModel.canFetchFromUrl(),
                    onEnterUrlForIcon = viewModel::displayEnterUrlForIconDialog,
                )
            }
            is ItemFormState.ReOrderField -> {
                // No-op as we keep content always on display.
            }
            is ItemFormState.Exit -> {
                if (state.safeItemIdCreated == null) {
                    navigateBack()
                } else {
                    navigateToItemDetails(state.safeItemIdCreated)
                }
            }
        }

        // Main screen
        AnimatedContent(
            targetState = itemFormState is ItemFormState.ReOrderField,
            label = "ItemFormStateAnimatedContent",
            transitionSpec = {
                if (targetState) {
                    slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                } else {
                    slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
                }
            },
        ) { targetState ->
            if (targetState) {
                val initialList = uiFields.filter { reorderOption.filterFieldToReorder(it) }
                ItemReOrderScreen(
                    isSaveEnabled = { newList -> viewModel.isReOrderSaveEnabled(initialList, newList) },
                    initialList = initialList,
                    exitReOrderMode = viewModel::exitReOrderMode,
                    updateOrder = viewModel::updateFieldsOrder,
                )
            } else {
                ItemFormScreen(
                    saveState = viewModel.isSaveEnabled(),
                    navigateBack = onBackClick,
                    nameField = viewModel.nameField,
                    uiField = uiFields,
                    screenTitle = screenTitle,
                    currentImage = itemIcon,
                    placeHolder = emojiAsItemIcon?.let(LbcTextSpec::Raw),
                    itemIconLoading = itemIconLoading,
                    openColorPickerBottomSheet = { isColorPickerVisible = true },
                    openItemImagePickerBottomSheet = { isItemIconPickerVisible = true },
                    isTouchExplorationEnabled = accessibilityState.isTouchExplorationEnabled,
                    snackbarHostState = snackbarHostState,
                    toggleIdentifier = { uiField -> toggleIdentifier(viewModel, context, isTouchExplorationEnabled, uiField) },
                    renameField = { id, name -> renameField(viewModel, id, name, isTouchExplorationEnabled, context) },
                    removeField = { uiField -> removeField(viewModel, uiField, isTouchExplorationEnabled, context) },
                    onReorganizeFieldClick = {
                        reorderOption = it
                        viewModel.enterReOrderMode()
                    },
                    useThumbnailAsIcon = viewModel::onThumbnailSelected,
                    itemFormActionsHolder = ItemFormActionsHolder(
                        ItemFormAction.AddNewField(onClick = { isNewFieldCreatorVisible = true }),
                        ItemFormAction.AddNewFile(onClick = { isNewFileCreatorVisible = true }),
                        ItemFormAction.SaveForm(onClick = { viewModel.tryToSaveItem(fields = uiFieldToSave) }),
                    ),
                )
            }
        }
    }
}

@Composable
fun ItemFormScreen(
    saveState: OSActionState,
    navigateBack: () -> Unit,
    nameField: UiField,
    uiField: List<UiField>,
    screenTitle: LbcTextSpec,
    currentImage: OSImageSpec?,
    placeHolder: LbcTextSpec?,
    itemIconLoading: Float?,
    openColorPickerBottomSheet: () -> Unit,
    openItemImagePickerBottomSheet: () -> Unit,
    isTouchExplorationEnabled: Boolean = false,
    snackbarHostState: SnackbarHostState,
    toggleIdentifier: (uiField: UiField) -> Unit,
    renameField: (id: UUID, currentFieldName: String) -> Unit,
    removeField: (uiField: UiField) -> Unit,
    onReorganizeFieldClick: (ReorderOption) -> Unit,
    useThumbnailAsIcon: (OSImageSpec) -> Unit,
    itemFormActionsHolder: ItemFormActionsHolder,
) {
    val scrollState: ScrollState = rememberScrollState()
    OSScreen(
        testTag = UiConstants.TestTag.Screen.ItemFormScreen,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
            .imePadding(),
    ) {
        Column {
            ItemFormTopAppBar(
                title = screenTitle,
                saveButtonState = saveState,
                navigateBack = navigateBack,
                validateForm = itemFormActionsHolder.saveForm.onClick,
                enabledSaveContentDescription = LbcTextSpec.StringResource(OSString.safeItemDetail_newItem_accessibility_enabled_save),
                disabledSaveContentDescription = LbcTextSpec.StringResource(OSString.safeItemDetail_newItem_accessibility_disabled_save),
                saveClickLabel = LbcTextSpec.StringResource(id = OSString.common_accessibility_save),
                addClickLabel = LbcTextSpec.StringResource(OSString.safeItemDetail_addField_buttonTitle),
                addField = itemFormActionsHolder.addNewField.onClick,
            )
            Column(
                modifier = Modifier.verticalScroll(scrollState),
            ) {
                if (!isTouchExplorationEnabled) {
                    ItemFormHeader(
                        currentImage = currentImage,
                        placeHolder = placeHolder,
                        loadingProgress = itemIconLoading,
                        openColorPickerBottomSheet = openColorPickerBottomSheet,
                        openItemImagePickerBottomSheet = openItemImagePickerBottomSheet,
                    )
                }
                FieldsLayout(
                    nameField = nameField,
                    uiFields = uiField,
                    toggleIdentifier = toggleIdentifier,
                    useThumbnailAsIcon = useThumbnailAsIcon,
                    renameField = renameField,
                    removeField = removeField,
                    reorderField = onReorganizeFieldClick,
                    itemFormActionsHolder = itemFormActionsHolder,
                )
                ItemFormActionList(
                    itemFormActionList = listOfNotNull(
                        itemFormActionsHolder.addNewField,
                        itemFormActionsHolder.addNewFile,
                        itemFormActionsHolder.saveForm.takeIf { isTouchExplorationEnabled },
                    ),
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .zIndex(UiConstants.SnackBar.ZIndex)
                .align(Alignment.BottomCenter),
        )
    }
}

private fun removeField(
    viewModel: ItemFormViewModel,
    it: UiField,
    isTouchExplorationEnabled: Boolean,
    context: Context,
) {
    viewModel.removeField(it)
    if (isTouchExplorationEnabled) {
        Toast.makeText(
            context,
            context.getString(
                OSString.itemForm_removeField_accessibility_feedback,
                it.fieldDescription.value.string(context),
            ),
            Toast.LENGTH_SHORT,
        ).show()
    }
}

private fun renameField(
    viewModel: ItemFormViewModel,
    id: UUID,
    name: String,
    isTouchExplorationEnabled: Boolean,
    context: Context,
) {
    viewModel.renameField(id, name) { oldName, newName ->
        if (isTouchExplorationEnabled) {
            Toast.makeText(
                context,
                context.getString(
                    OSString.itemForm_renameField_accessibility_feedback,
                    oldName,
                    newName,
                ),
                Toast.LENGTH_SHORT,
            ).show()
        }
    }
}

private fun toggleIdentifier(
    viewModel: ItemFormViewModel,
    context: Context,
    isTouchExplorationEnabled: Boolean,
    uiField: UiField,
) {
    viewModel.toggleIdentifier(uiField) { feedbackField ->
        if (isTouchExplorationEnabled) {
            val feedback = if (feedbackField.isIdentifier) {
                OSString.itemForm_useFieldAsIdentifier_accessibility_feedback
            } else {
                OSString.itemForm_dontUseFieldAsIdentifier_accessibility_feedback
            }
            Toast.makeText(
                context,
                context.getString(
                    feedback,
                    feedbackField.fieldDescription.value.string(context),
                    feedbackField.getDisplayedValue(),
                ),
                Toast.LENGTH_SHORT,
            ).show()
        }
    }
}

@SuppressLint("UnrememberedMutableState")
@OsDefaultPreview
@Composable
private fun ItemFormScreenPreview() {
    OSPreviewBackgroundTheme {
        ItemFormScreen(
            saveState = OSActionState.Enabled,
            navigateBack = {},
            nameField = NameTextUiField(
                id = UUID.randomUUID(),
                fieldDescription = mutableStateOf(LbcTextSpec.Raw(loremIpsum(1))),
                placeholder = LbcTextSpec.Raw(loremIpsum(1)),
                safeItemFieldKind = SafeItemFieldKind.Text,
                onValueChange = {},
            ),
            uiField = emptyList(),
            screenTitle = loremIpsumSpec(1),
            currentImage = null,
            placeHolder = null,
            itemIconLoading = null,
            openColorPickerBottomSheet = {},
            openItemImagePickerBottomSheet = {},
            isTouchExplorationEnabled = true,
            snackbarHostState = SnackbarHostState(),
            toggleIdentifier = {},
            renameField = { _, _ -> },
            removeField = {},
            onReorganizeFieldClick = {},
            useThumbnailAsIcon = {},
            itemFormActionsHolder = ItemFormActionsHolder(
                ItemFormAction.AddNewField {},
                ItemFormAction.AddNewFile {},
                ItemFormAction.SaveForm {},
            ),
        )
    }
}
