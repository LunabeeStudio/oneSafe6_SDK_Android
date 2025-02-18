package studio.lunabee.onesafe.ime

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.bubbles.domain.usecase.CreateContactUseCase
import studio.lunabee.compose.androidtest.extension.waitAndPrintRootToCacheDir
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.onesafe.OSMainActivityTest
import studio.lunabee.onesafe.test
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.testUUIDs
import studio.lunabee.onesafe.ui.UiConstants
import javax.inject.Inject

@OptIn(ExperimentalTestApi::class)
@HiltAndroidTest
class ImeDeeplinkHelperTest : OSMainActivityTest() {
    @get:Rule override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @Inject
    lateinit var createContactUseCase: CreateContactUseCase

    override val initialTestState: InitialTestState = InitialTestState.Home()

    @Test
    fun deeplinkBubblesOnboarding_onboarding_test(): Unit = invoke {
        ImeDeeplinkHelper.deeplinkBubblesOnboarding(this.activity!!)
        hasTestTag(UiConstants.TestTag.Screen.OnBoardingBubblesScreen)
            .waitAndPrintRootToCacheDir(printRule)
    }

    @Test
    fun deeplinkBubblesHomeContact_test(): Unit = invoke {
        ImeDeeplinkHelper.deeplinkBubblesHomeContact(this.activity!!)
        hasTestTag(UiConstants.TestTag.Screen.BubblesHomeScreen)
            .waitAndPrintRootToCacheDir(printRule)
        hasTestTag(UiConstants.TestTag.Screen.BubblesHomeScreenContactTab)
            .waitAndPrintRootToCacheDir(printRule)
    }

    @Test
    fun deeplinkBubblesWriteMessage_test(): Unit = invoke {
        runTest { createContactUseCase.test(DoubleRatchetUUID(testUUIDs[0])) }
        ImeDeeplinkHelper.deeplinkBubblesWriteMessage(this.activity!!, testUUIDs[0])
        hasTestTag(UiConstants.TestTag.Screen.WriteMessageScreen)
            .waitAndPrintRootToCacheDir(printRule)
    }
}
