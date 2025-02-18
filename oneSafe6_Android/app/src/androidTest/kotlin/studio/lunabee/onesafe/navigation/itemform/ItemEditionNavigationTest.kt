package studio.lunabee.onesafe.navigation.itemform

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.compose.androidtest.extension.waitAndPrintWholeScreenToCacheDir
import studio.lunabee.compose.androidtest.extension.waitUntilAtLeastOneExists
import studio.lunabee.compose.androidtest.extension.waitUntilDoesNotExist
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.onesafe.AppAndroidTestUtils.createItemFieldFileData
import studio.lunabee.onesafe.AppAndroidTestUtils.createItemFieldPhotoData
import studio.lunabee.onesafe.OSMainActivityTest
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.domain.usecase.item.AddFieldUseCase
import studio.lunabee.onesafe.domain.usecase.item.CreateItemUseCase
import studio.lunabee.onesafe.domain.usecase.item.MoveToBinItemUseCase
import studio.lunabee.onesafe.extension.iconSample
import studio.lunabee.onesafe.hasExcludeSearch
import studio.lunabee.onesafe.test.CommonTestUtils.createItemFieldData
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.firstSafeId
import studio.lunabee.onesafe.test.testUUIDs
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.UiConstants.TestTag.Item.RenameFieldTextField
import java.util.UUID
import javax.inject.Inject

@HiltAndroidTest
@OptIn(ExperimentalTestApi::class)
class ItemEditionNavigationTest : OSMainActivityTest() {
    private val fieldName: String = "Login"
    lateinit var itemId: UUID

    @get:Rule(order = 0)
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    private val itemName = "itemName"
    private val itemName2 = "itemName2"

    override val initialTestState: InitialTestState = InitialTestState.Home {
        val item = createItemUseCase(itemName, null, false, null, null).data!!
        val safeItem = createItemUseCase(itemName2, null, false, null, null).data!!
        itemId = item.id
        addFieldUseCase(
            item.id,
            createItemFieldData(
                name = fieldName,
                position = 0.0,
                placeholder = fieldName,
                value = "user",
                kind = SafeItemFieldKind.Text,
            ),
        )
        moveToBinItemUseCase(safeItem)
    }

    @Inject
    lateinit var createItemUseCase: CreateItemUseCase

    @Inject
    lateinit var addFieldUseCase: AddFieldUseCase

    @Inject
    lateinit var moveToBinItemUseCase: MoveToBinItemUseCase

    @Test
    fun navigate_to_edit_screen_from_home_test() {
        invoke {
            hasExcludeSearch(hasText(itemName))
                .waitUntilExactlyOneExists()
                .performClick()
            hasContentDescription(getString(OSString.safeItemDetail_accessibility_edit))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
                .performClick()
            hasTestTag(testTag = UiConstants.TestTag.Screen.ItemFormScreen)
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
        }
    }

    @Test
    fun navigate_back_on_save_test() {
        invoke {
            hasExcludeSearch(hasText(itemName))
                .waitUntilExactlyOneExists()
                .performClick()
            hasContentDescription(getString(OSString.safeItemDetail_accessibility_edit))
                .waitUntilExactlyOneExists()
                .performClick()
            val itemField = hasText(itemName)
                .waitUntilExactlyOneExists()
            itemField
                .performClick() // request focus
            itemField
                .performTextInput(text = "_edit")
            hasTestTag(UiConstants.TestTag.Item.SaveAction)
                .waitUntilExactlyOneExists()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.itemDetailsScreen(testUUIDs[0]))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
        }
    }

    @Test
    fun navigate_to_edit_screen_from_deleted_item_impossible_test() {
        invoke {
            hasText(getString(OSString.common_bin))
                .waitUntilExactlyOneExists()
                .performClick()
            hasExcludeSearch(hasText(itemName2))
                .waitUntilExactlyOneExists()
                .performClick()
            hasContentDescription(getString(OSString.safeItemDetail_accessibility_edit))
                .waitUntilDoesNotExist()
        }
    }

    @Test
    fun delete_field_confirmation_dialog_test() {
        invoke {
            hasExcludeSearch(hasText(itemName))
                .waitUntilExactlyOneExists()
                .performClick()
            hasContentDescription(getString(OSString.safeItemDetail_accessibility_edit))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
                .performClick()
            hasTestTag(UiConstants.TestTag.Item.fieldActionButton(fieldName))
                .waitUntilExactlyOneExists()
                .performClick()
            hasText(getString(OSString.common_delete)).waitUntilExactlyOneExists().performClick()
            onNode(isDialog())
                .assertIsDisplayed()
            hasText(getString(OSString.common_cancel))
                .waitUntilExactlyOneExists()
                .performClick()
            hasTestTag(UiConstants.TestTag.Item.ItemFormField)
                .waitUntilAtLeastOneExists()
                .onLast()
                .performTextClearance()
            isRoot()
                .waitAndPrintWholeScreenToCacheDir(printRule)
            hasTestTag(UiConstants.TestTag.Item.fieldActionButton(fieldName))
                .waitUntilExactlyOneExists()
                .performClick()
            hasText(getString(OSString.common_delete)).waitUntilExactlyOneExists().performClick()
            isRoot()
                .waitAndPrintWholeScreenToCacheDir(printRule, "field_deleted")
            hasText(fieldName)
                .waitUntilDoesNotExist()
        }
    }

    /**
     * Go to item detail screen.
     * Edit item.
     * -> No field, no re-order option
     */
    @Test
    fun item_edition_without_reorder_option_test() {
        invoke {
            hasExcludeSearch(hasText(itemName))
                .waitUntilExactlyOneExists()
                .performClick()
            hasContentDescription(getString(OSString.safeItemDetail_accessibility_edit))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
                .performClick()
            hasTestTag(testTag = UiConstants.TestTag.Screen.ItemFormScreen)
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
            hasContentDescription(getString(OSString.safeItemDetail_reorder))
                .waitUntilDoesNotExist()
        }
    }

    @Test
    fun item_edition_to_item_reorder_screen_without_changes_test() {
        invoke {
            hasExcludeSearch(hasText(itemName))
                .waitUntilExactlyOneExists()
                .performClick()
            hasContentDescription(getString(OSString.safeItemDetail_accessibility_edit))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
                .performClick()
            hasTestTag(testTag = UiConstants.TestTag.Screen.ItemFormScreen)
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
            hasText(getString(OSString.safeItemDetail_addField_buttonTitle))
                .waitUntilExactlyOneExists()
                .performClick()
            hasText(getString(OSString.fieldName_email))
                .waitUntilExactlyOneExists()
                .performClick()
            hasText(getString(OSString.safeItemDetail_addField_buttonTitle))
                .waitUntilExactlyOneExists()
                .performClick()
            hasText(getString(OSString.fieldName_password))
                .waitUntilExactlyOneExists()
                .performClick()
            hasContentDescription(getString(OSString.safeItemDetail_reorder))
                .waitUntilExactlyOneExists()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.ItemReOrderScreen)
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
            hasTestTag(UiConstants.TestTag.Item.SaveAction)
                .waitUntilExactlyOneExists()
                .assertIsNotEnabled()
        }
    }

    @Test
    fun rename_field_test() {
        invoke {
            hasExcludeSearch(hasText(itemName))
                .waitUntilExactlyOneExists()
                .performClick()
            hasContentDescription(getString(OSString.safeItemDetail_accessibility_edit))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
                .performClick()
            hasTestTag(UiConstants.TestTag.Item.fieldActionButton(fieldName))
                .waitUntilExactlyOneExists()
                .performClick()
            hasText(getString(OSString.itemForm_action_rename)).waitUntilExactlyOneExists().performClick()
            onNode(isDialog())
                .assertIsDisplayed()

            val newFieldName = "newName"
            onNodeWithTag(RenameFieldTextField)
                .performClick()
                .performTextInput(newFieldName)
            onNodeWithText(getString(OSString.common_confirm)).performClick()
            hasExcludeSearch(hasText(newFieldName))
                .waitUntilExactlyOneExists()
            onNodeWithText(fieldName).assertDoesNotExist()
            hasTestTag(UiConstants.TestTag.Item.SaveAction)
                .waitUntilExactlyOneExists()
                .assertIsEnabled()
        }
    }

    @Test
    fun reorder_fields_kind_test() {
        val informationFields: List<String> = buildList { repeat(3) { index -> add("$fieldName$index") } }
        val mediaFields: List<String> = buildList { repeat(3) { index -> add("photo_$index.jpg") } }
        val fileFields: List<String> = buildList { repeat(3) { index -> add("file_$index.test") } }
        invoke {
            runTest {
                informationFields.forEach { name ->
                    addFieldUseCase(itemId, createItemFieldData(name = name, kind = SafeItemFieldKind.Text))
                }
                mediaFields.forEach { name ->
                    val fileId = UUID.randomUUID()
                    addFieldUseCase(itemId, createItemFieldPhotoData(fileId = fileId.toString(), name = name))
                    fileRepository.addFile(fileId, iconSample, firstSafeId)
                }
                fileFields.forEach { name ->
                    val fileId = UUID.randomUUID()
                    addFieldUseCase(itemId, createItemFieldFileData(fileId = fileId.toString(), name = name))
                    fileRepository.addFile(fileId, iconSample, firstSafeId)
                }
            }
            hasExcludeSearch(hasText(itemName))
                .waitUntilExactlyOneExists()
                .performClick()
            hasContentDescription(getString(OSString.safeItemDetail_accessibility_edit))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
                .performClick()
            testFieldsDisplayed(
                testTag = UiConstants.TestTag.Item.InformationFieldsCard,
                visibleFieldsName = informationFields,
                notVisibleFieldsName = mediaFields + fileFields,
            )
            Espresso.pressBack()
            testFieldsDisplayed(
                testTag = UiConstants.TestTag.Item.MediaFieldsCard,
                visibleFieldsName = mediaFields,
                notVisibleFieldsName = informationFields + fileFields,
            )
            Espresso.pressBack()
            testFieldsDisplayed(
                testTag = UiConstants.TestTag.Item.FileFieldsCard,
                visibleFieldsName = fileFields,
                notVisibleFieldsName = informationFields + mediaFields,
            )
        }
    }

    @Test
    fun remove_field_test() {
        invoke {
            hasExcludeSearch(hasText(itemName))
                .waitUntilExactlyOneExists()
                .performClick()
            hasContentDescription(getString(OSString.safeItemDetail_accessibility_edit))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
                .performClick()
            hasTestTag(UiConstants.TestTag.Item.fieldActionButton(fieldName))
                .waitUntilExactlyOneExists()
                .performClick()
            hasText(getString(OSString.common_delete))
                .waitUntilExactlyOneExists()
                .performClick()
            hasText(getString(OSString.itemForm_deleteField_dialog_confirmButton))
                .waitUntilExactlyOneExists()
                .performClick()
            hasTestTag(UiConstants.TestTag.Item.fieldActionButton(fieldName))
                .waitUntilDoesNotExist()
        }
    }

    private fun ComposeUiTest.testFieldsDisplayed(
        testTag: String,
        visibleFieldsName: List<String>,
        notVisibleFieldsName: List<String>,
    ) {
        hasContentDescription(getString(OSString.safeItemDetail_reorder)).and(hasParent(hasTestTag(testTag)))
            .waitUntilExactlyOneExists()
            .performScrollTo()
            .performClick()
        notVisibleFieldsName.forEach { name ->
            hasText(name).waitUntilDoesNotExist().assertDoesNotExist()
        }
        visibleFieldsName.forEach { name ->
            hasText(name).waitUntilExactlyOneExists(useUnmergedTree = true).assertIsDisplayed()
        }
    }
}
