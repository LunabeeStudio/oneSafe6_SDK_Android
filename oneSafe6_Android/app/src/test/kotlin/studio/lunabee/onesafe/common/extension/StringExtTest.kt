package studio.lunabee.onesafe.common.extension

import androidx.emoji2.bundled.BundledEmojiCompatConfig
import androidx.emoji2.text.EmojiCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.runner.RunWith
import studio.lunabee.onesafe.AppAndroidTestUtils
import studio.lunabee.onesafe.commonui.extension.startEmojiOrNull
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class StringExtTest {

    @Test
    fun startEmojiOrNull_test() {
        EmojiCompat.init(BundledEmojiCompatConfig(InstrumentationRegistry.getInstrumentation().targetContext))
        AppAndroidTestUtils.waitEmojiInit()

        val testCases = listOf(
            "" to null,
            "abc" to null,
            "\uD83D\uDE00" to "\uD83D\uDE00",
            "\uD83D\uDD11 abc" to "\uD83D\uDD11",
            "\uD83D\uDC68\u200D\uD83D\uDC67\u200D\uD83D\uDC67🐒 abc" to "\uD83D\uDC68\u200D\uD83D\uDC67\u200D\uD83D\uDC67",
            "abc \uD83D\uDD11" to null,
            "    \uD83D\uDD11" to "\uD83D\uDD11",
            "\uDB40\uDC20" to null, // empty emoji
        )
        testCases.forEachIndexed { idx, testCase ->
            assertEquals(testCase.second, testCase.first.startEmojiOrNull(), "Fail at $idx")
        }
    }
}
