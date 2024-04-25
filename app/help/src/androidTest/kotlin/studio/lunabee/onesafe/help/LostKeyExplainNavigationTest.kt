package studio.lunabee.onesafe.help

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.ui.UiConstants
import javax.inject.Inject

@HiltAndroidTest
@OptIn(ExperimentalTestApi::class)
class LostKeyExplainNavigationTest @Inject constructor() : OSHelpActivityTest() {

    @get:Rule override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)
    override val initialTestState: InitialTestState = InitialTestState.SignedUp()

    @Test
    fun navigate_to_lostKey_test() {
        invoke {
            hasText(targetContext.getString(OSString.cipherRecover_keyCard_whyButton))
                .waitUntilExactlyOneExists()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.LostKeyExplainScreen)
                .waitUntilExactlyOneExists()
            Espresso.pressBack()
            hasTestTag(UiConstants.TestTag.Screen.CipherKeyPromptScreen)
                .waitUntilExactlyOneExists()
        }
    }
}
