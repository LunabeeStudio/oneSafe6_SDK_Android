package studio.lunabee.onesafe.item

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import org.junit.Test
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.extension.iconSample
import studio.lunabee.onesafe.model.OSItemIllustration
import studio.lunabee.onesafe.model.OSSafeItemStyle
import studio.lunabee.onesafe.molecule.OSSafeItem
import studio.lunabee.compose.androidtest.LbcComposeTest
import studio.lunabee.compose.androidtest.extension.printToCacheDir
import studio.lunabee.onesafe.ui.UiConstants
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class OSSafeItemTest : LbcComposeTest() {
    @Test
    fun simple_name_test() {
        val label = "abc"
        setOSSafeItem(label) {
            val textNode = onNodeWithTag(
                testTag = UiConstants.TestTag.OSSafeItemText,
                useUnmergedTree = true,
            ).assertExists()
            val layoutResult = textNode.getTextLayoutResult()

            assertFalse { layoutResult.hasVisualOverflow }
            assertEquals(1, layoutResult.lineCount)
        }
    }

    /**
     * Test ellipsize of (not so) long word
     */
    @Test
    fun ellipsize_name_test() {
        lateinit var label: MutableState<String>
        invoke {
            setContent {
                label = remember { mutableStateOf("abcdefghijklmno") }

                OSSafeItem(
                    illustration = OSItemIllustration.Image(OSImageSpec.Data(iconSample)),
                    style = OSSafeItemStyle.Regular,
                    label = LbcTextSpec.Raw(label.value),
                    modifier = Modifier
                        .width(56.dp),
                    contentDescription = null,
                )
            }

            val textNode = onNodeWithTag(
                testTag = UiConstants.TestTag.OSSafeItemText,
                useUnmergedTree = true,
            ).assertExists()

            var result = textNode.getTextLayoutResult()
            while (result.lineCount != 1) {
                // Reduce string until ellipsis
                label.value = label.value.take(label.value.count() - 1)
                result = textNode.getTextLayoutResult()
            }

            onRoot().printToCacheDir(printRule, "_OSSafeItem")

            assertTrue { result.layoutInput.text.text.endsWith(Typography.ellipsis) }
        }
    }

    /**
     * Test the line wrap of long word
     */
    @Test
    fun cut_name_test() {
        val label = "abcdefghijklmno"
        setOSSafeItem(label) {
            val textNode = onNodeWithTag(
                testTag = UiConstants.TestTag.OSSafeItemText,
                useUnmergedTree = true,
            ).assertExists()
            val layoutResult = textNode.getTextLayoutResult()

            assertFalse { layoutResult.hasVisualOverflow }
            assertEquals(2, layoutResult.lineCount)
        }
    }

    /**
     * Test the line wrap + ellipsize of very long word
     */
    @Test
    fun cut_long_name_test() {
        val label = "abcdefghijklmnopqrstuvwxyz"
        setOSSafeItem(label) {
            val textNode = onNodeWithTag(
                testTag = UiConstants.TestTag.OSSafeItemText,
                useUnmergedTree = true,
            ).assertExists()
            val layoutResult = textNode.getTextLayoutResult()

            assertFalse { layoutResult.hasVisualOverflow }
            assertEquals(2, layoutResult.lineCount)
            assertTrue { layoutResult.layoutInput.text.endsWith(Typography.ellipsis) }
        }
    }

    /**
     * Test line break after first word + ellipsize second line
     */
    @Test
    fun cut_two_words_long_name_test() {
        val label = "a bcdefghijklmnopqrstuvwxyz"
        setOSSafeItem(label) {
            val textNode = onNodeWithTag(
                testTag = UiConstants.TestTag.OSSafeItemText,
                useUnmergedTree = true,
            )
                .assertExists()
                .assertTextContains(value = "a bc", substring = true)
            val layoutResult = textNode.getTextLayoutResult()

            assertFalse { layoutResult.hasVisualOverflow }
            assertEquals(2, layoutResult.lineCount)
            assertTrue { layoutResult.layoutInput.text.endsWith(Typography.ellipsis) }
            val firstLineEnd = layoutResult.getLineEnd(0, visibleEnd = true)
            val secondLineStart = layoutResult.getLineStart(1)
            assertEquals(1, firstLineEnd) // a
            assertEquals("bcd", label.substring(secondLineStart, secondLineStart + 3)) // bcd
        }
    }

    private fun setOSSafeItem(label: String, block: ComposeUiTest.() -> Unit) {
        invoke {
            setContent {
                OSSafeItem(
                    illustration = OSItemIllustration.Image(OSImageSpec.Data(iconSample)),
                    style = OSSafeItemStyle.Regular,
                    label = LbcTextSpec.Raw(label),
                    modifier = Modifier
                        .width(56.dp),
                )
            }

            onRoot().printToCacheDir(printRule, "_OSSafeItem")

            block()
        }
    }

    private fun SemanticsNodeInteraction.getTextLayoutResult(): TextLayoutResult {
        val textLayoutResults = mutableListOf<TextLayoutResult>()
        fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action?.invoke(textLayoutResults)
        return textLayoutResults.first()
    }
}
