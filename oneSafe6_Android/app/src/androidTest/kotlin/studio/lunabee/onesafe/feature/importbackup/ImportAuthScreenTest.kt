package studio.lunabee.onesafe.feature.importbackup

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
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
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.dialog.ErrorDialogState
import studio.lunabee.onesafe.domain.model.importexport.OSArchiveKind
import studio.lunabee.onesafe.error.OSImportExportError
import studio.lunabee.onesafe.feature.importbackup.auth.ImportAuthArchiveKindLabels
import studio.lunabee.onesafe.feature.importbackup.auth.ImportAuthRoute
import studio.lunabee.onesafe.feature.importbackup.auth.ImportAuthState
import studio.lunabee.onesafe.feature.importbackup.auth.ImportAuthViewModel
import studio.lunabee.onesafe.ui.theme.OSUserTheme
import java.time.LocalDateTime

@OptIn(ExperimentalTestApi::class)
@HiltAndroidTest
class ImportAuthScreenTest : LbcComposeTest() {

    @get:Rule(order = 0)
    val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    private val navigateToImportSaveDataDestination: (Boolean) -> Unit = spyk({ })
    private val resetState: () -> Unit = spyk({ })

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun wrong_credentials_test() {
        val mockkViewModel: ImportAuthViewModel = mockk()
        every { mockkViewModel.importAuthState } returns MutableStateFlow(ImportAuthState.WrongCredentials).asStateFlow()
        every { mockkViewModel.creationDate } returns LocalDateTime.now()
        every { mockkViewModel.archiveKind } returns OSArchiveKind.Backup
        every { mockkViewModel.importAuthArchiveKindLabels } returns ImportAuthArchiveKindLabels.Backup(LocalDateTime.now())

        invoke {
            setContent {
                OSUserTheme(customPrimaryColor = null) {
                    ImportAuthRoute(
                        navigateBack = { },
                        viewModel = mockkViewModel,
                        onSuccess = navigateToImportSaveDataDestination,
                    )
                }
            }

            onNodeWithText(getString(OSString.import_decryptImportCard_errorMessage))
                .assertIsDisplayed()
            onRoot().printToCacheDir(printRule)
        }

        verify(exactly = 0) { navigateToImportSaveDataDestination(any()) }
        verify(exactly = 0) { resetState() }
    }

    @Test
    fun loading_state_test() {
        val mockkViewModel: ImportAuthViewModel = mockk()
        every { mockkViewModel.importAuthState } returns MutableStateFlow(ImportAuthState.AuthInProgress).asStateFlow()
        every { mockkViewModel.creationDate } returns LocalDateTime.now()
        every { mockkViewModel.archiveKind } returns OSArchiveKind.Backup
        every { mockkViewModel.importAuthArchiveKindLabels } returns ImportAuthArchiveKindLabels.Backup(LocalDateTime.now())

        invoke {
            setContent {
                OSUserTheme(customPrimaryColor = null) {
                    ImportAuthRoute(
                        navigateBack = { },
                        viewModel = mockkViewModel,
                        onSuccess = { },
                    )
                }
            }
            onNodeWithText(getString(OSString.import_authentication_progress))
                .assertIsDisplayed()

            onNodeWithText(getString(OSString.common_next))
                .assertIsNotEnabled()

            onNodeWithText(getString(OSString.import_decryptImportCard_passwordFieldLabelOsPlus))
                .assertIsNotEnabled()
            onRoot()
                .printToCacheDir(printRule)
        }

        verify(exactly = 0) { navigateToImportSaveDataDestination(any()) }
        verify(exactly = 0) { resetState() }
    }

    @Test
    fun success_state_test() {
        val mockkViewModel: ImportAuthViewModel = mockk()
        every { mockkViewModel.importAuthState } returns MutableStateFlow(ImportAuthState.Success(resetState, false)).asStateFlow()
        every { mockkViewModel.creationDate } returns LocalDateTime.now()
        every { mockkViewModel.archiveKind } returns OSArchiveKind.Backup
        every { mockkViewModel.importAuthArchiveKindLabels } returns ImportAuthArchiveKindLabels.Backup(LocalDateTime.now())

        invoke {
            setContent {
                OSUserTheme(customPrimaryColor = null) {
                    ImportAuthRoute(
                        navigateBack = { },
                        viewModel = mockkViewModel,
                        onSuccess = navigateToImportSaveDataDestination,
                    )
                }
            }
        }
        verify(exactly = 1) { navigateToImportSaveDataDestination(false) }
        verify(exactly = 1) { resetState() }

        every { mockkViewModel.importAuthState } returns MutableStateFlow(ImportAuthState.Success(resetState, true)).asStateFlow()
        invoke {
            setContent {
                OSUserTheme(customPrimaryColor = null) {
                    ImportAuthRoute(
                        navigateBack = { },
                        viewModel = mockkViewModel,
                        onSuccess = navigateToImportSaveDataDestination,
                    )
                }
            }
        }
        verify(exactly = 1) { navigateToImportSaveDataDestination(false) }
        verify(exactly = 2) { resetState() }
    }

    @Test
    fun unexpected_error_test() {
        val error = OSImportExportError(OSImportExportError.Code.UNEXPECTED_ERROR)
        val dialogState = ErrorDialogState(
            error = error,
            actions = emptyList(),
            dismiss = { },
        )

        val mockkViewModel: ImportAuthViewModel = mockk()
        every {
            mockkViewModel.importAuthState
        } returns MutableStateFlow(ImportAuthState.UnexpectedError(dialogState)).asStateFlow()
        every { mockkViewModel.creationDate } returns LocalDateTime.now()
        every { mockkViewModel.archiveKind } returns OSArchiveKind.Backup
        every { mockkViewModel.importAuthArchiveKindLabels } returns ImportAuthArchiveKindLabels.Backup(LocalDateTime.now())

        invoke {
            setContent {
                OSUserTheme(customPrimaryColor = null) {
                    ImportAuthRoute(
                        navigateBack = { },
                        viewModel = mockkViewModel,
                        onSuccess = navigateToImportSaveDataDestination,
                    )
                }
            }

            onNodeWithText(getString(OSString.error_defaultTitle))
                .assertIsDisplayed()
                .printToCacheDir(printRule)
        }

        verify(exactly = 0) { navigateToImportSaveDataDestination(any()) }
        verify(exactly = 0) { resetState() }
    }
}
