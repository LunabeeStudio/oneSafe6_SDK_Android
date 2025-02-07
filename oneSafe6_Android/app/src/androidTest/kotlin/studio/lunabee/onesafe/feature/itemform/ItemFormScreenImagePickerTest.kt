package studio.lunabee.onesafe.feature.itemform

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import org.junit.Test
import studio.lunabee.compose.androidtest.LbcComposeTest
import studio.lunabee.compose.androidtest.extension.waitAndPrintWholeScreenToCacheDir
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.domain.model.camera.CameraSystem
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.extension.iconSample
import studio.lunabee.onesafe.feature.camera.model.CameraData
import studio.lunabee.onesafe.feature.camera.model.OSMediaType
import studio.lunabee.onesafe.feature.itemform.bottomsheet.image.ItemIconPickerBottomSheet
import studio.lunabee.onesafe.feature.itemform.manager.ExternalPhotoCapture
import studio.lunabee.onesafe.feature.itemform.manager.InAppMediaCapture
import studio.lunabee.onesafe.feature.itemform.model.ItemFormAction
import studio.lunabee.onesafe.feature.itemform.model.ItemFormActionsHolder
import studio.lunabee.onesafe.feature.itemform.model.uifield.text.impl.NormalTextUiField
import studio.lunabee.onesafe.feature.itemform.screen.ItemFormScreen
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.test.OSTestConfig
import studio.lunabee.onesafe.ui.UiConstants
import java.io.File
import java.util.UUID

@OptIn(ExperimentalTestApi::class)
class ItemFormScreenImagePickerTest : LbcComposeTest() {

    @Test
    fun display_image_picker_from_picture_button_without_picture_bottom_sheet() {
        setContent {
            onNodeWithTag(testTag = UiConstants.TestTag.Item.PictureButton).performClick()
            assertBottomSheetIsDisplayedWithCorrectEntries(withDeleteEntry = false)
        }
    }

    @Test
    fun display_image_picker_from_add_picture_button_without_picture_bottom_sheet() {
        setContent {
            onNodeWithTag(testTag = UiConstants.TestTag.Item.AddPictureButton).performClick()
            assertBottomSheetIsDisplayedWithCorrectEntries(withDeleteEntry = false)
        }
    }

    @Test
    fun display_image_picker_from_picture_button_with_picture_bottom_sheet() {
        setContent(currentImage = iconSample) {
            onNodeWithTag(testTag = UiConstants.TestTag.Item.PictureButton).performClick()
            assertBottomSheetIsDisplayedWithCorrectEntries(withDeleteEntry = true)
        }
    }

    @Test
    fun display_image_picker_from_add_picture_button_with_picture_bottom_sheet() {
        setContent(currentImage = iconSample) {
            onNodeWithTag(testTag = UiConstants.TestTag.Item.AddPictureButton).performClick()
            assertBottomSheetIsDisplayedWithCorrectEntries(withDeleteEntry = true)
        }
    }

    @Test
    fun delete_selected_image() {
        setContent(currentImage = iconSample) {
            onNodeWithTag(testTag = iconSample.decodeToString()).assertIsDisplayed()
            onNodeWithTag(testTag = UiConstants.TestTag.Item.AddPictureButton).performClick()
            onNodeWithText(text = getString(OSString.itemCreation_picture_choice_delete)).performClick()
            onNodeWithTag(testTag = DefaultTag).assertIsDisplayed()
        }
    }

    private fun ComposeUiTest.assertBottomSheetIsDisplayedWithCorrectEntries(withDeleteEntry: Boolean) {
        onNodeWithTag(testTag = UiConstants.TestTag.BottomSheet.ItemImagePickerBottomSheet).performTouchInput { swipeUp() }
        onNodeWithTag(testTag = UiConstants.TestTag.BottomSheet.ItemImagePickerBottomSheet).assertIsDisplayed()
        onNodeWithText(text = getString(OSString.itemCreation_picture_choice_fromCamera)).assertIsDisplayed()
        onNodeWithText(text = getString(OSString.itemCreation_picture_choice_fromGallery)).assertIsDisplayed()
        if (withDeleteEntry) {
            onNodeWithText(text = getString(OSString.itemCreation_picture_choice_delete)).assertIsDisplayed()
        } else {
            onNodeWithText(text = getString(OSString.itemCreation_picture_choice_delete)).assertDoesNotExist()
        }
        hasTestTag(testTag = UiConstants.TestTag.Screen.ItemFormScreen).waitAndPrintWholeScreenToCacheDir(printRule)
    }

    private fun setContent(
        currentImage: ByteArray? = null,
        block: ComposeUiTest.() -> Unit,
    ) {
        invoke {
            setContent {
                var isImagePickerVisible by rememberSaveable { mutableStateOf(value = false) }
                var osImageSpec: OSImageSpec? by remember {
                    mutableStateOf(value = currentImage?.let { OSImageSpec.Data(data = currentImage) })
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag(
                            tag = osImageSpec
                                ?.getAs<ByteArray>()
                                ?.decodeToString() ?: DefaultTag,
                        ),
                ) {
                    ItemIconPickerBottomSheet(
                        isVisible = isImagePickerVisible,
                        onBottomSheetClosed = { isImagePickerVisible = false },
                        hasImageToDisplay = osImageSpec != null,
                        removeImageSelected = { osImageSpec = null },
                        onImageCaptureFromCamera = {},
                        onIconPickedByUser = { },
                        cameraData = when (OSTestConfig.cameraSystem) {
                            CameraSystem.InApp -> CameraData.InApp(
                                InAppMediaCapture(null, null, OSMediaType.PHOTO),
                            )
                            CameraSystem.External -> CameraData.External(lazy { ExternalPhotoCapture(File(""), Uri.EMPTY) })
                        },
                        canFetchFromUrl = false,
                        onFetchFromUrl = {},
                        onEnterUrlForIcon = {},
                    )

                    ItemFormScreen(
                        saveState = OSActionState.Disabled,
                        navigateBack = {},
                        nameField = NormalTextUiField(
                            id = UUID.randomUUID(),
                            safeItemFieldKind = SafeItemFieldKind.Text,
                            fieldDescription = remember { mutableStateOf(LbcTextSpec.Raw("item name")) },
                            placeholder = LbcTextSpec.Raw("item name"),
                        ),
                        uiField = listOf(),
                        screenTitle = LbcTextSpec.Raw("Edit"),
                        currentImage = osImageSpec,
                        placeHolder = null,
                        itemIconLoading = null,
                        openColorPickerBottomSheet = {},
                        openItemImagePickerBottomSheet = { isImagePickerVisible = true },
                        snackbarHostState = remember { SnackbarHostState() },
                        toggleIdentifier = {},
                        renameField = { _, _ -> },
                        removeField = { _ -> },
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
            block()
        }
    }

    companion object {
        private const val DefaultTag: String = "DefaultTag"
    }
}
