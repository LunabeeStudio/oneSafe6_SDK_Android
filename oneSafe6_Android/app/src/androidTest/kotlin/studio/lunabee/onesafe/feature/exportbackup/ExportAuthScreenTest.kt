package studio.lunabee.onesafe.feature.exportbackup

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Test
import studio.lunabee.compose.androidtest.LbcComposeTest
import studio.lunabee.compose.androidtest.extension.printToCacheDir
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.extension.markdownToAnnotatedString
import studio.lunabee.onesafe.domain.model.importexport.ExportMetadata
import studio.lunabee.onesafe.feature.exportbackup.auth.ExportAuthRoute
import studio.lunabee.onesafe.feature.exportbackup.auth.ExportAuthUiState
import studio.lunabee.onesafe.feature.exportbackup.auth.ExportAuthViewModel
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.theme.OSUserTheme

@OptIn(ExperimentalTestApi::class)
class ExportAuthScreenTest : LbcComposeTest() {
    private val navigateToExportDataDestination: (itemCount: Int, contactCount: Int, safeNav: Boolean) -> Unit = spyk({ _, _, _ -> })
    private val resetState: () -> Unit = spyk({ })
    private val navigateBack: () -> Unit = spyk({ })

    private val testPassword: String = "a"

    @Test
    fun wait_for_user_input_test() {
        val mockkViewModel: ExportAuthViewModel = mockk()
        every { mockkViewModel.exportMetadata } returns MutableStateFlow(
            ExportMetadata(itemCount = 10, contactCount = 10),
        ).asStateFlow()
        every { mockkViewModel.exportAuthState } returns MutableStateFlow(ExportAuthUiState.WaitForPassword)

        invoke {
            setContent {
                OSUserTheme(customPrimaryColor = null) {
                    ExportAuthRoute(
                        navigateBack = { },
                        viewModel = mockkViewModel,
                        navigateToExportDataDestination = navigateToExportDataDestination,
                    )
                }
            }

            onNodeWithText(
                getString(OSString.backup_protectBackup_withBubbles_description, 10, 10)
                    .markdownToAnnotatedString().text,
            ).assertIsDisplayed()
            onRoot().printToCacheDir(printRule)
        }

        verify(exactly = 0) { navigateToExportDataDestination(any(), any(), any()) }
        verify(exactly = 0) { resetState() }
    }

    @Test
    fun password_incorrect_test() {
        val mockkViewModel: ExportAuthViewModel = mockk()
        every { mockkViewModel.exportMetadata } returns MutableStateFlow(
            ExportMetadata(itemCount = 10, contactCount = 10),
        ).asStateFlow()
        every { mockkViewModel.exportAuthState } returns MutableStateFlow(ExportAuthUiState.PasswordIncorrect(resetState))

        invoke {
            setContent {
                OSUserTheme(customPrimaryColor = null) {
                    ExportAuthRoute(
                        navigateBack = { },
                        viewModel = mockkViewModel,
                        navigateToExportDataDestination = navigateToExportDataDestination,
                    )
                }
            }

            onNodeWithText(getString(OSString.backup_protectBackup_passwordCard_passwordErrorLabel))
                .performScrollTo()
                .assertIsDisplayed()
            onRoot()
                .printToCacheDir(printRule)
            onNodeWithText(getString(OSString.backup_protectBackup_passwordCard_passwordInputLabel))
                .performTextInput(testPassword)
        }

        verify(exactly = 0) { navigateToExportDataDestination(any(), any(), any()) }
        verify(exactly = 1) { resetState() }
    }

    @Test
    fun password_valid_test() {
        val mockkViewModel: ExportAuthViewModel = mockk()
        every { mockkViewModel.exportMetadata } returns MutableStateFlow(
            ExportMetadata(itemCount = 10, contactCount = 0),
        ).asStateFlow()
        every { mockkViewModel.exportAuthState } returns MutableStateFlow(ExportAuthUiState.PasswordValid(resetState))

        invoke {
            setContent {
                OSUserTheme(customPrimaryColor = null) {
                    ExportAuthRoute(
                        navigateBack = { },
                        viewModel = mockkViewModel,
                        navigateToExportDataDestination = navigateToExportDataDestination,
                    )
                }
            }

            onRoot().printToCacheDir(printRule)
        }

        verify(exactly = 1) { navigateToExportDataDestination(any(), any(), any()) }
        verify(exactly = 1) { resetState() }
    }

    @Test
    fun password_checking_test() {
        val mockkViewModel: ExportAuthViewModel = mockk()
        every { mockkViewModel.exportMetadata } returns MutableStateFlow(
            ExportMetadata(itemCount = 0, contactCount = 10),
        ).asStateFlow()
        every { mockkViewModel.exportAuthState } returns MutableStateFlow(ExportAuthUiState.CheckingPassword)

        invoke {
            setContent {
                OSUserTheme(customPrimaryColor = null) {
                    ExportAuthRoute(
                        navigateBack = { },
                        viewModel = mockkViewModel,
                        navigateToExportDataDestination = navigateToExportDataDestination,
                    )
                }
            }

            onNodeWithText(getString(OSString.common_next))
                .assertIsNotEnabled()
            onRoot()
                .printToCacheDir(printRule)
        }

        verify(exactly = 0) { navigateToExportDataDestination(any(), any(), any()) }
        verify(exactly = 0) { resetState() }
    }

    @Test
    fun export_empty_screen_test() {
        val mockkViewModel: ExportAuthViewModel = mockk()
        every { mockkViewModel.exportMetadata } returns MutableStateFlow(
            ExportMetadata(itemCount = 0, contactCount = 0),
        ).asStateFlow()
        every { mockkViewModel.exportAuthState } returns MutableStateFlow(ExportAuthUiState.WaitForPassword)

        invoke {
            setContent {
                OSUserTheme(customPrimaryColor = null) {
                    ExportAuthRoute(
                        navigateBack = navigateBack,
                        viewModel = mockkViewModel,
                        navigateToExportDataDestination = navigateToExportDataDestination,
                    )
                }
            }

            onNodeWithTag(UiConstants.TestTag.Screen.ExportEmptyScreen)
                .assertIsDisplayed()
            onRoot().printToCacheDir(printRule)
            onNodeWithText(getString(OSString.common_back))
                .assertIsDisplayed()
                .performClick()
        }

        verify(exactly = 1) { navigateBack() }
    }
}
