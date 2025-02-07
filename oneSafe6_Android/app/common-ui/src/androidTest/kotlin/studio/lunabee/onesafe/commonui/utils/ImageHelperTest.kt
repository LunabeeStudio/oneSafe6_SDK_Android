package studio.lunabee.onesafe.commonui.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertNull

class ImageHelperTest {

    private val imageHelper = ImageHelper(
        Dispatchers.Default,
        Dispatchers.Default,
    )

    @Test
    fun createBitmapWithText_empty_test(): TestResult = runTest {
        val bitmapSpaceEmoji = imageHelper.createBitmapWithText("\uDB40\uDC20")
        assertNull(bitmapSpaceEmoji)
        val bitmapEmpty = imageHelper.createBitmapWithText("")
        assertNull(bitmapEmpty)
    }
}
