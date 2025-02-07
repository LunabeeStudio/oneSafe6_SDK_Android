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
 * Created by Lunabee Studio / Date - 9/30/2024 - for the oneSafe6 SDK.
 * Last modified 30/09/2024 11:40
 */

package studio.lunabee.onesafe.navigation.panicdestruction

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.espresso.Espresso
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import studio.lunabee.compose.androidtest.extension.waitUntilDoesNotExist
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.onesafe.OSMainActivityTest
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.extension.markdownToAnnotatedString
import studio.lunabee.onesafe.domain.usecase.UpdatePanicButtonWidgetUseCase
import studio.lunabee.onesafe.domain.usecase.panicmode.AddPanicWidgetToHomeScreenUseCase
import studio.lunabee.onesafe.domain.usecase.panicmode.IsPanicDestructionEnabledUseCase
import studio.lunabee.onesafe.domain.usecase.panicmode.IsPanicWidgetInstalledUseCase
import studio.lunabee.onesafe.importexport.usecase.GetAutoBackupSettingUseCase
import studio.lunabee.onesafe.navigation.settings.SettingsNavigationTest
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.widget.panic.di.WidgetModule

@OptIn(ExperimentalTestApi::class)
@HiltAndroidTest
@UninstallModules(WidgetModule::class)
class SetupPanicDestructionNavigationTest : OSMainActivityTest() {
    @get:Rule(order = 0)
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.Home {}

    private val cloudBackupEnabled = MutableStateFlow(false)

    @BindValue
    val getAutoBackupSettingUseCase: GetAutoBackupSettingUseCase = mockk {
        every { cloudBackupEnabled(any()) } returns cloudBackupEnabled
    }

    @BindValue
    val updatePanicButtonWidgetUseCase: UpdatePanicButtonWidgetUseCase = mockk<UpdatePanicButtonWidgetUseCase> {
        coEvery { this@mockk.invoke() } returns Unit
    }

    @BindValue
    val isPanicWidgetInstalledUseCase: IsPanicWidgetInstalledUseCase = mockk<IsPanicWidgetInstalledUseCase> {
        coEvery { this@mockk.invoke() } returns false
    }

    @BindValue
    val addPanicWidgetToHomeScreenUseCase: AddPanicWidgetToHomeScreenUseCase = mockk<AddPanicWidgetToHomeScreenUseCase> {
        coEvery { this@mockk.invoke() } returns Unit
        coEvery { this@mockk.isSupported() } returns true
    }

    private val isPanicDestructionEnabled: MutableStateFlow<Boolean> = MutableStateFlow(false)

    @BindValue
    val isPanicDestructionEnabledUseCase: IsPanicDestructionEnabledUseCase = mockk<IsPanicDestructionEnabledUseCase> {
        every { this@mockk.invoke() } returns isPanicDestructionEnabled
    }

    @Test
    fun display_warning_auto_backup_test(): Unit = invoke {
        SettingsNavigationTest.navToSettings()
        hasText(getString(OSString.settings_panicDestruction_disabled))
            .waitUntilExactlyOneExists()
            .performScrollTo()
            .performClick()
        hasText(getString(OSString.panicdestruction_settings_warning).markdownToAnnotatedString().text)
            .waitUntilExactlyOneExists()
        Espresso.pressBack()
        cloudBackupEnabled.value = true
        hasText(getString(OSString.settings_panicDestruction_disabled))
            .waitUntilExactlyOneExists()
            .performScrollTo()
            .performClick()
        hasTestTag(UiConstants.TestTag.Screen.WidgetPanicModeSettingsScreen)
            .waitUntilExactlyOneExists()
            .assertIsDisplayed()
        hasText(getString(OSString.panicdestruction_settings_warning).markdownToAnnotatedString().text)
            .waitUntilDoesNotExist()
            .assertDoesNotExist()
    }

    @Test
    fun settings_screen_state_disabled_test(): Unit = invoke {
        coEvery { isPanicWidgetInstalledUseCase.invoke() } returns false
        SettingsNavigationTest.navToSettings()
        hasText(getString(OSString.settings_panicDestruction_disabled))
            .waitUntilExactlyOneExists()
            .assertIsDisplayed()
            .performClick()
        hasText(getString(OSString.panicdestruction_settings_description_disabled))
            .waitUntilExactlyOneExists()
            .assertIsDisplayed()
    }

    @Test
    fun settings_screen_state_disabled_not_supported(): Unit = invoke {
        coEvery { addPanicWidgetToHomeScreenUseCase.isSupported() } returns false
        coEvery { isPanicWidgetInstalledUseCase.invoke() } returns false
        SettingsNavigationTest.navToSettings()
        hasText(getString(OSString.settings_panicDestruction_disabled))
            .waitUntilExactlyOneExists()
            .assertIsDisplayed()
            .performClick()
        hasText(getString(OSString.panicdestruction_settings_destruction_installedAndDisabled))
            .waitUntilExactlyOneExists()
            .assertIsDisplayed()
    }

    @Test
    fun settings_screen_state_installed_test(): Unit = invoke {
        coEvery { isPanicWidgetInstalledUseCase.invoke() } returns true
        isPanicDestructionEnabled.value = false
        SettingsNavigationTest.navToSettings()
        hasText(getString(OSString.settings_panicDestruction_installedAndDisabled))
            .waitUntilExactlyOneExists()
            .performScrollTo()
            .performClick()
        hasText(getString(OSString.panicdestruction_settings_destruction_installedAndDisabled))
            .waitUntilExactlyOneExists()
            .assertIsDisplayed()
        isPanicDestructionEnabled.value = true
        hasText(getString(OSString.panicdestruction_settings_description_enabled))
            .waitUntilExactlyOneExists()
            .assertIsDisplayed()
    }
}
