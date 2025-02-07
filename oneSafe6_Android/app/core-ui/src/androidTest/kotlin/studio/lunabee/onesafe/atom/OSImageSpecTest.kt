package studio.lunabee.onesafe.atom

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.rules.ActivityScenarioRule
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals

class OSImageSpecTest {
    @get:Rule
    val composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity> =
        createAndroidComposeRule()

    private val stateRestoration: StateRestorationTester by lazy { StateRestorationTester(composeTestRule) }

    @Test
    fun uri_test() {
        val expected = Uri.EMPTY
        val osImageSpec = OSImageSpec.Uri(uri = expected)
        assertEquals(expected = expected, osImageSpec.getAs())
    }

    @Test
    fun byte_array_test() {
        val expected = byteArrayOf(100)
        val osImageSpec = OSImageSpec.Data(data = expected)
        assertEquals(expected = expected, osImageSpec.getAs())
    }

    @Test
    fun saver_test() {
        stateRestoration.setContent {
            val osImageSpec by rememberSaveable(stateSaver = OSImageSpec.Saver) {
                mutableStateOf(OSImageSpec.Uri(uri = Uri.EMPTY))
            }
            OSImage(image = osImageSpec, modifier = Modifier.testTag(tag = Uri.EMPTY.path.toString()))
        }
        composeTestRule.onNodeWithTag(testTag = Uri.EMPTY.path.toString()).assertIsDisplayed()
        stateRestoration.emulateSavedInstanceStateRestore()
        // Still display, no crash.
        composeTestRule.onNodeWithTag(testTag = Uri.EMPTY.path.toString()).assertIsDisplayed()
    }
}
