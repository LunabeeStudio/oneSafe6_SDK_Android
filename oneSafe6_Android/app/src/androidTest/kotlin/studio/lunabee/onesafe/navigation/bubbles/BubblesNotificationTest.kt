package studio.lunabee.onesafe.navigation.bubbles

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.toKotlinInstant
import org.junit.Rule
import org.junit.Test
import studio.lunabee.bubbles.domain.usecase.CreateContactUseCase
import studio.lunabee.compose.androidtest.extension.waitAndPrintRootToCacheDir
import studio.lunabee.compose.androidtest.extension.waitUntilAtLeastOneExists
import studio.lunabee.compose.androidtest.extension.waitUntilDoesNotExist
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.messaging.domain.model.SharedMessage
import studio.lunabee.messaging.domain.usecase.SaveMessageUseCase
import studio.lunabee.onesafe.OSMainActivityTest
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.domain.usecase.settings.SetAppVisitUseCase
import studio.lunabee.onesafe.test
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.ui.UiConstants
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltAndroidTest
@OptIn(ExperimentalTestApi::class)
class BubblesNotificationTest : OSMainActivityTest() {

    private val contactId: UUID = UUID.randomUUID()
    private val recipientId: UUID = UUID.randomUUID()

    @get:Rule(order = 0)
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @Inject
    lateinit var setAppVisitUseCase: SetAppVisitUseCase

    override val initialTestState: InitialTestState = InitialTestState.Home {
        setAppVisitUseCase.setHasDoneOnboardingBubbles()
    }

    @Inject
    lateinit var createContactUseCase: CreateContactUseCase

    @Inject
    lateinit var saveMessageUseCase: SaveMessageUseCase

    @Test
    fun test_receive_message_display_notification(): TestResult = runTest(timeout = 30.seconds) {
        invoke {
            runBlocking {
                createContactUseCase.test(
                    id = DoubleRatchetUUID(contactId),
                    name = "Test",
                )
                saveMessageUseCase(
                    plainMessage = SharedMessage("message", DoubleRatchetUUID(recipientId), Instant.now().toKotlinInstant()),
                    contactId = DoubleRatchetUUID(contactId),
                    channel = "",
                    id = DoubleRatchetUUID(UUID.randomUUID()),
                    safeItemId = null,
                )
            }
            hasContentDescription(getString(OSString.accessibility_home_contacts_button_clickLabel))
                .waitUntilExactlyOneExists()
                .performClick()
            hasTestTag(UiConstants.TestTag.Item.NotificationIndicator)
                .waitUntilAtLeastOneExists(useUnmergedTree = true)
            hasText("Test")
                .waitAndPrintRootToCacheDir(printRule, "_notification_indicator")
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.WriteMessageScreen)
                .waitAndPrintRootToCacheDir(printRule, "_message")
            Espresso.closeSoftKeyboard()
            Espresso.pressBack()
            hasTestTag(UiConstants.TestTag.Screen.BubblesHomeScreen)
                .waitAndPrintRootToCacheDir(printRule, "_home_no_indicator")
            hasTestTag(UiConstants.TestTag.Item.NotificationIndicator)
                .waitUntilDoesNotExist(useUnmergedTree = true)
        }
    }
}
