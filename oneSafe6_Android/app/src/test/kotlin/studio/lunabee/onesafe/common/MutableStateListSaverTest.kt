package studio.lunabee.onesafe.common

import androidx.activity.ComponentActivity
import androidx.compose.material3.Button
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.RunWith
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.common.composable.rememberMutableStateListOf
import studio.lunabee.onesafe.test.InjectComponentActivityRule

@RunWith(AndroidJUnit4::class)
class MutableStateListSaverTest {
    @get:Rule(order = 0)
    val addActivityToRobolectricRule: TestWatcher = InjectComponentActivityRule()

    @get:Rule(order = 1)
    val composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity> =
        createAndroidComposeRule()

    private val stateRestoration: StateRestorationTester by lazy {
        StateRestorationTester(composeTestRule = composeTestRule)
    }

    /**
     * Test [androidx.compose.runtime.saveable.autoSaver] for default type.
     */
    @Test
    fun string_mutable_list_saver() {
        stateRestoration.setContent {
            val mutableListOf: MutableList<String> = rememberMutableStateListOf(elements = emptyList())
            OSText(text = LbcTextSpec.Raw(mutableListOf.joinToString()), modifier = Modifier.testTag(tag = TextTestTag))
            Button(
                onClick = { mutableListOf += "NewText" },
                modifier = Modifier
                    .testTag(tag = ButtonTestTag),
                content = { },
            )
        }

        composeTestRule.onNodeWithTag(testTag = TextTestTag).assertTextContains(value = "")
        composeTestRule.onNodeWithTag(testTag = ButtonTestTag).performClick()
        composeTestRule.onNodeWithTag(testTag = TextTestTag).assertTextContains(
            value = listOf("NewText").joinToString(),
        )

        stateRestoration.emulateSavedInstanceStateRestore()
        composeTestRule.onNodeWithTag(testTag = TextTestTag).assertTextContains(
            value = listOf("NewText").joinToString(),
        )
    }

    /**
     * Test use of a custom [Saver]. Write your own class test to test a custom [Saver].
     * This test just ensures that [rememberMutableStateListOf] use the custom [Saver] provided.
     */
    @Test
    fun custom_mutable_list_saver() {
        class TestContent(val id: String, val name: String) {
            override fun toString(): String = "$id$name"
        }

        val saver: Saver<TestContent, Any> = mapSaver(
            save = { mapOf("id" to it.id, "name" to it.name) },
            restore = { TestContent(id = it["id"] as String, name = it["name"] as String) },
        )

        val testContent = TestContent(id = "id", name = "name")
        stateRestoration.setContent {
            val mutableListOf: MutableList<TestContent> = rememberMutableStateListOf(
                elements = emptyList(),
                saver = saver,
            )
            OSText(
                text = LbcTextSpec.Raw(mutableListOf.getOrNull(0)?.toString().orEmpty()),
                modifier = Modifier.testTag(tag = TextTestTag),
            )
            Button(
                onClick = { mutableListOf += testContent },
                modifier = Modifier.testTag(tag = ButtonTestTag),
                content = { },
            )
        }

        composeTestRule.onNodeWithTag(testTag = TextTestTag).assertTextContains(value = "")
        composeTestRule.onNodeWithTag(testTag = ButtonTestTag).performClick()
        composeTestRule.onNodeWithTag(testTag = TextTestTag).assertTextContains(value = testContent.toString())

        stateRestoration.emulateSavedInstanceStateRestore()
        composeTestRule.onNodeWithTag(testTag = TextTestTag).assertTextContains(value = testContent.toString())
    }

    companion object {
        private const val TextTestTag: String = "TextTestTag"
        private const val ButtonTestTag: String = "ButtonTestTag"
    }
}
