package studio.lunabee.onesafe.common.model

import androidx.emoji2.bundled.BundledEmojiCompatConfig
import androidx.emoji2.text.EmojiCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.runner.RunWith
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.AppAndroidTestUtils
import studio.lunabee.onesafe.commonui.EmojiNameProvider
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class EmojiNameProviderTest {
    @Test
    fun placeholder_test() {
        EmojiCompat.init(BundledEmojiCompatConfig(InstrumentationRegistry.getInstrumentation().targetContext))
        AppAndroidTestUtils.waitEmojiInit()

        val tests = listOf(
            "🐺 abc" to LbcTextSpec.Raw("🐺"),
            "   🐺 abc" to LbcTextSpec.Raw("🐺"),
            "abc 🦄" to LbcTextSpec.Raw("A"),
            "🐺🌈 abc" to LbcTextSpec.Raw("🐺"),
        )
        tests.forEachIndexed { idx, (name, expected) ->
            val placeholder: LbcTextSpec = EmojiNameProvider(name).placeholderName
            assertEquals(expected = expected, actual = placeholder, "Failed at $idx")
        }
    }
}
