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
 * Created by Lunabee Studio / Date - 9/12/2024 - for the oneSafe6 SDK.
 * Last modified 12/09/2024 10:47
 */

package studio.lunabee.onesafe.navigation.autodestruction

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import studio.lunabee.compose.androidtest.extension.waitAndPrintRootToCacheDir
import studio.lunabee.compose.androidtest.extension.waitUntilDoesNotExist
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.onesafe.OSMainActivityTest
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.extension.markdownToAnnotatedString
import studio.lunabee.onesafe.di.Inject
import studio.lunabee.onesafe.domain.usecase.autodestruction.EnableAutoDestructionUseCase
import studio.lunabee.onesafe.importexport.usecase.GetAutoBackupSettingUseCase
import studio.lunabee.onesafe.navigation.settings.SettingsNavigationTest
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.ui.UiConstants

@OptIn(ExperimentalTestApi::class)
@HiltAndroidTest
class SetupAutoDestructionNavigationTest : OSMainActivityTest() {
    @get:Rule(order = 0)
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.Home {}

    private val cloudBackupEnabled = MutableStateFlow(false)

    @BindValue
    val getAutoBackupSettingUseCase: GetAutoBackupSettingUseCase = mockk {
        every { cloudBackupEnabled(any()) } returns cloudBackupEnabled
    }

    @Inject
    lateinit var enableAutoDestructionUseCase: EnableAutoDestructionUseCase

    @Test
    fun display_warning_auto_backup_test(): Unit = invoke {
        SettingsNavigationTest.navToSettings()
        hasText(getString(OSString.settings_section_security_option_label))
            .waitUntilExactlyOneExists()
            .performClick()
        hasText(getString(OSString.settings_security_section_autodestruction))
            .waitUntilExactlyOneExists()
            .performClick()
        hasText(getString(OSString.autodestruction_onBoarding_warning).markdownToAnnotatedString().text)
            .waitUntilExactlyOneExists()
        Espresso.pressBack()
        cloudBackupEnabled.value = true
        hasText(getString(OSString.settings_security_section_autodestruction))
            .waitUntilExactlyOneExists()
            .performClick()
        hasTestTag(UiConstants.TestTag.Screen.AutoDestructionOnBoardingScreen)
            .waitUntilExactlyOneExists()
            .assertIsDisplayed()
        hasText(getString(OSString.autodestruction_onBoarding_warning).markdownToAnnotatedString().text)
            .waitUntilDoesNotExist()
            .assertDoesNotExist()
    }

    @Test
    fun on_boarding_screen_state_test(): Unit = invoke {
        runBlocking { enableAutoDestructionUseCase(charArrayOf('b')) }
        SettingsNavigationTest.navToSettings()
        hasText(getString(OSString.settings_section_security_option_label))
            .waitUntilExactlyOneExists()
            .performClick()
        hasText(getString(OSString.settings_security_section_autodestruction_enabled))
            .waitUntilExactlyOneExists()
            .performClick()
        hasText(getString(OSString.autodestruction_onBoarding_enabled_action))
            .waitUntilExactlyOneExists()
            .performScrollTo()
            .performClick()
        hasText(getString(OSString.settings_security_section_autodestruction))
            .waitUntilExactlyOneExists()
            .performClick()
        hasText(getString(OSString.autodestruction_onBoarding_disabled_description))
            .waitUntilExactlyOneExists()
            .performClick()
    }

    @Test
    fun full_setup_auto_destruction_test(): Unit = invoke {
        SettingsNavigationTest.navToSettings()
        hasText(getString(OSString.settings_section_security_option_label))
            .waitUntilExactlyOneExists()
            .performClick()
        hasText(getString(OSString.settings_security_section_autodestruction))
            .waitUntilExactlyOneExists()
            .performClick()
        hasText(getString(OSString.autodestruction_onBoarding_disabled_action))
            .waitUntilExactlyOneExists()
            .performScrollTo()
            .performClick()
        hasTestTag(UiConstants.TestTag.Screen.PasswordCreation)
            .waitAndPrintRootToCacheDir(printRule)
        hasText(getString(OSString.fieldName_password))
            .waitUntilExactlyOneExists()
            .performTextInput("a") // the old user's password
        hasText(getString(OSString.common_confirm))
            .waitUntilExactlyOneExists()
            .performScrollTo()
            .performClick()
        hasText(getString(OSString.changePassword_error_passwordAlreadyUsed))
            .waitUntilExactlyOneExists()
            .assertIsDisplayed()
        onNodeWithTag(UiConstants.TestTag.Item.PasswordCreationTextField)
            .performTextClearance()
        onNodeWithTag(UiConstants.TestTag.Item.PasswordCreationTextField)
            .performTextInput("z")
        hasText(getString(OSString.common_confirm))
            .waitUntilExactlyOneExists()
            .performScrollTo()
            .performClick()
        hasTestTag(UiConstants.TestTag.Screen.PasswordConfirmation)
            .waitUntilExactlyOneExists()
        hasText(getString(OSString.fieldName_password))
            .waitUntilExactlyOneExists()
            .performTextInput("z")
        hasText(getString(OSString.common_confirm))
            .waitUntilExactlyOneExists()
            .performScrollTo()
            .performClick()
        hasText(getString(OSString.autodestruction_success))
            .waitUntilExactlyOneExists()
            .assertIsDisplayed()
    }
}
