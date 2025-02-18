/*
 * Copyright (c) 2024 Lunabee Studio
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Created by Lunabee Studio / Date - 7/25/2024 - for the oneSafe6 SDK.
 * Last modified 7/25/24, 10:31 PM
 */

package studio.lunabee.onesafe.navigation.safe

import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import androidx.test.espresso.NoActivityResumedException
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.compose.androidtest.extension.waitAndPrintRootToCacheDir
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.onesafe.MainActivity
import studio.lunabee.onesafe.OSMainActivityTest
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.navigation.onboardingPasswordCreation
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.StaticIdProvider
import studio.lunabee.onesafe.test.assertThrows
import studio.lunabee.onesafe.test.testUUIDs
import studio.lunabee.onesafe.ui.UiConstants

@HiltAndroidTest
@OptIn(ExperimentalTestApi::class)
class CreateSafeLoginNavigationTest : OSMainActivityTest() {

    @get:Rule(order = 0)
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.SignedUp()

    @Test
    fun new_safe_from_login_navigation_test() {
        invoke {
            StaticIdProvider.id = testUUIDs[1]
            runNewSafeOnboarding()
        }
    }

    @Test
    fun new_safe_from_login_navigation_exit_on_back_test() {
        invoke {
            StaticIdProvider.id = testUUIDs[1]
            runNewSafeOnboarding()
            runCatching {
                keyboardHelper.waitForKeyboardVisibility(true, timeout = 2_000)
                Espresso.closeSoftKeyboard()
            }
            assertThrows<NoActivityResumedException> {
                Espresso.pressBack()
            }
        }
    }

    private fun AndroidComposeUiTest<MainActivity>.runNewSafeOnboarding() {
        hasContentDescription(getString(OSString.signInScreen_accessibility_newSafe))
            .waitUntilExactlyOneExists()
            .performClick()
        hasTestTag(UiConstants.TestTag.Screen.CongratulationOnBoarding)
            .waitUntilExactlyOneExists()
        hasText(getString(OSString.multiSafeOnBoarding_presentation_button))
            .waitUntilExactlyOneExists()
            .performClick()
        onboardingPasswordCreation("b")
        hasTestTag(UiConstants.TestTag.Screen.CongratulationOnBoarding)
            .waitAndPrintRootToCacheDir(printRule, "_${UiConstants.TestTag.Screen.CongratulationOnBoarding}Screen")
            .assertIsDisplayed()
        hasText(getString(OSString.onBoarding_congratulationScreen_goButton))
            .waitUntilExactlyOneExists()
            .performClick()
        hasTestTag(UiConstants.TestTag.Screen.Login)
            .waitAndPrintRootToCacheDir(printRule, "_${UiConstants.TestTag.Screen.Home}Screen")
            .assertIsDisplayed()
        hasText(getString(OSString.signInScreen_welcome))
    }
}
