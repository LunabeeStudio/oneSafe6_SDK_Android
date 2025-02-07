package studio.lunabee.onesafe.navigation.itemform

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.bubbles.domain.crypto.BubblesCryptoEngine
import studio.lunabee.compose.androidtest.extension.printToCacheDir
import studio.lunabee.compose.androidtest.extension.waitAndPrintWholeScreenToCacheDir
import studio.lunabee.compose.androidtest.extension.waitUntilDoesNotExist
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.di.JCEModule
import studio.lunabee.onesafe.OSMainActivityTest
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.cryptography.android.ChachaPolyJCECryptoEngine
import studio.lunabee.onesafe.cryptography.android.CryptoEngine
import studio.lunabee.onesafe.cryptography.android.SecureIVProvider
import studio.lunabee.onesafe.domain.common.FieldIdProvider
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.feature.itemform.model.ItemCreationTemplateUiField
import studio.lunabee.onesafe.getLbcTextSpecResString
import studio.lunabee.onesafe.hasExcludeSearch
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.firstSafeId
import studio.lunabee.onesafe.ui.UiConstants
import javax.inject.Inject
import kotlin.test.assertEquals

@HiltAndroidTest
@OptIn(ExperimentalTestApi::class)
@UninstallModules(JCEModule::class)
class ItemBlockSaveNavigationTest : OSMainActivityTest() {
    @get:Rule(order = 0)
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.Home()

    @Inject
    lateinit var fieldIdProvider: FieldIdProvider

    @Inject
    lateinit var itemRepository: SafeItemRepository

    @BindValue
    val cryptoEngine: CryptoEngine = SlowCryptoEngine(ChachaPolyJCECryptoEngine(ivProvider = SecureIVProvider()))

    @BindValue
    val bubblesCryptoEngine: BubblesCryptoEngine = SlowCryptoEngine(ChachaPolyJCECryptoEngine(ivProvider = SecureIVProvider()))

    @Inject
    @ApplicationContext
    lateinit var context: Context

    private fun ComposeUiTest.navToBottomSheet() {
        // Navigate to BottomSheet
        hasTestTag(UiConstants.TestTag.BreadCrumb.OSCreateItemButton)
            .waitUntilExactlyOneExists()
            .performClick()
            .performTouchInput { swipeUp() }
        hasTestTag(UiConstants.TestTag.BottomSheet.CreateItemBottomSheet)
            .waitAndPrintWholeScreenToCacheDir(printRule, "_${UiConstants.TestTag.BottomSheet.CreateItemBottomSheet}")
    }

    /**
     * Flood the save button during item creation and assert only one item is actually created
     * ⚠️ The test does not exactly reproduce the real user interaction. The test pass even if we remove the ItemFormState check in
     * tryToSaveItem
     *
     * @see <a href="https://shorturl.ac/7db0u" />Notion</a>
     */
    @Test
    fun flood_save_button_test() {
        invoke {
            bottomSheetToItemCreationType(
                itemCreationTemplateField = ItemCreationTemplateUiField.Folder,
                itemTextRes = OSString.createItem_template_folderElement,
            )
            hasExcludeSearch(hasText(getString(OSString.fieldName_elementName)))
                .waitUntilExactlyOneExists()
                .performClick() // request focus
            hasExcludeSearch(hasText(getString(OSString.safeItemDetail_title_placeholder)))
                .waitUntilExactlyOneExists()
                .performTextInput(text = "flood_test")

            hasTestTag(UiConstants.TestTag.Item.SaveAction)
                .waitUntilExactlyOneExists()

            val saveBtn = hasTestTag(UiConstants.TestTag.Item.SaveAction)
                .waitUntilExactlyOneExists()

            repeat(10) {
                try {
                    saveBtn.performClick()
                } catch (e: Throwable) {
                    /* no-op */
                }
            }

            hasTestTag(UiConstants.TestTag.Screen.ItemFormScreen)
                .waitUntilDoesNotExist()
        }
        runTest {
            assertEquals(1, itemRepository.getSafeItemsCount(firstSafeId))
        }
    }

    private fun ComposeUiTest.bottomSheetToItemCreationType(
        itemCreationTemplateField: ItemCreationTemplateUiField,
        @StringRes itemTextRes: Int,
    ) {
        navToBottomSheet()
        val templateEntryMatcher = hasText(getString(itemTextRes))
        hasTestTag(UiConstants.TestTag.BottomSheet.CreateItemBottomSheet)
            .waitUntilExactlyOneExists()
            .performScrollToNode(templateEntryMatcher)

        templateEntryMatcher
            .waitUntilExactlyOneExists()
            .performClick()
        onRoot()
            .printToCacheDir(printRule, suffix = "_${itemCreationTemplateField.javaClass.simpleName}")

        itemCreationTemplateField.getTextFields(fieldIdProvider).forEach { fieldDescription ->
            val text = activity.getLbcTextSpecResString(fieldDescription.fieldDescription.value)
            onNodeWithText(text).assertExists()
        }
    }
}

private class SlowCryptoEngine(private val origin: ChachaPolyJCECryptoEngine) : CryptoEngine by origin {
    override fun encrypt(plainData: ByteArray, key: ByteArray, associatedData: ByteArray?): Result<ByteArray> {
        Thread.sleep(100)
        return origin.encrypt(plainData, key, associatedData)
    }
}
