package studio.lunabee.onesafe.navigation

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.ui.test.AndroidComposeUiTestEnvironment
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.core.content.ContextCompat
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso
import com.lunabee.lbcore.model.LBFlowResult
import com.lunabee.lbcore.model.LBResult
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import studio.lunabee.compose.androidtest.extension.waitAndPrintRootToCacheDir
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.BuildConfig
import studio.lunabee.onesafe.MainActivity
import studio.lunabee.onesafe.OSMainActivityTest
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.extension.setIsTest
import studio.lunabee.onesafe.domain.usecase.item.CreateItemUseCase
import studio.lunabee.onesafe.feature.migration.MigrationManager
import studio.lunabee.onesafe.hasExcludeSearch
import studio.lunabee.onesafe.importexport.usecase.ImportSaveDataUseCase
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.test
import studio.lunabee.onesafe.test.testUUIDs
import studio.lunabee.onesafe.ui.UiConstants
import java.util.UUID
import javax.inject.Inject

@HiltAndroidTest
class MigrationNavigationTest : OSMainActivityTest() {
    @get:Rule(order = 0)
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @Inject lateinit var createItemUseCase: CreateItemUseCase
    val itemName: String = "test"

    override val initialTestState: InitialTestState = InitialTestState.SignedUp {
        createItemUseCase.test(name = itemName)
    }

    @BindValue val importSaveDataUseCase: ImportSaveDataUseCase = mockk {
        every { this@mockk.invoke(any(), any()) } returns flowOf(LBFlowResult.Success(UUID.randomUUID()))
    }

    @BindValue val migrationManager: MigrationManager = mockk()

    private val migrationIntent: Intent
        get() {
            val uri = Uri.Builder()
                .scheme("content")
                .authority(AppConstants.Migration.OldOneSafePackage + ".migrationprovider")
                .path("migration/migration")
                .build()
            return Intent(AppConstants.Migration.NewOneSafeMigrationIntentAction, uri)
                .setPackage(BuildConfig.APPLICATION_ID)
                .setIsTest()
        }

    @Before
    fun setup() {
        // Bypass migration caller check
        mockkObject(MigrationManager.Companion)
        every { MigrationManager.isAllowedMigrationIntent(any(), any()) } returns true

        // Bypass permission request
        mockkStatic(ContextCompat::checkSelfPermission)
        every {
            ContextCompat.checkSelfPermission(any(), AppConstants.Migration.OldOneSafeServicePermission)
        } returns PackageManager.PERMISSION_GRANTED

        // Mock service binding
        every { migrationManager.initMigration(any()) } answers {
            @Suppress("UNCHECKED_CAST")
            (args[0] as (result: LBResult<Unit>) -> Unit).invoke(LBResult.Success(Unit))
        }
        every { migrationManager.getMigrationFlow(any(), any()) } returns flowOf(LBFlowResult.Success(Unit))
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun migration_flow_from_login_test() {
        AndroidComposeUiTestEnvironment {
            activity
        }.runTest {
            val scenario = launchActivity<MainActivity>(migrationIntent)
            scenario.onActivity { this@MigrationNavigationTest.activity = it }
            initKeyboardHelper()
            login()

            hasTestTag(UiConstants.TestTag.Screen.ImportSaveDataScreen)
                .waitAndPrintRootToCacheDir(printRule)
                .assertIsDisplayed()

            hasText(getString(OSString.importSettings_card_addButton))
                .waitAndPrintRootToCacheDir(printRule)
                .performScrollTo()
                .performClick()

            hasTestTag(UiConstants.TestTag.Screen.Home)
                .waitAndPrintRootToCacheDir(printRule)
                .assertIsDisplayed()

            logout() // avoid crash due to setting flow collection after test end
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun migration_flow_back_test() {
        AndroidComposeUiTestEnvironment {
            activity
        }.runTest {
            val scenario = launchActivity<MainActivity>(migrationIntent)
            scenario.onActivity { this@MigrationNavigationTest.activity = it }
            initKeyboardHelper()
            login()

            hasTestTag(UiConstants.TestTag.Screen.ImportSaveDataScreen)
                .waitAndPrintRootToCacheDir(printRule)
                .assertIsDisplayed()

            Espresso.pressBack()

            hasTestTag(UiConstants.TestTag.Screen.Home)
                .waitAndPrintRootToCacheDir(printRule)
                .assertIsDisplayed()

            logout() // avoid crash due to setting flow collection after test end
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun migration_flow_from_item_test() {
        invoke {
            login()
            // Nav to item
            hasExcludeSearch(hasText(itemName))
                .waitUntilExactlyOneExists()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.itemDetailsScreen(testUUIDs[0]))
                .waitAndPrintRootToCacheDir(printRule, "_itemDetails_screen")

            // Send migration intent
            this@MigrationNavigationTest.activity.startActivity(migrationIntent)

            hasText(getString(OSString.importSettings_card_addButton))
                .waitAndPrintRootToCacheDir(printRule)
                .performScrollTo()
                .performClick()

            hasTestTag(UiConstants.TestTag.Screen.Home)
                .waitAndPrintRootToCacheDir(printRule)
                .assertIsDisplayed()
        }
    }
}
