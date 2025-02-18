package studio.lunabee.onesafe.commonui.extension

import android.util.Patterns
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.emoji2.text.EmojiCompat
import com.lunabee.lblogger.LBLogger
import com.lunabee.lblogger.e
import studio.lunabee.onesafe.commonui.utils.AnnotatedStringHelper

private val logger = LBLogger.get("StringExt")

fun String?.isValidUrl(): Boolean {
    return this != null && Patterns.WEB_URL.matcher(this).matches()
}

fun String.markdownToAnnotatedString(): AnnotatedString {
    return buildAnnotatedString {
        AnnotatedStringHelper.fillAnnotatedStringBuilderFromMarkdown(this@markdownToAnnotatedString, this)
    }
}

fun String.replaceSpaceWithAsciiChar(): String =
    replace(" ", AsciiSpaceChar)

fun String.revertAsciiCharIntoSpace(): String =
    replace(AsciiSpaceChar, " ")

fun String.formatNumber(): String {
    val numbers = this.filter { it.isDigit() }
    return when {
        numbers.length <= 5 -> numbers
        numbers.length in 6..7 -> numbers.chunked(2).joinToString(separator = " ")
        else -> numbers.chunked(4).joinToString(separator = " ")
    }
}

private const val AsciiSpaceChar: String = "␣"

fun String.startEmojiOrNull(): String? {
    return try {
        val emojiCompat = EmojiCompat.get()
        if (emojiCompat.loadState == EmojiCompat.LOAD_STATE_SUCCEEDED) {
            val trimmedText = this.trimStart()
            val emojiStart = emojiCompat.getEmojiStart(trimmedText.subSequence(0, trimmedText.length), 0)
            if (emojiStart > -1) {
                val emojiEnd = emojiCompat.getEmojiEnd(trimmedText.subSequence(0, trimmedText.length), 0)
                trimmedText.substring(emojiStart, emojiEnd)
            } else {
                null
            }
        } else {
            null
        }
    } catch (e: IllegalStateException) {
        logger.e(e)
        null
    }
}
