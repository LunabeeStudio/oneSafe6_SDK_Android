package studio.lunabee.onesafe.feature.importbackup

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.feature.dialog.WrongExtensionDialogState
import studio.lunabee.onesafe.feature.importbackup.selectfile.ImportFileRoute
import studio.lunabee.onesafe.feature.importbackup.selectfile.ImportFileScreen
import studio.lunabee.onesafe.feature.importbackup.selectfile.ImportFileViewModel
import studio.lunabee.onesafe.feature.importbackup.selectfile.MetadataReadState
import studio.lunabee.onesafe.ui.UiConstants

@OptIn(ExperimentalTestApi::class)
@HiltAndroidTest
class ImportFileScreenTest : LbcComposeTest() {

    @get:Rule(order = 0)
    val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    private val navigateBack: () -> Unit = spyk({})
    private val navigateToImportAuthDestination: () -> Unit = spyk({})
    private val navigateToWarningNotFullySupportedArchive: () -> Unit = spyk({})
    private val dismiss: () -> Unit = spyk({})
    private val retry: () -> Unit = spyk({})
    private val pickFile: () -> Unit = spyk({})

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun wrong_file_selected_retry_test() {
        val mockkViewModel = mockk<ImportFileViewModel>().apply {
            every { importMetadataState } returns MutableStateFlow(
                value = MetadataReadState.Error(
                    dialogState = WrongExtensionDialogState(
                        retry = retry,
                        dismiss = dismiss,
                    ),
                ),
            ).asStateFlow()
        }

        invoke {
            setContent {
                ImportFileRoute(
                    navigateBack = navigateBack,
                    navigateToImportAuthDestination = navigateToImportAuthDestination,
                    viewModel = mockkViewModel,
                    navigateToWarningNotFullySupportedArchive = navigateToWarningNotFullySupportedArchive,
                )
            }

            onNode(isDialog())
                .assertIsDisplayed()

            onNodeWithText(getString(OSString.import_selectFile_error_wrongFileSelected))
                .assertIsDisplayed()

            onNodeWithText(getString(OSString.import_selectFile_error_selectAnotherOne))
                .assertIsDisplayed()
                .performClick()

            verify(exactly = 1) { retry() }
        }
    }

    @Test
    fun wrong_file_selected_dismiss_test() {
        val mockkViewModel = mockk<ImportFileViewModel>().apply {
            every { importMetadataState } returns MutableStateFlow(
                value = MetadataReadState.Error(
                    dialogState = WrongExtensionDialogState(
                        retry = retry,
                        dismiss = dismiss,
                    ),
                ),
            ).asStateFlow()
        }

        invoke {
            setContent {
                ImportFileRoute(
                    navigateBack = navigateBack,
                    navigateToImportAuthDestination = navigateToImportAuthDestination,
                    viewModel = mockkViewModel,
                    navigateToWarningNotFullySupportedArchive = navigateToWarningNotFullySupportedArchive,
                )
            }

            onNode(isDialog())
                .assertIsDisplayed()

            onNodeWithText(text = getString(OSString.import_selectFile_error_wrongFileSelected))
                .assertIsDisplayed()

            onNodeWithText(text = getString(OSString.common_cancel))
                .assertIsDisplayed()
                .performClick()

            verify(exactly = 1) { dismiss() }
        }
    }

    @Test
    fun pick_file_test() {
        invoke {
            setContent {
                ImportFileScreen(
                    pickFile = pickFile,
                    navigateBack = navigateBack,
                    extractProgress = null,
                )
            }

            onNodeWithText(text = getString(OSString.import_selectFile_button))
                .assertIsDisplayed()
                .performClick()

            verify(exactly = 1) { pickFile() }
        }
    }

    @Test
    fun unzipping_file_test() {
        invoke {
            setContent {
                ImportFileScreen(
                    pickFile = pickFile,
                    navigateBack = navigateBack,
                    extractProgress = .5f,
                )
            }

            onNodeWithText(text = getString(OSString.import_selectFile_button))
                .assertIsNotEnabled()

            onNodeWithContentDescription(label = getString(OSString.common_accessibility_back))
                .assertIsNotEnabled()

            onNodeWithTag(testTag = UiConstants.TestTag.Item.LinearProgressItem, useUnmergedTree = true)
                .assertIsDisplayed()
        }
    }
}
