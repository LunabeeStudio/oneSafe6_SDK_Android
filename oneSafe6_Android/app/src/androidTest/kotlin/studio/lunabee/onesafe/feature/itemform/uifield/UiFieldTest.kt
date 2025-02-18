package studio.lunabee.onesafe.feature.itemform.uifield

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import io.mockk.spyk
import io.mockk.verify
import org.junit.Test
import studio.lunabee.compose.androidtest.LbcComposeTest
import studio.lunabee.compose.androidtest.extension.waitUntilDoesNotExist
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.feature.itemform.model.uifield.text.impl.NormalTextUiField
import studio.lunabee.onesafe.ui.UiConstants
import java.util.UUID

@OptIn(ExperimentalTestApi::class)
class UiFieldTest : LbcComposeTest() {

    private val fieldDescription: String = "test field"

    @Test
    fun ui_field_label_test_test() {
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
            onNodeWithText(fieldDescription).performClick()
        }
    }

    @Test
    fun ui_field_deletion_if_empty_test() {
        lateinit var context: Context
        val uiField = NormalTextUiField(
            id = UUID.randomUUID(),
            fieldDescription = mutableStateOf(LbcTextSpec.Raw(fieldDescription)),
            placeholder = LbcTextSpec.Raw(fieldDescription),
            safeItemFieldKind = SafeItemFieldKind.Text,
        )
        val removeField = spyk({ })
        invoke {
            setContent {
                context = LocalContext.current
                uiField.MainComposable(
                    modifier = Modifier,
                    hasNext = false,
                    toggleIdentifier = {},
                    renameField = {},
                    removeField = removeField,
                    useThumbnailAsIcon = {},
                )
            }
            onNodeWithTag(UiConstants.TestTag.Item.fieldActionButton(fieldDescription)).performClick()
            hasText(context.getString(OSString.common_delete)).waitUntilExactlyOneExists()
                .assertIsDisplayed()
                .performClick()
            hasText(context.getString(OSString.itemForm_deleteField_dialog_message))
                .waitUntilDoesNotExist()
            verify(exactly = 1) { removeField() }
        }
    }

    @Test
    fun ui_field_deletion_if_not_empty_test() {
        lateinit var context: Context
        val uiField = NormalTextUiField(
            id = UUID.randomUUID(),
            fieldDescription = mutableStateOf(LbcTextSpec.Raw(fieldDescription)),
            placeholder = LbcTextSpec.Raw(fieldDescription),
            safeItemFieldKind = SafeItemFieldKind.Text,
        )
        val removeField = spyk({ })
        invoke {
            setContent {
                context = LocalContext.current
                uiField.MainComposable(
                    modifier = Modifier,
                    hasNext = false,
                    toggleIdentifier = {},
                    renameField = {},
                    removeField = removeField,
                    useThumbnailAsIcon = {},
                )
            }

            // Test if input change the value
            onNodeWithTag(UiConstants.TestTag.Item.ItemFormField).performTextInput("text")
            hasText("text").waitUntilExactlyOneExists()
            onNodeWithTag(UiConstants.TestTag.Item.fieldActionButton(fieldDescription)).performClick()
            hasText(context.getString(OSString.common_delete)).waitUntilExactlyOneExists()
                .assertIsDisplayed()
                .performClick()
            hasText(context.getString(OSString.itemForm_deleteField_dialog_message))
                .waitUntilExactlyOneExists().assertIsDisplayed()
            onNodeWithText(context.getString(OSString.itemForm_deleteField_dialog_confirmButton)).performClick()
            verify(exactly = 1) { removeField() }
        }
    }

    @Test
    fun display_identifier_label_test() {
        val uiField = NormalTextUiField(
            id = UUID.randomUUID(),
            fieldDescription = mutableStateOf(LbcTextSpec.Raw(fieldDescription)),
            placeholder = LbcTextSpec.Raw(fieldDescription),
            safeItemFieldKind = SafeItemFieldKind.Text,
        )
        uiField.isIdentifier = true
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

            onNodeWithTag(UiConstants.TestTag.Item.IdentifierLabelText)
                .assertIsDisplayed()
                .performClick()
            onNodeWithTag(UiConstants.TestTag.BottomSheet.IdentifierInfoBottomSheet).assertIsDisplayed()
        }
    }

    @Test
    fun has_unset_identifier_action_test() {
        lateinit var context: Context
        val uiField = NormalTextUiField(
            id = UUID.randomUUID(),
            fieldDescription = mutableStateOf(LbcTextSpec.Raw(fieldDescription)),
            placeholder = LbcTextSpec.Raw(fieldDescription),
            safeItemFieldKind = SafeItemFieldKind.Text,
        )
        uiField.isIdentifier = true
        invoke {
            setContent {
                context = LocalContext.current
                uiField.MainComposable(
                    modifier = Modifier,
                    hasNext = false,
                    toggleIdentifier = {},
                    renameField = {},
                    removeField = {},
                    useThumbnailAsIcon = {},
                )
            }

            onNodeWithTag(UiConstants.TestTag.Item.fieldActionButton(fieldDescription))
                .assertIsDisplayed()
                .performClick()
            onNodeWithTag(UiConstants.TestTag.Menu.FieldActionMenu).assertIsDisplayed()
            onNodeWithText(context.getString(OSString.itemForm_action_dontUseAsIdentifier)).assertIsDisplayed()
            onNodeWithText(context.getString(OSString.common_delete)).assertIsDisplayed()
        }
    }

    @Test
    fun has_identifier_action_test() {
        lateinit var context: Context
        val uiField = NormalTextUiField(
            id = UUID.randomUUID(),
            fieldDescription = mutableStateOf(LbcTextSpec.Raw(fieldDescription)),
            placeholder = LbcTextSpec.Raw(fieldDescription),
            safeItemFieldKind = SafeItemFieldKind.Text,
        )
        uiField.isIdentifier = false
        invoke {
            setContent {
                context = LocalContext.current
                uiField.MainComposable(
                    modifier = Modifier,
                    hasNext = false,
                    toggleIdentifier = {},
                    renameField = {},
                    removeField = {},
                    useThumbnailAsIcon = {},
                )
            }

            onNodeWithTag(UiConstants.TestTag.Item.fieldActionButton(fieldDescription))
                .assertIsDisplayed()
                .performClick()
            onNodeWithTag(UiConstants.TestTag.Menu.FieldActionMenu).assertIsDisplayed()
            onNodeWithText(context.getString(OSString.itemForm_action_useAsIdentifier)).assertIsDisplayed()
            onNodeWithText(context.getString(OSString.common_delete)).assertIsDisplayed()
        }
    }
}
