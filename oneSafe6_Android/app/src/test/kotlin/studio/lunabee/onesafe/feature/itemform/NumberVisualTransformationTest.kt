package studio.lunabee.onesafe.feature.itemform

import androidx.compose.ui.text.AnnotatedString
import org.junit.Test
import studio.lunabee.onesafe.feature.itemform.screen.NumberVisualTransformation
import kotlin.test.assertEquals

class NumberVisualTransformationTest {

    private val numberVisualTransformation: NumberVisualTransformation = NumberVisualTransformation()

    /**
     * Compute offset for empty transformation
     * Non reg https://www.notion.so/lunabeestudio/Crash-past-in-pin-field-bd128524e1c44292a8e4478356ceabc8?pvs=4
     */
    @Test
    fun filter_test() {
        val transformedText = numberVisualTransformation.filter(AnnotatedString("abcd"))
        val actual = transformedText.offsetMapping.originalToTransformed(1)
        assertEquals(0, actual)
    }
}
