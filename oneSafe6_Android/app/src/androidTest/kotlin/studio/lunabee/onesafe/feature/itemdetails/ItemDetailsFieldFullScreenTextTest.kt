package studio.lunabee.onesafe.feature.itemdetails

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import org.junit.Test
import studio.lunabee.compose.androidtest.LbcComposeTest
import studio.lunabee.compose.androidtest.extension.printToCacheDir
import studio.lunabee.onesafe.extension.loremIpsum
import studio.lunabee.onesafe.extension.loremIpsumSpec
import studio.lunabee.onesafe.feature.itemfielddetail.screen.ItemFieldDetailsTextScreen
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.theme.OSTheme

@OptIn(ExperimentalTestApi::class)
class ItemDetailsFieldFullScreenTextTest : LbcComposeTest() {

    @Test
    fun note_is_displayed() {
        invoke {
            setContent {
                OSTheme {
                    ItemFieldDetailsTextScreen(
                        fieldName = loremIpsumSpec(1),
                        fieldValue = loremIpsumSpec(words = 200),
                        navigateBack = { },
                    )
                }
            }
            onNodeWithTag(UiConstants.TestTag.Screen.ItemDetailsFieldFullScreen).assertIsDisplayed()
            onNodeWithText(loremIpsum(200)).assertIsDisplayed()
            onRoot().printToCacheDir(printRule)
        }
    }
}
