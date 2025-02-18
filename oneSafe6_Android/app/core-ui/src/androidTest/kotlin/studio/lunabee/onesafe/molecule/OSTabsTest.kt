package studio.lunabee.onesafe.molecule

import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performScrollToNode
import kotlin.test.Test
import studio.lunabee.compose.androidtest.LbcComposeTest
import studio.lunabee.compose.androidtest.extension.printToCacheDir
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.molecule.tabs.OSTabs
import studio.lunabee.onesafe.molecule.tabs.TabsData
import studio.lunabee.onesafe.ui.theme.OSTheme

@OptIn(ExperimentalTestApi::class)
class OSTabsTest : LbcComposeTest() {
    @Test
    fun ostabs_scroll_test() {
        val titles = listOf("aaaaaa", "bbb", "ccccc", "dd", "eeeeeeee", "ffff", "gggggggggggg").associateWith { null }.toList()
        invoke {
            setContent {
                OSTheme {
                    OSTabs(
                        data = titles.map { TabsData(title = LbcTextSpec.Raw(it.first), contentDescription = it.second) },
                        selectedTabIndex = 0,
                        modifier = Modifier,
                        onTabSelected = {},
                    )
                }
            }

            onRoot().printToCacheDir(printRule, "_before_scroll")
            onNode(hasScrollAction()).performScrollToNode(hasText(titles.last().first))
            onRoot().printToCacheDir(printRule, "_after_scroll")
            onNodeWithText(text = titles.last().first, useUnmergedTree = true).assertIsDisplayed()
        }
    }
}
