package studio.lunabee.onesafe.extension

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import org.junit.Test
import studio.lunabee.onesafe.commonui.extension.formatNumber
import studio.lunabee.onesafe.commonui.extension.markdownToAnnotatedString
import studio.lunabee.onesafe.commonui.extension.replaceSpaceWithAsciiChar
import studio.lunabee.onesafe.commonui.extension.revertAsciiCharIntoSpace
import kotlin.test.assertEquals

class StringExtTest {

    @Test
    fun markdownToAnnotatedString_no_formatting_test() {
        val text = "I am a text without any formatting, let's put an * to test"
        val annotatedString: AnnotatedString = text.markdownToAnnotatedString()
        assertEquals(text, annotatedString.text)
    }

    @Test
    fun markdownToAnnotatedString_test() {
        val initialText = "I'm a **Bold** text, *italic* and <u>Underline</u> B**old part**. And an * and emph*asis*"
        val finalText = "I'm a Bold text, italic and Underline Bold part. And an * and emphasis"
        val annotatedString: AnnotatedString = initialText.markdownToAnnotatedString()

        assertEquals(finalText, annotatedString.text)

        assertEquals(5, annotatedString.spanStyles.size)
        val expected0 = AnnotatedString.Range(SpanStyle(fontWeight = FontWeight.Bold), 6, 10)
        val actual0 = annotatedString.spanStyles[0]
        assertEquals(expected0, actual0)
        val expected1 = AnnotatedString.Range(SpanStyle(fontStyle = FontStyle.Italic), 17, 23)
        val actual1 = annotatedString.spanStyles[1]
        assertEquals(expected1, actual1)
        val expected2 = AnnotatedString.Range(SpanStyle(textDecoration = TextDecoration.Underline), 28, 37)
        val actual2 = annotatedString.spanStyles[2]
        assertEquals(expected2, actual2)
        val expected3 = AnnotatedString.Range(SpanStyle(fontWeight = FontWeight.Bold), 39, 47)
        val actual3 = annotatedString.spanStyles[3]
        assertEquals(expected3, actual3)
        val expected4 = AnnotatedString.Range(SpanStyle(fontStyle = FontStyle.Italic), 66, 70)
        val actual4 = annotatedString.spanStyles[4]
        assertEquals(expected4, actual4)
    }

    @Test
    fun replace_space_with_ascii_char_test() {
        assertEquals("This is a text".replaceSpaceWithAsciiChar(), "This␣is␣a␣text")
        assertEquals("double  space".replaceSpaceWithAsciiChar(), "double␣␣space")
        assertEquals("test␣ascii".replaceSpaceWithAsciiChar(), "test␣ascii")
    }

    @Test
    fun revert_ascii_char_into_space_test() {
        assertEquals("This␣is␣a␣text".revertAsciiCharIntoSpace(), "This is a text")
        assertEquals("double␣␣space".revertAsciiCharIntoSpace(), "double  space")
        assertEquals("test ascii".revertAsciiCharIntoSpace(), "test ascii")
    }

    @Test
    fun format_number_test() {
        val testCases = listOf(
            "" to "",
            "12345 " to "12345",
            "1234" to "1234",
            "12345" to "12345",
            "123456" to "12 34 56",
            "1234567" to "12 34 56 7",
            "12345678" to "1234 5678",
            "123456789" to "1234 5678 9",
            "12345678910111213" to "1234 5678 9101 1121 3",
        )
        testCases.forEach { testCase ->
            assertEquals(testCase.second, testCase.first.formatNumber())
        }
    }
}
