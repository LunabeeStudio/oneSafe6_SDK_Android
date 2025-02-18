package studio.lunabee.onesafe.common.composable

import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.RunWith
import studio.lunabee.compose.androidtest.LbcComposeTest
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.commonui.bottomsheet.BottomSheetHolder
import studio.lunabee.onesafe.commonui.bottomsheet.BottomSheetHolderColumnContent
import studio.lunabee.onesafe.test.InjectComponentActivityRule

@OptIn(ExperimentalTestApi::class, ExperimentalMaterial3Api::class)
@RunWith(AndroidJUnit4::class)
class BottomSheetHolderTest : LbcComposeTest() {

    @get:Rule(order = 0)
    val addActivityToRobolectricRule: TestWatcher = InjectComponentActivityRule()

    private val tag = "my_bottom_sheet"

    @Test
    fun hidden_bottomSheet_test() {
        invoke {
            setContent {
                BottomSheetHolder(
                    isVisible = false,
                    onBottomSheetClosed = {},
                    skipPartiallyExpanded = false,
                ) { _, _ ->
                    OSText(LbcTextSpec.Raw("test"), Modifier.testTag(tag))
                }
            }

            onNodeWithTag(tag).assertDoesNotExist()
        }
    }

    @Test
    fun visible_bottomSheet_test() {
        invoke {
            setContent {
                BottomSheetHolder(
                    isVisible = true,
                    onBottomSheetClosed = {},
                    skipPartiallyExpanded = false,
                ) { _, _ ->
                    OSText(LbcTextSpec.Raw("test"), Modifier.testTag(tag))
                }
            }

            onNodeWithTag(tag, true).assertIsDisplayed()
        }
    }

    @Test
    fun show_hide_bottomSheet_test() {
        invoke {
            val isVisibleFlow = MutableStateFlow(false)

            setContent {
                val isVisible by isVisibleFlow.collectAsState()
                BottomSheetHolder(
                    isVisible = isVisible,
                    onBottomSheetClosed = {},
                    skipPartiallyExpanded = false,
                ) { _, _ ->
                    OSText(LbcTextSpec.Raw("test"), Modifier.testTag(tag))
                }
            }

            onNodeWithTag(tag).assertDoesNotExist()
            isVisibleFlow.value = true
            onNodeWithTag(tag, true).assertIsDisplayed()
            isVisibleFlow.value = false
            onNodeWithTag(tag).assertDoesNotExist()
        }
    }

    @Test
    fun bottomSheet_columnContent_test() {
        invoke {
            setContent {
                BottomSheetHolder(
                    isVisible = true,
                    onBottomSheetClosed = {},
                    skipPartiallyExpanded = false,
                ) { _, paddingValues ->
                    BottomSheetHolderColumnContent(paddingValues = paddingValues) {
                        Button(onClick = { }) {
                            OSText(LbcTextSpec.Raw("clickable"), Modifier.testTag(tag))
                        }
                    }
                }
            }

            onNodeWithTag(tag, true)
                .assertIsDisplayed()
                .performClick()
        }
    }
}
