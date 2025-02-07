package studio.lunabee.onesafe

import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runAndroidComposeUiTest
import dagger.hilt.android.testing.HiltAndroidTest
import studio.lunabee.onesafe.test.OSActivityTest
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Run test with an [androidx.test.core.app.ActivityScenario] created with [MainActivity].
 *
 * Example:
 * ```
 * class MyMainActivityTest: OSMainActivityTest() {
 *     @get:Rule(order = 0) // this is mandatory
 *     override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)
 *
 *     override val initialTestState: InitialTestState = InitialTestState.SignedIn {
 *         createItemUseCase("itemName", null, false, null, null)
 *     }
 *
 *      @Test
 *      fun mu_navigation_test() {
 *          runTest {
 *             loginUseCase(testPassword.toCharArray())
 *             createItemUseCase.test(name = favoriteItemName, isFavorite = true)
 *             unloadMasterKey()
 *         }
 *         invoke {
 *             navigateFromLoginToHome() // Sign-in
 *             hasText(favoriteItemName)
 *                 .waitUntilAtLeastOneExists()
 *                 .filterToOne(hasAnyAncestor(hasTestTag(UiConstants.TestTag.Item.HomeItemSectionRow)))
 *                 .assertIsDisplayed()
 *                 .performClick()
 *             hasTestTag(UiConstants.TestTag.Screen.itemDetailsScreen(testUUIDs[1]))
 *                 .waitAndPrintRootToCacheDir( printRule)
 *                 .assertIsDisplayed()
 *         }
 *      }
 * }
 * ```
 */
@OptIn(ExperimentalTestApi::class)
@HiltAndroidTest
abstract class OSMainActivityTest : OSActivityTest<MainActivity>() {

    /**
     * Wrapper to run your test inside [MainActivity]. No initial navigation are made.
     * Example:
     * ```
     * invoke {
     *      hasText(favoriteItemName)
     *          .waitUntilAtLeastOneExists()
     *          .filterToOne(hasAnyAncestor(hasTestTag(UiConstants.TestTag.Item.HomeItemSectionRow)))
     *          .assertIsDisplayed()
     *          .performClick()
     * }
     *
     * invoke(navigateToHome = false) {
     *      // Here your are starting from the start destination depending on your InitialTestState
     * }
     * ```
     */
    operator fun invoke(
        effectContext: CoroutineContext = EmptyCoroutineContext,
        block: AndroidComposeUiTest<MainActivity>.() -> Unit,
    ) {
        runAndroidComposeUiTest(effectContext = effectContext) {
            this@OSMainActivityTest.activity = activity!!
            initKeyboardHelper()
            try {
                block()
            } catch (e: Throwable) {
                runCatching { onFailure(e) }
                throw e
            }
        }
    }
}
