package studio.lunabee.onesafe.navigation

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.onesafe.OSMainActivityTest
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.blocking
import studio.lunabee.onesafe.ui.UiConstants

@OptIn(ExperimentalTestApi::class)
@HiltAndroidTest
class BrokenSessionNavigationTest : OSMainActivityTest() {

    @get:Rule(order = 0)
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)
    override val initialTestState: InitialTestState = InitialTestState.Home()

    /**
     * Non reg test from 6.2.12.1
     */
    @Test
    fun autolock_twice_test() {
        invoke {
            hasTestTag(UiConstants.TestTag.Screen.Home)
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
            lockAppUseCase.blocking()
            hasTestTag(UiConstants.TestTag.Screen.Login)
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
            login()
            hasTestTag(UiConstants.TestTag.Screen.Home)
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
            lockAppUseCase.blocking()
            hasTestTag(UiConstants.TestTag.Screen.Login)
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
        }
    }
}
