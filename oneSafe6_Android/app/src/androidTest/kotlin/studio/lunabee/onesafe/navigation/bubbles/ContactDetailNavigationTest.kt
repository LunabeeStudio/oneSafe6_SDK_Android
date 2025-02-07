package studio.lunabee.onesafe.navigation.bubbles

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.bubbles.domain.model.contact.PlainContact
import studio.lunabee.bubbles.domain.usecase.CreateContactUseCase
import studio.lunabee.compose.androidtest.extension.waitAndPrintWholeScreenToCacheDir
import studio.lunabee.compose.androidtest.extension.waitUntilDoesNotExist
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.onesafe.OSMainActivityTest
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.domain.usecase.settings.SetAppVisitUseCase
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.ui.UiConstants
import java.util.UUID
import javax.inject.Inject

@HiltAndroidTest
@OptIn(ExperimentalTestApi::class)
class ContactDetailNavigationTest : OSMainActivityTest() {

    @get:Rule(order = 0)
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @Inject
    lateinit var setAppVisitUseCase: SetAppVisitUseCase

    @Inject
    lateinit var createContactUseCase: CreateContactUseCase

    override val initialTestState: InitialTestState = InitialTestState.Home {
        setAppVisitUseCase.setHasDoneOnboardingBubbles()
        createContactUseCase(
            PlainContact(
                id = DoubleRatchetUUID(UUID.randomUUID()),
                name = "Test",
                sharedKey = null,
                sharedConversationId = DoubleRatchetUUID(UUID.randomUUID()),
            ),
        )
    }

    @Test
    fun navigate_to_contact_detail_and_remove_contact() {
        invoke {
            hasContentDescription(getString(OSString.accessibility_home_contacts_button_clickLabel))
                .waitUntilExactlyOneExists()
                .performClick()

            hasText(getString(OSString.bubbles_contacts))
                .waitUntilExactlyOneExists()
                .performClick()
            hasText("Test")
                .and(hasAnyAncestor(hasTestTag(UiConstants.TestTag.Screen.BubblesHomeScreenContactTab)))
                .waitUntilExactlyOneExists()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.ContactDetailScreen)
                .waitAndPrintWholeScreenToCacheDir(printRule, "_contactDetail")
            hasText(getString(OSString.bubbles_contactDetail_deleteContact))
                .waitUntilExactlyOneExists()
                .performClick()
            onAllNodesWithText(getString(OSString.bubbles_contactDetail_delete_confirm)).filterToOne(
                hasAnyAncestor(isDialog()),
            )
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.BubblesHomeScreen)
                .waitAndPrintWholeScreenToCacheDir(printRule, "_noContact")
            hasText("Test")
                .waitUntilDoesNotExist()
        }
    }
}
