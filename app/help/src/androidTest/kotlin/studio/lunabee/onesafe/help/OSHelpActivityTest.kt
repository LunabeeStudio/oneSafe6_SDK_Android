package studio.lunabee.onesafe.help

import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runAndroidComposeUiTest
import androidx.test.espresso.Espresso
import studio.lunabee.onesafe.help.main.HelpActivity
import studio.lunabee.onesafe.test.OSActivityTest
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Run test with an [androidx.test.core.app.ActivityScenario] created with [HelpActivity].
 * Nothing is mocked, the database is impacted here (this is like someone is using the app)
 */
@OptIn(ExperimentalTestApi::class)
abstract class OSHelpActivityTest : OSActivityTest<HelpActivity>() {

    /**
     * Wrapper to run your test inside [HelpActivity]. No initial navigation are made.
     * Example:
     * ```
     * invoke {
     *      hasText(favoriteItemName)
     *          .waitUntilAtLeastOneExists()
     *          .filterToOne(hasAnyAncestor(hasTestTag(UiConstants.TestTag.Item.HomeItemSectionRow)))
     *          .assertIsDisplayed()
     *          .performClick()
     * }
     * ```
     */
    operator fun invoke(
        effectContext: CoroutineContext = EmptyCoroutineContext,
        block: AndroidComposeUiTest<HelpActivity>.() -> Unit,
    ) {
        runAndroidComposeUiTest(effectContext = effectContext) {
            this@OSHelpActivityTest.activity = activity!!
            initKeyboardHelper()
            try {
                closeInitialKeyboard()
                block()
            } catch (e: Throwable) {
                runCatching { onFailure(e) }
                throw e
            }
        }
    }

    private fun closeInitialKeyboard() {
        keyboardHelper.waitForKeyboardVisibility(visible = true, printRule = printRule)
        Espresso.closeSoftKeyboard()
        keyboardHelper.waitForKeyboardVisibility(visible = false, printRule = printRule)
    }
}
