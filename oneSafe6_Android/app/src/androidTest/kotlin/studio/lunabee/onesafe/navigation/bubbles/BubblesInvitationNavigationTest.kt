package studio.lunabee.onesafe.navigation.bubbles

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.AndroidComposeUiTestEnvironment
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.launchActivity
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import studio.lunabee.bubbles.domain.usecase.GetAllContactsUseCase
import studio.lunabee.compose.androidtest.extension.waitAndPrintRootToCacheDir
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.doubleratchet.model.DRMessageKey
import studio.lunabee.messaging.domain.model.DecryptResult
import studio.lunabee.onesafe.MainActivity
import studio.lunabee.onesafe.OSMainActivityTest
import studio.lunabee.onesafe.bubbles.ui.barcode.ScanBarcodeUiState
import studio.lunabee.onesafe.bubbles.ui.barcode.ScanBarcodeViewModel
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.extension.setIsTest
import studio.lunabee.onesafe.domain.usecase.settings.SetAppVisitUseCase
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.ui.UiConstants
import javax.inject.Inject

@HiltAndroidTest
@OptIn(ExperimentalTestApi::class)
class BubblesInvitationNavigationTest : OSMainActivityTest() {

    @get:Rule(order = 0)
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @Inject
    lateinit var setAppVisitUseCase: SetAppVisitUseCase

    override val initialTestState: InitialTestState = InitialTestState.SignedUp {
        setAppVisitUseCase.setHasDoneOnboardingBubbles()
    }

    private val uiBarcodeStateFlow: MutableStateFlow<ScanBarcodeUiState> = MutableStateFlow(ScanBarcodeUiState.Idle)

    companion object {
        @JvmStatic
        @BeforeClass
        fun cameraPermission() {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(
                context.packageName,
                Manifest.permission.CAMERA,
            )
        }
    }

    @BindValue
    val scanBarCodeViewModel: ScanBarcodeViewModel = mockk(relaxed = true) {
        every { dialogState } returns MutableStateFlow(null)
        every { uiResultState } returns uiBarcodeStateFlow
    }

    // Invitation url sample to avoid mocking double ratchet
    private val invitationMessage: String = Uri.decode(
        "ClswWTATBgcqhkjOPQIBBggqhkjOPQMBBwNCAATqeaDIwgouss" +
            "VmuEyeU%2FIfC0VXfXHEz8u19NQOFwk6VOq2MnKj3pFod4brdTxKfvYUqyAKgtsgTsW6m37GhngYElswWTATBgcqhkjOPQIBBggqhkjOPQMBBwNCAAR9YRcD" +
            "19%2Fu4u0MJVZwAO9aKhN63T6sGSNDTQxj1CIIbAKxREcnRKD1nrNSJd453GWBHyAP2Uef8M%2FS1wMPzhrwGiQwMmRkNTY5Yi0zNmYyLTQzNjktYWZkNC02Zjc" +
            "xYjNjZmYyMWQiJDI5MzJjZmU3LWQyZmQtNGQ3OS1hYzZmLTAyNjYxNjdiZTYxNg%3D%3D",
    )

    private val invitationIntent: Intent
        get() {
            val uri = Uri.Builder()
                .scheme("https")
                .authority("www.onesafe-apps.com")
                .path("bubbles")
                .fragment(invitationMessage)
                .build()
            return Intent(Intent.ACTION_VIEW, uri)
                .setIsTest()
        }

    @Inject
    lateinit var getContactUseCase: GetAllContactsUseCase

    // FIXME <Flaky>
    @Test
    fun full_bubbles_invitation_flow() {
        return
        invoke {
            invitationFlow()
            onNodeWithTag(UiConstants.TestTag.Item.InvitationList)
                .performScrollToNode(hasText(getString(OSString.common_continue)))
            hasText(getString(OSString.common_continue))
                .waitUntilExactlyOneExists()
                .performScrollTo()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.ScanBarCodeScreen)
                .waitAndPrintRootToCacheDir(printRule, "_scanBarcode")
            uiBarcodeStateFlow.value = runBlocking {
                ScanBarcodeUiState.NavigateToConversation(
                    DecryptResult.NewMessage(
                        getContactUseCase.invoke()
                            .first()
                            .first().id,
                        DRMessageKey(byteArrayOf()),
                    ),
                )
            }
            hasText("contact")
                .waitAndPrintRootToCacheDir(printRule, "_conversation")
        }
    }

    // FIXME <Flaky>
    @Test
    fun invitation_back_test() {
        return
        invoke {
            invitationFlow()
            backToFilledContactScreen()
        }
    }

    // FIXME <Flaky>
    fun full_bubbles_invitation_response_flow_test() {
        return
        invoke {
            invitationResponseFlow()
            onNodeWithTag(UiConstants.TestTag.Item.InvitationList)
                .performScrollToNode(hasText(getString(OSString.common_finish)))
            hasText(getString(OSString.common_finish))
                .waitUntilExactlyOneExists()
                .performScrollTo()
                .performClick()
            hasText(getString(OSString.bubbles_acceptedInvitation))
                .waitAndPrintRootToCacheDir(printRule, "_conversation")
        }
    }

    private fun AndroidComposeUiTest<MainActivity>.invitationFlow() {
        hasContentDescription(getString(OSString.accessibility_home_contacts_button_clickLabel))
            .waitUntilExactlyOneExists()
            .performClick()
        hasText(getString(OSString.bubbles_inviteContact_description))
            .waitAndPrintRootToCacheDir(printRule, "_contactTab")
        hasText(getString(OSString.bubbles_inviteContact))
            .waitUntilExactlyOneExists()
            .performClick()
        hasTestTag(UiConstants.TestTag.Screen.CreateContactScreen)
            .waitAndPrintRootToCacheDir(printRule, "_createContact")
        hasText(getString(OSString.bubbles_createContactScreen_textFieldLabel))
            .waitUntilExactlyOneExists()
            .performTextInput("contact")
        hasText(getString(OSString.bubbles_createContactScreen_fromScratch_invite)).and(
            isEnabled(),
        ).waitUntilExactlyOneExists()
            .performScrollTo()
            .performClick()
        hasTestTag(UiConstants.TestTag.Screen.InvitationScreen)
            .waitAndPrintRootToCacheDir(printRule, "_invitationScreen")
    }

    // FIXME <Flaky>
    @Test
    fun invitation_response_back_test() {
        return
        invoke {
            invitationResponseFlow()
            backToFilledContactScreen()
        }
    }

    private fun AndroidComposeUiTest<MainActivity>.backToFilledContactScreen() {
        hasContentDescription(getString(OSString.common_accessibility_back))
            .waitUntilExactlyOneExists()
            .performClick()
        hasTestTag(UiConstants.TestTag.Screen.BubblesHomeScreen)
            .waitAndPrintRootToCacheDir(printRule)
    }

    private fun AndroidComposeUiTest<MainActivity>.invitationResponseFlow() {
        hasContentDescription(getString(OSString.accessibility_home_contacts_button_clickLabel))
            .waitUntilExactlyOneExists()
            .performClick()
        hasText(getString(OSString.bubbles_inviteContact_description))
            .waitAndPrintRootToCacheDir(printRule, "_contactTab")
        hasText(getString(OSString.bubbles_scan))
            .waitUntilExactlyOneExists()
            .performClick()
        hasTestTag(UiConstants.TestTag.Screen.ScanBarCodeScreen)
            .waitAndPrintRootToCacheDir(printRule, "_scanBarcode")
        uiBarcodeStateFlow.value = runBlocking { ScanBarcodeUiState.NavigateToCreateContact(messageString = invitationMessage) }
        hasText(getString(OSString.bubbles_createContactScreen_textFieldLabel))
            .waitUntilExactlyOneExists()
            .performTextInput("contact")
        hasText(getString(OSString.common_finish)).and(
            isEnabled(),
        ).waitUntilExactlyOneExists()
            .performScrollTo()
            .performClick()
        hasTestTag(UiConstants.TestTag.Screen.InvitationResponseScreen)
            .waitAndPrintRootToCacheDir(printRule, "_invitationResponse")
    }

    // FIXME <Flaky>
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun deeplink_invitation_response_back_test() {
        return
        AndroidComposeUiTestEnvironment {
            activity
        }.runTest {
            val scenario = launchActivity<MainActivity>(invitationIntent)
            scenario.onActivity { this@BubblesInvitationNavigationTest.activity = it }
            initKeyboardHelper()
            login()

            hasText(getString(OSString.bubbles_createContactScreen_textFieldLabel))
                .waitAndPrintRootToCacheDir(printRule)
                .performScrollTo()
                .performTextInput("contact")
            hasText(getString(OSString.common_finish)).and(
                isEnabled(),
            )
                .waitUntilExactlyOneExists()
                .performScrollTo()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.InvitationResponseScreen)
                .waitAndPrintRootToCacheDir(printRule, "_invitationResponse")

            backToFilledContactScreen()
            logout() // avoid crash due to setting flow collection after test end
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun deeplink_invitation_create_contact_back_test() {
        AndroidComposeUiTestEnvironment {
            activity
        }.runTest {
            val scenario = launchActivity<MainActivity>(invitationIntent)
            scenario.onActivity { this@BubblesInvitationNavigationTest.activity = it }
            initKeyboardHelper()
            login()

            hasText(getString(OSString.bubbles_createContactScreen_textFieldLabel))
                .waitAndPrintRootToCacheDir(printRule)
                .performScrollTo()
                .performTextInput("contact")
            hasContentDescription(getString(OSString.common_accessibility_back)).and(
                isEnabled(),
            )
                .waitUntilExactlyOneExists()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.Home)
                .waitAndPrintRootToCacheDir(printRule)
            logout() // avoid crash due to setting flow collection after test end
        }
    }
}
