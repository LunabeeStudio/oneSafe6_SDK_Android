package studio.lunabee.onesafe.navigation.bubbles

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.bubbles.domain.usecase.CreateContactUseCase
import studio.lunabee.compose.androidtest.extension.waitAndPrintRootToCacheDir
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.onesafe.OSMainActivityTest
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.domain.usecase.settings.SetAppVisitUseCase
import studio.lunabee.onesafe.test
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.ui.UiConstants
import javax.inject.Inject

@HiltAndroidTest
@OptIn(ExperimentalTestApi::class)
class BubbleScreenNavigationTest : OSMainActivityTest() {

    @get:Rule(order = 0)
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @Inject
    lateinit var setAppVisitUseCase: SetAppVisitUseCase

    override val initialTestState: InitialTestState = InitialTestState.Home {
        setAppVisitUseCase.setHasDoneOnboardingBubbles()
    }

    @Inject
    lateinit var createContactUseCase: CreateContactUseCase

    @Test
    fun navigate_to_bubbles_no_contact_land_on_contact_page() {
        invoke {
            hasContentDescription(getString(OSString.accessibility_home_contacts_button_clickLabel))
                .waitUntilExactlyOneExists()
                .performClick()
            hasText(getString(OSString.bubbles_inviteContact_description))
                .waitAndPrintRootToCacheDir(printRule, "_contactTab")
            hasText(getString(OSString.bubbles_conversations))
                .waitUntilExactlyOneExists()
                .performClick()
            hasText(getString(OSString.bubbles_noContact_title))
                .waitAndPrintRootToCacheDir(printRule, "_emptyConversationTab")
        }
    }

    @Test
    fun createContactAndNavigateToBubbleScreen() {
        invoke {
            runTest { createContactUseCase.test(name = "Test") }
            hasContentDescription(getString(OSString.accessibility_home_contacts_button_clickLabel))
                .waitUntilExactlyOneExists()
                .performClick()
            hasText(getString(OSString.bubbles_decryptMessage))
                .waitAndPrintRootToCacheDir(printRule, "_conversationTab")
            hasText(getString(OSString.bubbles_contacts))
                .waitUntilExactlyOneExists()
                .performClick()
            hasText("Test")
                .and(hasAnyAncestor(hasTestTag(UiConstants.TestTag.Screen.BubblesHomeScreenContactTab)))
                .waitUntilExactlyOneExists()
        }
    }
}
