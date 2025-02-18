package studio.lunabee.onesafe.feature.itemform.uifield

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Test
import studio.lunabee.compose.androidtest.LbcComposeTest
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.domain.model.safeitem.FieldMask
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.extension.iconSample
import studio.lunabee.onesafe.feature.itemform.model.uifield.file.FromUriFileUiField
import studio.lunabee.onesafe.feature.itemform.model.uifield.file.ThumbnailState
import studio.lunabee.onesafe.feature.itemform.model.uifield.text.impl.NameTextUiField
import studio.lunabee.onesafe.feature.itemform.model.uifield.text.impl.NormalTextUiField
import studio.lunabee.onesafe.feature.itemform.model.uifield.text.impl.PasswordTextUiField
import studio.lunabee.onesafe.feature.itemform.model.uifield.text.impl.YearMonthDateTextUiField
import studio.lunabee.onesafe.ui.UiConstants
import java.io.FileNotFoundException
import java.util.UUID
import kotlin.test.assertFalse

@OptIn(ExperimentalTestApi::class)
class TextUiFieldTest : LbcComposeTest() {

    private val fieldDescription: String = "test field"

    /**
     * Simple test to check if onValueChanged is well triggered and recomposed on user input
     */
    @Test
    fun text_ui_field_test() {
        val uiField = NormalTextUiField(
            id = UUID.randomUUID(),
            fieldDescription = mutableStateOf(LbcTextSpec.Raw(fieldDescription)),
            placeholder = LbcTextSpec.Raw(fieldDescription),
            safeItemFieldKind = SafeItemFieldKind.Text,
        )

        invoke {
            setContent {
                uiField.MainComposable(
                    modifier = Modifier,
                    hasNext = false,
                    toggleIdentifier = {},
                    renameField = {},
                    removeField = {},
                    useThumbnailAsIcon = {},
                )
            }

            // Test if input change the value
            onNodeWithTag(UiConstants.TestTag.Item.ItemFormField).performTextInput("text")
            onNodeWithText("text").assertIsDisplayed()
        }
    }

    /**
     * Check if the error label is correctly displayed on uiField
     */
    @Test
    fun text_ui_field_error_test() {
        val uiField = NameTextUiField(
            id = UUID.randomUUID(),
            fieldDescription = mutableStateOf(LbcTextSpec.Raw(fieldDescription)),
            placeholder = LbcTextSpec.Raw(fieldDescription),
            safeItemFieldKind = SafeItemFieldKind.Text,
            onValueChange = {},
        )
        lateinit var errorString: String
        invoke {
            setContent {
                errorString = LbcTextSpec.StringResource(OSString.safeItemDetail_title_empty_errorLabel).string
                uiField.MainComposable(
                    modifier = Modifier,
                    hasNext = false,
                    toggleIdentifier = {},
                    renameField = {},
                    removeField = {},
                    useThumbnailAsIcon = {},
                )
            }
            onNodeWithText(errorString).assertDoesNotExist()
            uiField.isErrorDisplayed = true
            hasText(errorString).waitUntilExactlyOneExists().assertIsDisplayed()
        }
    }

    /**
     * Test if password has the passwordGeneration action
     * Test the visibility on password uiField
     * - On focus -> The value should be visible
     * - Clear focus -> The value go back to invisible
     */
    @Test
    fun password_ui_field_visibility_on_focus_test() {
        val uiField = PasswordTextUiField(
            id = UUID.randomUUID(),
            safeItemFieldKind = SafeItemFieldKind.Password,
            fieldDescription = mutableStateOf(LbcTextSpec.Raw(fieldDescription)),
            placeholder = LbcTextSpec.Raw(fieldDescription),
        )
        lateinit var focusManager: FocusManager
        invoke {
            setContent {
                focusManager = LocalFocusManager.current
                uiField.MainComposable(
                    modifier = Modifier,
                    hasNext = false,
                    toggleIdentifier = {},
                    renameField = {},
                    removeField = {},
                    useThumbnailAsIcon = {},
                )
            }
            uiField.initValues(
                isIdentifier = false,
                initialValue = "text",
            )
            onNodeWithTag(UiConstants.TestTag.Item.GeneratePasswordAction).assertIsDisplayed()
            onNodeWithTag(UiConstants.TestTag.Item.VisibilityAction).assertIsDisplayed()
            hasText(PasswordVisualTransformation().filter(AnnotatedString("text")).text.text)
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
            onNodeWithTag(UiConstants.TestTag.Item.ItemFormField).performClick()
            hasText("text").waitUntilExactlyOneExists()
                .assertIsDisplayed()
            runOnUiThread {
                focusManager.clearFocus(true)
            }
            hasText(PasswordVisualTransformation().filter(AnnotatedString("text")).text.text)
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
        }
    }

    /**
     * YearMonth Text field
     * Test if max char number works
     * Test if mask is well applied
     * Test error if date parsing fails if bad month
     */
    @Test
    fun yearMonth_ui_field_test() {
        val uiField = YearMonthDateTextUiField(
            id = UUID.randomUUID(),
            fieldDescription = mutableStateOf(LbcTextSpec.Raw(fieldDescription)),
            placeholder = LbcTextSpec.Raw(fieldDescription),
            safeItemFieldKind = SafeItemFieldKind.YearMonth,
        )
        invoke {
            setContent {
                uiField.MainComposable(
                    modifier = Modifier,
                    hasNext = false,
                    toggleIdentifier = {},
                    renameField = {},
                    removeField = {},
                    useThumbnailAsIcon = {},
                )
            }
            uiField.initValues(
                isIdentifier = false,
                initialValue = null,
            )
            onNodeWithTag(UiConstants.TestTag.Item.ItemFormField).performTextInput("123456")
            onNodeWithText("123456").assertIsDisplayed()
            waitForIdle()
            assert(uiField.isInError())

            onNodeWithTag(UiConstants.TestTag.Item.ItemFormField).performTextClearance()

            onNodeWithTag(UiConstants.TestTag.Item.ItemFormField).performTextInput("1234")
            hasText(
                FieldMask.getApplyMaskOnString(
                    "1234".toCharArray(),
                    FieldMask.MonthYearDateMasks.first().formattingMask.orEmpty(),
                ),
            ).waitUntilExactlyOneExists().assertIsDisplayed()
            assertFalse(uiField.isInError())

            onNodeWithTag(UiConstants.TestTag.Item.ItemFormField).performTextClearance()
            onNodeWithTag(UiConstants.TestTag.Item.ItemFormField).performTextInput("1634")
            waitForIdle()
            assert(uiField.isInError())
        }
    }

    /**
     * Inject non digit text into the PasswordTextUiField.pin field (copy/paste use case)
     * Non reg https://www.notion.so/lunabeestudio/Crash-past-in-pin-field-bd128524e1c44292a8e4478356ceabc8?pvs=4
     */
    @Test
    fun number_kind_with_non_digit_text_test() {
        val uiField = PasswordTextUiField.pin(id = UUID.randomUUID(), LbcTextSpec.Raw(""), LbcTextSpec.Raw(""))
        invoke {
            setContent {
                uiField.MainComposable(
                    modifier = Modifier,
                    hasNext = false,
                    toggleIdentifier = {},
                    renameField = {},
                    removeField = {},
                    useThumbnailAsIcon = {},
                )
            }
            onNodeWithTag(UiConstants.TestTag.Item.ItemFormField)
                .performClick() // focus
                .performTextInput("a")

            // Let the cursor appears (and so trigger NumberVisualTransformation stuff)
            mainClock.advanceTimeByFrame()
        }
    }

    @Test
    fun file_kind_error_test() {
        val uiField = FromUriFileUiField(
            id = UUID.randomUUID(),
            safeItemFieldKind = SafeItemFieldKind.Photo,
            fileName = "Test.jpeg",
            fileId = UUID.randomUUID(),
            fileExtension = "jpeg",
            thumbnailFlow = MutableStateFlow(ThumbnailState.Finished(OSImageSpec.Data(iconSample))),
            getInputStream = { throw FileNotFoundException() },
        )
        uiField.displayErrorOnFieldIfNeeded()
        invoke {
            setContent {
                uiField.MainComposable(
                    modifier = Modifier,
                    hasNext = false,
                    toggleIdentifier = { },
                    renameField = { },
                    removeField = {},
                    useThumbnailAsIcon = { },
                )
            }
            hasText(getString(OSString.export_backup_error_fileNotFound_title))
                .waitUntilExactlyOneExists()
        }
    }
}
