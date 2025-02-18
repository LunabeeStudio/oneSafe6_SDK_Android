package studio.lunabee.onesafe.navigation.settings

import android.Manifest
import android.os.Build
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.TouchInjectionScope
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeUp
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import studio.lunabee.compose.androidtest.extension.waitAndPrintRootToCacheDir
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.onesafe.OSMainActivityTest
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.domain.usecase.item.CreateItemUseCase
import studio.lunabee.onesafe.importexport.usecase.LocalAutoBackupUseCase
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.firstSafeId
import studio.lunabee.onesafe.test.test
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.UiConstants.TestTag.ScrollableContent.SettingsHorizontalPager
import javax.inject.Inject

@HiltAndroidTest
@OptIn(ExperimentalTestApi::class)
class SettingsNavigationTest : OSMainActivityTest() {

    @get:Rule(order = 0)
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)
    override val initialTestState: InitialTestState = InitialTestState.Home()

    @Inject lateinit var createItemUseCase: CreateItemUseCase

    @Inject lateinit var localAutoBackupUseCase: LocalAutoBackupUseCase

    companion object {
        @JvmStatic
        @BeforeClass
        fun notificationPermission() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val context = InstrumentationRegistry.getInstrumentation().targetContext
                InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(
                    context.packageName,
                    Manifest.permission.POST_NOTIFICATIONS,
                )
            }
        }

        context(OSMainActivityTest, ComposeUiTest)
        fun navToSettings() {
            val label = getString(OSString.accessibility_home_settings_button_clickLabel)
            hasContentDescription(label)
                .waitAndPrintRootToCacheDir(printRule, "_wait_settings_button")
                .assertIsDisplayed()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.Settings)
                .waitAndPrintRootToCacheDir(printRule, "_${UiConstants.TestTag.Screen.Settings}Screen")
                .assertIsDisplayed()
        }
    }

    // Biometric navigation test is tested in SettingsScreenTest with mocked data.
    @Test
    fun nav_to_settings_screen_test() {
        invoke {
            navToSettings()
        }
    }

    @Test
    fun nav_to_export_empty_screen_test() {
        invoke {
            navToSettings()
            hasText(getString(OSString.settings_backupCard_exportLabel))
                .waitUntilExactlyOneExists()
                .performScrollTo()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.ExportEmptyScreen)
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
        }
    }

    @Test
    fun nav_to_export_auth_screen_test() {
        invoke {
            runBlocking {
                createItemUseCase.test(name = "test")
            }
            navToSettings()
            hasText(getString(OSString.settings_backupCard_exportLabel))
                .waitUntilExactlyOneExists()
                .performScrollTo()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.ExportAuthScreen)
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
        }
    }

    @Test
    fun nav_to_import_select_screen_test() {
        invoke {
            navToSettings()
            hasText(getString(OSString.settings_backupCard_importLabel))
                .waitUntilExactlyOneExists()
                .performScrollTo()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.ImportFileScreen)
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
        }
    }

    @Test
    fun nav_to_about_and_credit_screen_test() {
        invoke {
            navToSettings()
            onNodeWithTag(SettingsHorizontalPager).performTouchInput(TouchInjectionScope::swipeLeft)
            val aboutTextMatcher = hasText(getString(OSString.settings_section_onesafe_aboutLabel))
            hasTestTag(UiConstants.TestTag.ScrollableContent.SettingsGlobalLazyColumn)
                .waitUntilExactlyOneExists()
                .performScrollToNode(aboutTextMatcher)

            aboutTextMatcher
                .waitUntilExactlyOneExists()
                .performClick()

            hasTestTag(UiConstants.TestTag.Screen.AboutScreen)
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()

            hasTestTag(UiConstants.TestTag.Item.AboutScreenList)
                .waitUntilExactlyOneExists()
                .performScrollToNode(hasText(getString(OSString.aboutScreen_creditCard_label)))

            hasText(getString(OSString.aboutScreen_creditCard_label))
                .waitUntilExactlyOneExists()
                .performClick()

            hasTestTag(UiConstants.TestTag.Screen.CreditsScreen)
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
        }
    }

    @Test
    fun manual_lock_settings_test() {
        invoke {
            val lockText = getString(OSString.settings_multiSafe_deleteSafe)
            navToSettings()
            onNodeWithTag(UiConstants.TestTag.ScrollableContent.SettingsHorizontalPager).performTouchInput(TouchInjectionScope::swipeUp)

            hasText(lockText)
                .waitUntilExactlyOneExists()
                .performClick()
        }
    }

    @Test
    fun nav_to_auto_backups_settings_test() {
        invoke {
            runBlocking {
                createItemUseCase.test(name = "test")
                localAutoBackupUseCase.invoke(firstSafeId).collect()
            }

            navToSettings()
            onNodeWithTag(SettingsHorizontalPager)
                .performScrollToNode(hasText(getString(OSString.settings_autoBackupScreen_title)))

            hasText(getString(OSString.settings_autoBackupScreen_title))
                .waitUntilExactlyOneExists()
                .performScrollTo()
                .performClick()

            hasTestTag(UiConstants.TestTag.Screen.AutoBackupSettingsScreen)
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()

            hasText(getString(OSString.settings_multiSafe_autoBackupScreen_allowAutoBackup_title))
                .waitUntilExactlyOneExists()
                .performScrollTo()
                .performClick()

            hasText(getString(OSString.settings_autoBackupScreen_informations_title))
                .waitUntilExactlyOneExists()
                .performScrollTo()
                .assertIsDisplayed()

            hasText(getString(OSString.settings_autoBackupScreen_restore_button))
                .waitUntilExactlyOneExists()
                .performScrollTo()
                .performClick()

            hasTestTag(UiConstants.TestTag.Screen.ImportFileScreen)
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
        }
    }
}
