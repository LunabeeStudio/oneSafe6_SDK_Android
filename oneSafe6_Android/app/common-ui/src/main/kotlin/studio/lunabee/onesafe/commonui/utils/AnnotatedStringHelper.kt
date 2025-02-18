package studio.lunabee.onesafe.commonui.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration

object AnnotatedStringHelper {

    // TODO : https://www.notion.so/lunabeestudio/Conversion-from-Markdown-to-AnnotatedString-da1aa9cd59724985aa24bbc0fd19f1ea
    fun fillAnnotatedStringBuilderFromMarkdown(string: String, to: AnnotatedString.Builder) {
        var newStr = string
        MainRegex.findAll(string).map { it.value }.forEach { matching: String ->

            val start = newStr.indexOf(matching)
            var end = start + matching.length // (the EXCLUSIVE end of the range)
            if (start > -1) {
                when {
                    matching.matches(BoldRegex) -> {
                        end -= 2 * BoldTag.length
                        val matchingReplace = matching.replace(BoldTag, "")
                        newStr = newStr.replace(matching, matchingReplace)
                        to.addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
                    }

                    matching.matches(ItalicRegex) -> {
                        end -= 2 * ItalicTag.length
                        val matchingReplace = matching.replace(ItalicTag, "")
                        newStr = newStr.replace(matching, matchingReplace)
                        to.addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, end)
                    }
                    matching.matches(UnderlineRegex) -> {
                        end -= UnderlineOpenTag.length + UnderlineCloseTag.length
                        val matchingReplace = matching.replace(UnderlineOpenTag, "").replace(UnderlineCloseTag, "")
                        newStr = newStr.replace(matching, matchingReplace)
                        to.addStyle(SpanStyle(textDecoration = TextDecoration.Underline), start, end)
                    }
                }
            }
        }

        to.append(newStr)
    }

    private const val BoldRegexPattern: String = "\\*\\*(.*?)\\*\\*"
    private const val ItalicRegexPattern: String = "\\*(?![*\\s])(?:[^*]*[^*\\s])?\\*"
    private const val UnderlineRegexPattern: String = "<u\\s*.*>\\s*.*<\\/u>"

    private val BoldRegex: Regex = Regex(BoldRegexPattern)
    private val ItalicRegex: Regex = Regex(ItalicRegexPattern)
    private val UnderlineRegex: Regex = Regex(UnderlineRegexPattern)
    private val MainRegex: Regex = Regex("$BoldRegexPattern|$ItalicRegexPattern|$UnderlineRegexPattern")

    private const val BoldTag: String = "**"
    private const val ItalicTag: String = "*"
    private const val UnderlineOpenTag: String = "<u>"
    private const val UnderlineCloseTag: String = "</u>"
}
