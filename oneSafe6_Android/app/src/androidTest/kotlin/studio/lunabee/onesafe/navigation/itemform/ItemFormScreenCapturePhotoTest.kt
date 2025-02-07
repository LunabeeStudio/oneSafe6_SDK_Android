package studio.lunabee.onesafe.navigation.itemform

import android.Manifest
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasAnySibling
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import studio.lunabee.compose.androidtest.extension.waitUntilDoesNotExist
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.onesafe.BuildConfig
import studio.lunabee.onesafe.OSMainActivityTest
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.domain.model.camera.CameraSystem
import studio.lunabee.onesafe.feature.itemform.ItemEditionDataManagerColorPickerTest
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSTestConfig
import studio.lunabee.onesafe.test.testUUIDs
import studio.lunabee.onesafe.ui.UiConstants
import java.io.File
import java.util.regex.Pattern
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
@HiltAndroidTest
class ItemFormScreenCapturePhotoTest : OSMainActivityTest() {
    @get:Rule(order = 0)
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.Home { }

    private val device: UiDevice by lazy { UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()) }

    companion object {
        @JvmStatic
        @BeforeClass
        fun cameraPermission() {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(
                context.packageName,
                Manifest.permission.CAMERA,
            )
        }
    }

    /**
     * Create an item using the camera
     * Check snackbar for adding new photo is triggered
     * Re-navigate to the camera from the snackbar
     */
    // FIXME <Flaky>
    @Test
    fun navigate_to_form_from_camera_and_capture_another_test() {
        return
        invoke {
            itemCreationBottomSheet()
            hasText(getString(OSString.safeItemDetail_addImage_menu_camera))
                .waitUntilExactlyOneExists(useUnmergedTree = true)
                .assertIsDisplayed()
                .performClick()
            doCapture()
            captureAnother()
        }
    }

    /**
     * Create an item using the camera
     * Check snackbar for adding new photo is triggered is not displayed
     * Add a new field from camera
     * Re-navigate to the camera from the snackbar
     */
    // FIXME <Flaky>
    @Test
    fun add_another_field_from_camera_test() {
        return
        invoke {
            itemCreationBottomSheet()
            hasText(getString(OSString.createItem_template_folderElement))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
                .performClick()
            hasText(getString(OSString.safeItemDetail_snackbar_anotherPhoto))
                .waitUntilDoesNotExist()
            captureForNewField()
            captureAnother()
        }
    }

    /**
     * • Pick a color
     * • Capture an image
     * • Check override color dialog is displayed
     * • Confirm
     * • Capture another image
     * • Check override color dialog is NOT displayed
     * • Pick a color
     * • Capture another image
     * • Check override color dialog is displayed
     */
    // FIXME <Flaky>
    @Test
    fun item_creation_change_image_after_color_picking_test() {
        return
        invoke {
            itemCreationBottomSheet()
            hasText(getString(OSString.createItem_template_folderElement))
                .waitUntilExactlyOneExists()
                .performClick()
            pickColor()
            captureNewItemIconFromForm()
            hasText(getString(OSString.common_yes)).and(hasAnyAncestor(isDialog()))
                .waitUntilExactlyOneExists()
                .performClick()
            captureNewItemIconFromForm()
            isDialog()
                .waitUntilDoesNotExist()
            pickColor()
            captureNewItemIconFromForm()
            hasText(getString(OSString.common_yes)).and(hasAnyAncestor(isDialog()))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
        }
    }

    /**
     * • Set item icon using the camera
     * • Add a fields using the camera
     * • (Re)Set item icon using the camera
     * • Add a fields using the camera
     * • Save (only check no crash)
     */
    // FIXME <Flaky>
    @Test
    fun capture_many_images_test() {
        return
        invoke {
            itemCreationBottomSheet()
            hasText(getString(OSString.createItem_template_folderElement))
                .waitUntilExactlyOneExists()
                .performClick()
            captureNewItemIconFromForm()
            captureForNewField()
            captureNewItemIconFromForm()
            captureForNewField()
            hasTestTag(UiConstants.TestTag.Item.SaveAction)
                .waitUntilExactlyOneExists()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.itemDetailsScreen(testUUIDs[0]))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
        }
    }

    context(ComposeUiTest)
    private fun captureForNewField() {
        hasText(getString(OSString.safeItemDetail_addFile_buttonTitle))
            .waitUntilExactlyOneExists()
            .assertIsDisplayed()
            .performClick()
        hasText(getString(OSString.safeItemDetail_addImage_menu_camera))
            .waitUntilExactlyOneExists(useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()
        doCapture()
    }

    context(ComposeUiTest)
    private fun ItemFormScreenCapturePhotoTest.captureNewItemIconFromForm() {
        hasTestTag(UiConstants.TestTag.Item.PictureButton)
            .waitUntilExactlyOneExists()
            .performClick()
        hasText(getString(OSString.itemCreation_picture_choice_fromCamera))
            .waitUntilExactlyOneExists()
            .performClick()
        doCapture()
    }

    context(ComposeUiTest)
    private fun doCapture() {
        when (OSTestConfig.cameraSystem) {
            CameraSystem.InApp -> captureWithInAppCamera()
            CameraSystem.External -> captureWithSystemCamera()
        }
    }

    context(ComposeUiTest)
    private fun pickColor() {
        ItemEditionDataManagerColorPickerTest.openColorPicker()
        ItemEditionDataManagerColorPickerTest.pickColor()
        ItemEditionDataManagerColorPickerTest.validatePicker()
    }

    context(ComposeUiTest)
    private fun captureAnother() {
        hasText(getString(OSString.common_yes)).and(hasAnySibling(hasText(getString(OSString.safeItemDetail_snackbar_anotherPhoto))))
            .waitUntilExactlyOneExists()
            .performClick()

        when (OSTestConfig.cameraSystem) {
            CameraSystem.InApp -> {
                hasTestTag(UiConstants.TestTag.Screen.CameraActivityScreen)
                    .waitUntilExactlyOneExists()
                    .assertIsDisplayed()
            }
            CameraSystem.External -> {
                waitUntil("oneSafe goes background (behind camera app)", 10_000) {
                    device.wait(Until.gone(By.pkg("studio.lunabee.onesafe.dev")), 0)
                }
            }
        }
    }

    context(ComposeUiTest)
    private fun itemCreationBottomSheet() {
        hasTestTag(UiConstants.TestTag.BreadCrumb.OSCreateItemButton)
            .waitUntilExactlyOneExists()
            .performClick()
        hasTestTag(UiConstants.TestTag.BottomSheet.CreateItemBottomSheet)
            .waitUntilExactlyOneExists()
            .performTouchInput { swipeUp() }
            .assertIsDisplayed()
    }

    context(ComposeUiTest)
    private fun captureWithInAppCamera() {
        hasTestTag(UiConstants.TestTag.Item.ShutterButton)
            .waitUntilExactlyOneExists()
            .performClick()
        hasTestTag(UiConstants.TestTag.Item.CameraPreviewConfirmButton)
            .waitUntilExactlyOneExists()
            .performClick()
    }

    private fun captureWithSystemCamera() {
        try {
            val shutterSelector = By.res(Pattern.compile(".*:id/shutter_button"))
            val doneSelector = By.res(Pattern.compile(".*:id/(done_button)"))
            device.wait(Until.findObjects(shutterSelector), 10_000)!![0].click()
            // fallback to shutter, depending on device it can be the same id
            device.wait(Until.findObjects(doneSelector), 5_000)?.get(0)?.click()
                ?: device.wait(Until.findObjects(shutterSelector), 5_000)!![0].click()
            assertTrue(device.wait(Until.hasObject(By.pkg(BuildConfig.APPLICATION_ID)), 10_000))
        } catch (e: Throwable) {
            device.takeScreenshot(File("${printRule.basePath}_camera_window_dump.png"))
            device.dumpWindowHierarchy(File("${printRule.basePath}_camera_window_dump.xml"))
            throw e
        }
    }
}
