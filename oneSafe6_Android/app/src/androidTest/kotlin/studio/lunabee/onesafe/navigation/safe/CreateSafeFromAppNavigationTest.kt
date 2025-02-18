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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.compose.androidtest.extension.waitUntilDoesNotExist
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.onesafe.MainActivity
import studio.lunabee.onesafe.OSMainActivityTest
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.domain.usecase.item.CreateItemUseCase
import studio.lunabee.onesafe.navigation.onboardingPasswordCreation
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.StaticIdProvider
import studio.lunabee.onesafe.test.assertThrows
import studio.lunabee.onesafe.test.test
import studio.lunabee.onesafe.test.testUUIDs
import studio.lunabee.onesafe.ui.UiConstants
import javax.inject.Inject

@HiltAndroidTest
@OptIn(ExperimentalTestApi::class)
class CreateSafeFromAppNavigationTest : OSMainActivityTest() {

    @get:Rule(order = 0)
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.Home()

    private val newSafePassword = "b"

    @Inject
    lateinit var createItemUseCase: CreateItemUseCase

    @Test
    fun new_safe_from_app_navigation_test(): TestResult = runTest {
        createItemUseCase.test("Item A")
        invoke {
            hasText("Item A")
                .waitUntilExactlyOneExists()
            runNewSafeOnboarding(newSafePassword)
            runBlocking { safeRepository.currentSafeIdFlow().first { it == null } }
            login(newSafePassword)
            hasTestTag(UiConstants.TestTag.Screen.Home)
                .waitUntilExactlyOneExists()
            runBlocking { createItemUseCase.test("Item B") }
            hasText("Item B")
                .waitUntilExactlyOneExists()
            hasText("Item A")
                .waitUntilDoesNotExist()
        }
    }

    @Test
    fun new_safe_from_app_navigation_exit_on_back_test() {
        invoke {
            runNewSafeOnboarding(newSafePassword)
            runCatching {
                keyboardHelper.waitForKeyboardVisibility(true, timeout = 2_000)
                Espresso.closeSoftKeyboard()
            }
            assertThrows<NoActivityResumedException> {
                Espresso.pressBack()
            }
        }
    }

    @Test
    fun new_safe_from_app_navigation_re_open_first_safe_test() {
        invoke {
            runNewSafeOnboarding(newSafePassword)
            runBlocking { safeRepository.currentSafeIdFlow().first { it == null } }
            login()
            hasTestTag(UiConstants.TestTag.Screen.Settings)
                .waitUntilExactlyOneExists()
        }
    }

    private fun AndroidComposeUiTest<MainActivity>.runNewSafeOnboarding(password: String) {
        StaticIdProvider.id = testUUIDs[1]
        hasContentDescription(getString(OSString.accessibility_home_settings_button_clickLabel))
            .waitUntilExactlyOneExists()
            .performClick()
        hasText(getString(OSString.settings_tab_app))
            .waitUntilExactlyOneExists()
            .performClick()
        hasText(getString(OSString.settings_multiSafe_newSafe))
            .waitUntilExactlyOneExists()
            .performClick()
        hasText(getString(OSString.multiSafeOnBoarding_presentation_button))
            .waitUntilExactlyOneExists()
            .performClick()
        onboardingPasswordCreation(password)
        hasTestTag(UiConstants.TestTag.Screen.CongratulationOnBoarding)
            .waitUntilExactlyOneExists()
            .assertIsDisplayed()
        hasText(getString(OSString.onBoarding_congratulationScreen_goButton))
            .waitUntilExactlyOneExists()
            .performClick()
        hasTestTag(UiConstants.TestTag.Screen.Login)
            .waitUntilExactlyOneExists()
            .assertIsDisplayed()
        hasText(getString(OSString.signInScreen_welcome))
            .waitUntilExactlyOneExists()
    }
}
