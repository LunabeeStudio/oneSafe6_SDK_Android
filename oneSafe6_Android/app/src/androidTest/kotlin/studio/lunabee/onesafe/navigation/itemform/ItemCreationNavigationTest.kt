package studio.lunabee.onesafe.navigation.itemform

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.test.espresso.Espresso
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import studio.lunabee.compose.androidtest.extension.printToCacheDir
import studio.lunabee.compose.androidtest.extension.waitAndPrintRootToCacheDir
import studio.lunabee.compose.androidtest.extension.waitAndPrintWholeScreenToCacheDir
import studio.lunabee.compose.androidtest.extension.waitUntilDoesNotExist
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.onesafe.OSMainActivityTest
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.domain.common.FieldIdProvider
import studio.lunabee.onesafe.domain.repository.AppVisitRepository
import studio.lunabee.onesafe.feature.itemform.model.ItemCreationTemplateUiField
import studio.lunabee.onesafe.getLbcTextSpecResString
import studio.lunabee.onesafe.hasExcludeSearch
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.blocking
import studio.lunabee.onesafe.test.firstSafeId
import studio.lunabee.onesafe.test.testUUIDs
import studio.lunabee.onesafe.ui.UiConstants
import java.util.UUID
import javax.inject.Inject

@HiltAndroidTest
@OptIn(ExperimentalTestApi::class)
class ItemCreationNavigationTest : OSMainActivityTest() {
    @get:Rule(order = 0)
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.Home()

    @Inject
    lateinit var fieldIdProvider: FieldIdProvider

    @Inject
    @ApplicationContext
    lateinit var context: Context

    @Inject
    lateinit var appVisitRepository: AppVisitRepository

    private fun ComposeUiTest.navToBottomSheet() {
        // Navigate to BottomSheet
        hasTestTag(UiConstants.TestTag.BreadCrumb.OSCreateItemButton)
            .waitUntilExactlyOneExists()
            .performClick()
            .performTouchInput { swipeUp() }
        hasTestTag(UiConstants.TestTag.BottomSheet.CreateItemBottomSheet)
            .waitAndPrintWholeScreenToCacheDir(printRule, "_${UiConstants.TestTag.BottomSheet.CreateItemBottomSheet}")
    }

    @Test
    fun bottom_sheet_to_item_creation_screen_credit_card() {
        invoke {
            bottomSheetToItemCreationType(
                itemCreationTemplateField = ItemCreationTemplateUiField.CreditCard,
                itemTextRes = OSString.createItem_template_creditCardElement,
            )
        }
    }

    @Test
    fun bottom_sheet_to_item_creation_screen_websiteElement_no_tooltip_test() {
        invoke {
            bottomSheetToItemCreationType(
                itemCreationTemplateField = ItemCreationTemplateUiField.Website,
                itemTextRes = OSString.createItem_template_websiteElement,
            )
        }
    }

    @Test
    fun bottom_sheet_to_item_creation_screen_websiteElement_tooltip_test() {
        invoke {
            bottomSheetToItemCreationType(
                itemCreationTemplateField = ItemCreationTemplateUiField.Website,
                itemTextRes = OSString.createItem_template_websiteElement,
                hasSeenUrlTooltip = false,
            )
        }
    }

    @Test
    fun bottom_sheet_to_item_creation_screen_applicationElement() {
        invoke {
            bottomSheetToItemCreationType(
                itemCreationTemplateField = ItemCreationTemplateUiField.Application,
                itemTextRes = OSString.createItem_template_applicationElement,
            )
        }
    }

    @Test
    fun bottom_sheet_to_item_creation_screen_any_tooltip_emoji_test() {
        invoke {
            bottomSheetToItemCreationType(
                itemCreationTemplateField = ItemCreationTemplateUiField.Application,
                itemTextRes = OSString.createItem_template_applicationElement,
                hasSeenUrlTooltip = true,
                hasSeenEmojiTooltip = false,
            )
        }
    }

    @Test
    fun bottom_sheet_to_item_creation_screen_folderElement() {
        invoke {
            bottomSheetToItemCreationType(
                itemCreationTemplateField = ItemCreationTemplateUiField.Folder,
                itemTextRes = OSString.createItem_template_folderElement,
            )
        }
    }

    @Test
    fun bottom_sheet_to_item_creation_screen_customElement() {
        invoke {
            bottomSheetToItemCreationType(
                itemCreationTemplateField = ItemCreationTemplateUiField.Custom,
                itemTextRes = OSString.createItem_template_customElement,
            )
        }
    }

    @Test
    fun item_creation_to_item_details() {
        invoke {
            navigateToItemCreationCustomElementScreen()
            val itemName = "itemNameTest"
            createItem(itemName = itemName)
            checkThatNavigationToItemDetailsIsDone(itemId = testUUIDs[0], itemName = itemName)
            navigateBack(parentItemId = null, itemName = itemName)
        }
    }

    @Test
    fun item_details_creation_to_item_details() {
        invoke {
            navigateToItemCreationCustomElementScreen()
            val parentItemName = "parentItemNameTest"
            val childItemName = "childItemNameTest"
            createItem(itemName = parentItemName)
            checkThatNavigationToItemDetailsIsDone(itemId = testUUIDs[0], itemName = parentItemName)
            navigateToItemCreationCustomElementScreen()
            createItem(itemName = childItemName)
            checkThatNavigationToItemDetailsIsDone(itemId = testUUIDs[1], itemName = childItemName)
            navigateBack(parentItemId = testUUIDs[0], itemName = childItemName)
            navigateBack(parentItemId = null, itemName = parentItemName)
        }
    }

    @Test
    fun create_item_with_deleted_field_test() {
        val errorString = context.getString(OSString.safeItemDetail_title_empty_errorLabel)
        invoke {
            bottomSheetToItemCreationType(
                itemCreationTemplateField = ItemCreationTemplateUiField.CreditCard,
                itemTextRes = OSString.createItem_template_creditCardElement,
            )
            hasExcludeSearch(hasText(getString(OSString.safeItemDetail_card_number_placeholder)))
                .waitUntilExactlyOneExists()
                .performTextInput(text = "1234")
            onNodeWithTag(
                UiConstants.TestTag.Item.fieldActionButton(getString(OSString.safeItemDetail_card_number_placeholder)),
            ).performClick()
            onNodeWithText(getString(OSString.common_delete)).performClick()
            onNode(isDialog())
                .assertIsDisplayed()
            onNodeWithText(getString(OSString.itemForm_deleteField_dialog_confirmButton))
                .performClick()
            onNodeWithTag(testTag = UiConstants.TestTag.Item.SaveAction)
                .assertIsEnabled()
                .performClick()
            hasText(errorString).waitUntilExactlyOneExists().assertIsDisplayed()
            hasExcludeSearch(hasText(getString(OSString.fieldName_elementName)))
                .waitUntilExactlyOneExists()
                .performTextInput(text = "item")
            hasText(errorString).waitUntilDoesNotExist().assertDoesNotExist()
            onNodeWithTag(testTag = UiConstants.TestTag.Item.SaveAction)
                .assertIsEnabled()
                .performClick()
            checkThatNavigationToItemDetailsIsDone(testUUIDs[0], "item")
            // Check that no information other than the title was saved
            hasText(getString(OSString.safeItemDetail_contentCard_informations_empty))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
        }
    }

    @Test
    fun create_item_change_identifier_test() {
        invoke {
            bottomSheetToItemCreationType(
                itemCreationTemplateField = ItemCreationTemplateUiField.Application,
                itemTextRes = OSString.createItem_template_applicationElement,
            )

            // Unset identifier
            onNodeWithTag(UiConstants.TestTag.Item.fieldActionButton(getString(OSString.fieldName_email)))
                .performClick()
            onNodeWithTag(UiConstants.TestTag.Menu.FieldActionMenu).assertIsDisplayed()
            onNodeWithText(context.getString(OSString.itemForm_action_dontUseAsIdentifier)).performClick()
            hasTestTag(UiConstants.TestTag.Menu.FieldActionMenu).waitUntilDoesNotExist()
            onNodeWithTag(UiConstants.TestTag.Item.IdentifierLabelText).assertDoesNotExist()

            // Set identifier
            waitForIdle()
            onNodeWithTag(UiConstants.TestTag.Item.fieldActionButton(getString(OSString.fieldName_password)))
                .performScrollTo()
                .performClick()
            onNodeWithTag(UiConstants.TestTag.Menu.FieldActionMenu).assertIsDisplayed()
            onNodeWithText(context.getString(OSString.itemForm_action_useAsIdentifier)).performClick()
            hasTestTag(UiConstants.TestTag.Menu.FieldActionMenu).waitUntilDoesNotExist()
            onNodeWithTag(UiConstants.TestTag.Item.IdentifierLabelText).assertExists().performClick()
            onNodeWithTag(UiConstants.TestTag.BottomSheet.IdentifierInfoBottomSheet).assertIsDisplayed()
        }
    }

    /**
     * Create a credit card template item
     * Enter a bad date -> Assert error is displayed
     * Enter a good date -> Assert no error
     * Save the item -> Assert the yearMonth Date is correctly formatted
     */
    @Test
    fun create_item_year_month_field_test() {
        val errorString = context.getString(OSString.safeItemDetail_yearMonth_errorLabel)
        invoke {
            bottomSheetToItemCreationType(
                itemCreationTemplateField = ItemCreationTemplateUiField.CreditCard,
                itemTextRes = OSString.createItem_template_creditCardElement,
            )
            hasText(getString(OSString.fieldName_card_expiryDate)).waitUntilExactlyOneExists()
                .performTextInput(text = "1322")
            onNodeWithTag(testTag = UiConstants.TestTag.Item.SaveAction).performClick()
            hasText(errorString).waitUntilExactlyOneExists().assertIsDisplayed()
            onNodeWithText(getString(OSString.fieldName_card_expiryDate)).performTextClearance()
            onNodeWithText(getString(OSString.fieldName_card_expiryDate)).performTextInput("1122")
            hasText(errorString).waitUntilDoesNotExist().assertDoesNotExist()
            hasExcludeSearch(hasText(getString(OSString.fieldName_elementName)))
                .waitUntilExactlyOneExists()
                .performTextInput(text = "item")
            onNodeWithTag(testTag = UiConstants.TestTag.Item.SaveAction).performClick()
            checkThatNavigationToItemDetailsIsDone(testUUIDs[0], "item")
            hasText("11/22").waitUntilExactlyOneExists(useUnmergedTree = true).assertExists()
        }
    }

    /**
     * Non reg test hotfix 1.15.1
     *
     * • Nav to create
     * • Autolock
     * • Login
     * • Save item
     * -> no crash + auto nav to item
     */
    // FIXME <Flaky> (critical)
    @Test
    fun autolock_from_create_form_test() {
        val itemName = "autolock"
        invoke {
            navigateToItemCreationCustomElementScreen()

            // Lock & login
            lockAppUseCase.blocking()
            hasTestTag(UiConstants.TestTag.Item.LoginPasswordTextField)
                .waitUntilExactlyOneExists()
                .performTextInput(testPassword)
            hasTestTag(UiConstants.TestTag.Item.LoginButtonIcon)
                .waitUntilExactlyOneExists()
                .performScrollTo()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.ItemFormScreen)
                .waitAndPrintRootToCacheDir(printRule)
                .assertIsDisplayed()

            createItem(itemName)
            checkThatNavigationToItemDetailsIsDone(testUUIDs[0], itemName)
        }
    }

    /**
     * Go to create item screen
     * Go to re-order screen.
     * Navigate back without modification
     * -> No dialog, create item screen is displayed
     * Go back again
     * -> No dialog, home is displayed
     * -> No item created
     */
    @Test
    fun item_creation_to_item_reorder_screen_without_changes_test() {
        invoke {
            bottomSheetToItemCreationType(
                itemCreationTemplateField = ItemCreationTemplateUiField.CreditCard,
                itemTextRes = OSString.createItem_template_creditCardElement,
            )
            hasContentDescription(getString(OSString.safeItemDetail_reorder))
                .waitUntilExactlyOneExists()
                .performScrollTo()
                .performClick()

            hasTestTag(UiConstants.TestTag.Screen.ItemReOrderScreen)
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()

            hasTestTag(UiConstants.TestTag.Item.SaveAction)
                .waitUntilExactlyOneExists()
                .assertIsNotEnabled()

            Espresso.pressBack()

            hasTestTag(UiConstants.TestTag.Screen.ItemFormScreen)
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()

            Espresso.pressBack()

            hasTestTag(UiConstants.TestTag.Screen.Home)
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
        }
    }

    private fun ComposeUiTest.bottomSheetToItemCreationType(
        itemCreationTemplateField: ItemCreationTemplateUiField,
        @StringRes itemTextRes: Int,
        hasSeenUrlTooltip: Boolean = true,
        hasSeenEmojiTooltip: Boolean = true,
    ) {
        runBlocking {
            appVisitRepository.setHasSeenItemEditionUrlToolTip(firstSafeId, hasSeenUrlTooltip)
            appVisitRepository.setHasSeenItemEditionEmojiToolTip(firstSafeId, hasSeenEmojiTooltip)
            appVisitRepository.setHasSeenItemReadEditToolTip(firstSafeId, true)
        }
        navToBottomSheet()
        val templateEntryMatcher = hasText(getString(itemTextRes))
        hasTestTag(UiConstants.TestTag.BottomSheet.CreateItemBottomSheet)
            .waitUntilExactlyOneExists()
            .performScrollToNode(templateEntryMatcher)

        templateEntryMatcher
            .waitUntilExactlyOneExists()
            .performClick()

        if (!hasSeenUrlTooltip) {
            hasText(getString(OSString.itemForm_tips_url_autoFetch))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()

            hasText(getString(OSString.common_tips_gotIt))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
                .performClick()

            hasText(getString(OSString.itemForm_tips_url_autoFetch))
                .waitUntilDoesNotExist()
                .assertDoesNotExist()
        }

        if (!hasSeenEmojiTooltip) {
            hasText(getString(OSString.itemForm_tips_title_emoji))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()

            hasText(getString(OSString.common_tips_gotIt))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
                .performClick()

            hasText(getString(OSString.itemForm_tips_title_emoji))
                .waitUntilDoesNotExist()
                .assertDoesNotExist()
        }

        onRoot()
            .printToCacheDir(printRule, suffix = "_${itemCreationTemplateField.javaClass.simpleName}")

        itemCreationTemplateField.getTextFields(fieldIdProvider).forEach { fieldDescription ->
            val text = activity.getLbcTextSpecResString(fieldDescription.fieldDescription.value)
            onNodeWithText(text).assertExists()
        }
    }

    private fun ComposeUiTest.navigateToItemCreationCustomElementScreen() {
        bottomSheetToItemCreationType(
            itemCreationTemplateField = ItemCreationTemplateUiField.Custom,
            itemTextRes = OSString.createItem_template_customElement,
        )
    }

    private fun ComposeUiTest.createItem(itemName: String) {
        hasExcludeSearch(hasText(getString(OSString.fieldName_elementName)))
            .waitUntilExactlyOneExists()
            .performClick() // request focus
        hasExcludeSearch(hasText(getString(OSString.safeItemDetail_title_placeholder)))
            .waitUntilExactlyOneExists()
            .performTextInput(text = itemName)
        onRoot()
            .printToCacheDir(printRule, suffix = "_createItemScreenFilled_$itemName")
        onNodeWithTag(testTag = UiConstants.TestTag.Item.SaveAction)
            .assertIsEnabled()
            .performClick()
        onRoot()
            .printToCacheDir(printRule, suffix = "_itemCreated_$itemName")
    }

    private fun ComposeUiTest.checkThatNavigationToItemDetailsIsDone(itemId: UUID, itemName: String) {
        hasTestTag(UiConstants.TestTag.Screen.itemDetailsScreen(itemId = itemId))
            .waitAndPrintRootToCacheDir(printRule = printRule, suffix = "_itemDetails_${itemId}_$itemName")
            .assertIsDisplayed()
    }

    private fun ComposeUiTest.navigateBack(parentItemId: UUID?, itemName: String) {
        onAllNodesWithContentDescription(getString(OSString.common_accessibility_back))
            .filterToOne(!hasAnyAncestor(hasTestTag(UiConstants.TestTag.Screen.SearchScreen)))
            .performClick()

        if (parentItemId == null) {
            onNodeWithTag(UiConstants.TestTag.Screen.Home)
                .assertIsDisplayed()
            onRoot()
                .printToCacheDir(printRule, suffix = "_home")
        } else {
            checkThatNavigationToItemDetailsIsDone(itemId = parentItemId, itemName = itemName)
        }
    }
}
