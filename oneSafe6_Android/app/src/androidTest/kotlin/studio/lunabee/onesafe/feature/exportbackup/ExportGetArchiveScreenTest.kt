package studio.lunabee.onesafe.feature.exportbackup

import androidx.compose.material3.SnackbarVisuals
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import studio.lunabee.compose.androidtest.LbcComposeTest
import studio.lunabee.compose.androidtest.extension.printToCacheDir
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.feature.exportbackup.getarchive.ExportGetArchiveRoute
import studio.lunabee.onesafe.feature.exportbackup.getarchive.ExportGetArchiveScreen
import studio.lunabee.onesafe.feature.exportbackup.getarchive.ExportGetArchiveUiState
import studio.lunabee.onesafe.feature.exportbackup.getarchive.ExportGetArchiveViewModel
import studio.lunabee.onesafe.ui.theme.OSUserTheme
import java.io.File

@OptIn(ExperimentalTestApi::class)
@HiltAndroidTest
class ExportGetArchiveScreenTest : LbcComposeTest() {

    @get:Rule(order = 0)
    val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    private val navigateBackToSettingsDestination: () -> Unit = spyk({ })
    private val navigateToExportAuthDestination: () -> Unit = spyk({ })
    private val reset: () -> Unit = spyk({ })
    private val showSnackBar: (visuals: SnackbarVisuals) -> Unit = spyk({ })
    private val shareFile: () -> Unit = spyk({ })
    private val saveFile: () -> Unit = spyk({ })

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun export_get_archive_restart_process_test() {
        val mockkViewModel: ExportGetArchiveViewModel = mockk()
        every { mockkViewModel.archiveFile } returns File("")
        every {
            mockkViewModel.exportGetArchiveState
        } returns MutableStateFlow(ExportGetArchiveUiState.RestartExport(reset)).asStateFlow()

        invoke {
            setContent {
                OSUserTheme(customPrimaryColor = null) {
                    ExportGetArchiveRoute(
                        navigateBackToSettingsDestination = navigateBackToSettingsDestination,
                        navigateToExportAuthDestination = navigateToExportAuthDestination,
                        showSnackBar = showSnackBar,
                        viewModel = mockkViewModel,
                    )
                }
            }

            onRoot().printToCacheDir(printRule)
        }

        verify(exactly = 1) { navigateToExportAuthDestination() }
        verify(exactly = 1) { reset() }
    }

    @Test
    fun export_get_archive_save_file_success_test() {
        val mockkViewModel: ExportGetArchiveViewModel = mockk()
        every { mockkViewModel.archiveFile } returns File("")
        every { mockkViewModel.exportGetArchiveState } returns MutableStateFlow(
            ExportGetArchiveUiState.Success(type = ExportGetArchiveUiState.Type.Save, reset),
        ).asStateFlow()

        invoke {
            setContent {
                OSUserTheme(customPrimaryColor = null) {
                    ExportGetArchiveRoute(
                        navigateBackToSettingsDestination = navigateBackToSettingsDestination,
                        navigateToExportAuthDestination = navigateToExportAuthDestination,
                        showSnackBar = showSnackBar,
                        viewModel = mockkViewModel,
                    )
                }
            }

            onRoot().printToCacheDir(printRule)

            verify(exactly = 1) { showSnackBar(any()) }
            verify(exactly = 1) { reset() }
        }
    }

    @Test
    fun export_end_button_visibility() {
        invoke {
            setContent {
                ExportGetArchiveScreen(
                    shareFile = shareFile,
                    saveFile = saveFile,
                    navigateBackToSettingsDestination = navigateBackToSettingsDestination,
                )
            }

            // Done button should not be displayed at this point.
            onNodeWithText(text = getString(OSString.backup_exportBackup_doneButton))
                .assertDoesNotExist()

            // Check action button state and perform click.
            hasText(text = getString(OSString.backup_exportBackup_saveButton))
                .waitUntilExactlyOneExists()
                .assertIsEnabled()
                .assertIsDisplayed()
                .performScrollTo()
                .performClick()

            // Check that save method is called after click.
            verify(exactly = 1) { saveFile() }

            // Check that done button is now displayed and redirects correctly to settings screen.
            hasText(text = getString(OSString.backup_exportBackup_doneButton))
                .waitUntilExactlyOneExists()
                .performScrollTo()
                .performClick()

            verify(exactly = 1) { navigateBackToSettingsDestination() }
        }
    }
}
