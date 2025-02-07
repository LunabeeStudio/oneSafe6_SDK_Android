package studio.lunabee.onesafe.navigation.settings

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.onesafe.OSMainActivityTest
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.StaticIdProvider
import studio.lunabee.onesafe.test.testUUIDs
import studio.lunabee.onesafe.ui.UiConstants

@HiltAndroidTest
@OptIn(ExperimentalTestApi::class)
class DeleteSafeNavigationTest : OSMainActivityTest() {

    @get:Rule(order = 0)
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.Home {
        StaticIdProvider.id = testUUIDs[1]
        generateCryptoForNewSafeUseCase(passwordB.toCharArray())
        finishSafeCreationUseCase()
    }

    private val passwordB = "B"

    @Test
    fun remove_first_safe_test(): Unit = invoke {
        SettingsNavigationTest.navToSettings()
        val deleteSafeTextMatcher = hasText(getString(OSString.settings_multiSafe_deleteSafe))
        hasTestTag(UiConstants.TestTag.ScrollableContent.SettingsSafeLazyColumn)
            .waitUntilExactlyOneExists()
            .performScrollToNode(deleteSafeTextMatcher)
        deleteSafeTextMatcher
            .waitUntilExactlyOneExists()
            .performClick()
        hasText(getString(OSString.settings_section_safeAction_accountDeletion_alert_confirm))
            .waitUntilExactlyOneExists()
            .performClick()
        hasTestTag(UiConstants.TestTag.Screen.Login)
            .waitUntilExactlyOneExists()
        login(passwordB)
        hasTestTag(UiConstants.TestTag.Screen.Home)
            .waitUntilExactlyOneExists()
    }
}
