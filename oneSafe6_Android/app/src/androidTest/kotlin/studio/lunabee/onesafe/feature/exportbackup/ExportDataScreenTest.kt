package studio.lunabee.onesafe.feature.exportbackup

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import com.lunabee.lbcore.model.LBFlowResult
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
import studio.lunabee.onesafe.commonui.OSPlurals
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.extension.markdownToAnnotatedString
import studio.lunabee.onesafe.feature.exportbackup.exportdata.ExportDataRoute
import studio.lunabee.onesafe.feature.exportbackup.exportdata.ExportDataViewModel
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.theme.OSUserTheme
import java.io.File

@OptIn(ExperimentalTestApi::class)
@HiltAndroidTest
class ExportDataScreenTest : LbcComposeTest() {

    @get:Rule(order = 0)
    val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    private val navigateToExportGetArchiveDestination: (filePath: String) -> Unit = spyk({ })

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun export_processing_test() {
        val mockkViewModel: ExportDataViewModel = mockk()
        every { mockkViewModel.exportDataState } returns MutableStateFlow(LBFlowResult.Loading<File>()).asStateFlow()

        invoke {
            setContent {
                OSUserTheme(customPrimaryColor = null) {
                    ExportDataRoute(
                        navigateBack = { },
                        viewModel = mockkViewModel,
                        navigateToExportGetArchiveDestination = navigateToExportGetArchiveDestination,
                        itemCount = 100,
                        contactCount = 10,
                    )
                }
            }

            onNodeWithText(
                getString(
                    OSString.export_progressCard_withBubbles_description,
                    getQuantityString(OSPlurals.export_progressCard_withBubbles_itemDescription, 100, 100),
                    getQuantityString(OSPlurals.export_progressCard_withBubbles_bubblesDescription, 10, 10),
                ).markdownToAnnotatedString().text,
            ).assertIsDisplayed()
            onNodeWithTag(UiConstants.TestTag.Item.LinearProgressItem)
                .assertIsDisplayed()
            onRoot().printToCacheDir(printRule)
        }

        verify(exactly = 0) { navigateToExportGetArchiveDestination(any()) }
    }

    @Test
    fun export_error_test() {
        val mockkViewModel: ExportDataViewModel = mockk()
        every { mockkViewModel.exportDataState } returns MutableStateFlow(LBFlowResult.Failure<File>()).asStateFlow()

        invoke {
            setContent {
                OSUserTheme(customPrimaryColor = null) {
                    ExportDataRoute(
                        navigateBack = { },
                        viewModel = mockkViewModel,
                        navigateToExportGetArchiveDestination = navigateToExportGetArchiveDestination,
                        itemCount = 100,
                        contactCount = 10,
                    )
                }
            }

            onNodeWithText(getString(OSString.error_defaultMessage))
                .assertIsDisplayed()
            onNodeWithTag(UiConstants.TestTag.Item.LinearProgressItem)
                .assertIsDisplayed()
            onRoot()
                .printToCacheDir(printRule)
        }

        verify(exactly = 0) { navigateToExportGetArchiveDestination(any()) }
    }

    @Test
    fun export_success_test() {
        val mockkViewModel: ExportDataViewModel = mockk()
        every { mockkViewModel.exportDataState } returns MutableStateFlow(LBFlowResult.Success(File(""))).asStateFlow()

        invoke {
            setContent {
                OSUserTheme(customPrimaryColor = null) {
                    ExportDataRoute(
                        navigateBack = { },
                        viewModel = mockkViewModel,
                        navigateToExportGetArchiveDestination = navigateToExportGetArchiveDestination,
                        itemCount = 100,
                        contactCount = 10,
                    )
                }
            }

            onRoot().printToCacheDir(printRule)
        }

        verify(exactly = 1) { navigateToExportGetArchiveDestination(any()) }
    }
}
