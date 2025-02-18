package studio.lunabee.onesafe.navigation.itemform

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.core.graphics.applyCanvas
import com.lunabee.lbcore.model.LBFlowResult
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import org.junit.Rule
import org.junit.Test
import studio.lunabee.compose.androidtest.extension.waitAndPrintWholeScreenToCacheDir
import studio.lunabee.compose.androidtest.extension.waitUntilDoesNotExist
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.di.RemoteModuleBinds
import studio.lunabee.onesafe.OSMainActivityTest
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.error.OSRemoteError
import studio.lunabee.onesafe.hasDrawable
import studio.lunabee.onesafe.repository.datasource.UrlMetadataRemoteDataSource
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.ui.UiConstants
import java.io.File
import javax.inject.Inject

@HiltAndroidTest
@OptIn(ExperimentalTestApi::class)
@UninstallModules(RemoteModuleBinds::class)
class ItemFormUrlMetaDataNavigationTest : OSMainActivityTest() {

    @get:Rule(order = 0)
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.Home()

    private val lunabeeUrl = "lunabee.studio"

    private val youtubeUrl = "youtube.com"

    private val brokenUrl = "lunabee.stu"

    private val youtubeTitle = "YouTube"

    @BindValue
    val mockkUrlMetadataRemoteDataSourceImpl: UrlMetadataRemoteDataSource = mockk {
        coEvery { getPageHtmlCode(any()) }.throws(OSRemoteError(OSRemoteError.Code.UNKNOWN_HTTP_ERROR))
        coEvery { getPageHtmlCode(youtubeUrl) } returns "<title>$youtubeTitle</title>"
        coEvery { getPageHtmlCode(lunabeeUrl) } returns "<title>Lunabee Studio</title>"

        every { downloadFavIcon(any(), any()) } answers {
            flow {
                val url: String = firstArg()
                val file: File = secondArg()
                fillFile(url, file)
                emit(LBFlowResult.Success(file))
            }
        }
        every { downloadImage(lunabeeUrl, any()) } answers {
            flow {
                val url: String = firstArg()
                val file: File = secondArg()
                fillFile(url, file)
                emit(LBFlowResult.Success(secondArg() as File))
            }
        }
        every { downloadImage(brokenUrl, any()) } answers {
            flow {
                val file: File = secondArg()
                emit(LBFlowResult.Failure(OSRemoteError(OSRemoteError.Code.UNKNOWN_HTTP_ERROR), file))
            }
        }
    }

    private fun fillFile(url: String, file: File) {
        val color = if (url == lunabeeUrl) Color.GREEN else Color.RED
        val bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888).applyCanvas {
            drawColor(color)
        }
        file.outputStream().use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        }
    }

    @Inject
    @ApplicationContext
    lateinit var context: Context

    private fun ComposeUiTest.navigateToItemForm() {
        hasTestTag(UiConstants.TestTag.BreadCrumb.OSCreateItemButton)
            .waitAndPrintWholeScreenToCacheDir(printRule)
            .performClick()
            .performTouchInput { swipeUp() }
        hasText(getString(OSString.createItem_template_websiteElement))
            .waitAndPrintWholeScreenToCacheDir(printRule)
            .assertIsDisplayed()
            .performClick()
    }

    // Check that the data on title is well replaced and the icon is fetched
    @Test
    fun fetch_all_data_test() {
        invoke {
            navigateToItemForm()
            hasText(getString(OSString.fieldName_url))
                .waitUntilExactlyOneExists()
                .performTextInput(text = youtubeUrl)
            hasText("$youtubeTitle")
                .waitUntilExactlyOneExists()
                .assertExists()
            hasDrawable(OSDrawable.ic_image)
                .waitUntilDoesNotExist()
        }
    }

    /**
     * check no snackbar displayed on url auto fetch with error
     * check snackbar displayed on manually trigger fetch from url with error
     */
    @Test
    fun enter_wrong_url_error_test() {
        invoke {
            navigateToItemForm()
            hasText(getString(OSString.fieldName_url))
                .waitUntilExactlyOneExists()
                .performTextInput(text = brokenUrl)
            hasText(getString(OSString.safeItemDetail_urlFetching_errorDescription))
                .waitUntilDoesNotExist()
            hasTestTag(UiConstants.TestTag.Item.PictureButton)
                .waitUntilExactlyOneExists()
                .performClick()
            hasText(getString(OSString.itemCreation_picture_fromUrl))
                .waitUntilExactlyOneExists()
                .performClick()
            hasText(getString(OSString.safeItemDetail_urlFetching_errorDescription))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
        }
    }

    // assert that if not url is set, we don't display the fetch button
    @Test
    fun force_fetch_button_not_displayed_test() {
        invoke {
            navigateToItemForm()
            hasTestTag(UiConstants.TestTag.Item.PictureButton)
                .waitUntilExactlyOneExists()
                .performClick()
            hasText(getString(OSString.itemCreation_picture_fromUrl))
                .waitUntilDoesNotExist()
        }
    }

    // check that we don't change the title if it's already set while the icon is changed
    @Test
    fun don_t_replace_title_if_already_set_test() {
        invoke {
            navigateToItemForm()
            hasText(getString(OSString.fieldName_elementName))
                .waitUntilExactlyOneExists()
                .performTextInput(text = "Lunabee")
            hasText(getString(OSString.fieldName_url))
                .waitUntilExactlyOneExists()
                .performTextInput(text = youtubeUrl)
            hasDrawable(OSDrawable.ic_image)
                .waitUntilDoesNotExist()
            hasText("Lunabee")
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
        }
    }

    // Check that force fetching change the icon even it one is already set
    @Test
    fun force_fetch_on_image_picker_test() {
        invoke {
            navigateToItemForm()
            hasText(getString(OSString.fieldName_url))
                .waitUntilExactlyOneExists()
                .performTextInput(text = youtubeUrl)
            hasDrawable(OSDrawable.ic_image)
                .waitUntilDoesNotExist()
            hasTestTag(testTag = UiConstants.TestTag.Item.SaveAction)
                .waitUntilExactlyOneExists()
                .performClick()
            hasContentDescription(getString(OSString.safeItemDetail_accessibility_edit))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
                .performClick()
            hasText(getString(OSString.fieldName_url))
                .waitUntilExactlyOneExists()
                .performTextClearance()
            hasText(getString(OSString.fieldName_url))
                .waitUntilExactlyOneExists()
                .performTextInput(text = lunabeeUrl)
            hasTestTag(UiConstants.TestTag.Item.PictureButton)
                .waitUntilExactlyOneExists()
                .performClick()
            hasText(getString(OSString.itemCreation_picture_fromUrl))
                .waitUntilExactlyOneExists()
                .performClick()
            // Dialog for color change -> Assert that icon is changed
            isDialog()
                .waitUntilExactlyOneExists()
        }
    }

    // Check the icon selection option via url is working when failing
    @Test
    fun enter_url_for_icon_failing_test() {
        invoke {
            navigateToItemForm()
            hasTestTag(UiConstants.TestTag.Item.PictureButton)
                .waitUntilExactlyOneExists()
                .performClick()
            hasText(getString(OSString.safeItemDetail_icon_import_fromUrl))
                .waitUntilExactlyOneExists()
                .performClick()
            hasAnyAncestor(isDialog()).and(hasText(getString(OSString.safeItemDetail_icon_import_fromUrl_dialog_label)))
                .waitUntilExactlyOneExists()
                .performTextInput(brokenUrl)
            hasText(getString(OSString.common_confirm))
                .waitUntilExactlyOneExists()
                .performClick()
            hasText(getString(OSString.safeItemDetail_urlFetching_errorDescription))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
        }
    }
}
