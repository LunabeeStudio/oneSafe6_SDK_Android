package studio.lunabee.onesafe.feature.importbackup

import androidx.compose.material3.SnackbarVisuals
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import com.lunabee.lbcore.model.LBFlowResult
import com.lunabee.lbcore.model.LBResult
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import studio.lunabee.compose.androidtest.LbcComposeTest
import studio.lunabee.compose.androidtest.extension.printToCacheDir
import studio.lunabee.onesafe.commonui.OSPlurals
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.extension.markdownToAnnotatedString
import studio.lunabee.onesafe.domain.model.importexport.ImportMetadata
import studio.lunabee.onesafe.domain.model.importexport.ImportMode
import studio.lunabee.onesafe.domain.model.importexport.OSArchiveKind
import studio.lunabee.onesafe.feature.importbackup.savedata.ImportSaveDataRoute
import studio.lunabee.onesafe.feature.importbackup.savedata.ImportSaveDataUiState
import studio.lunabee.onesafe.feature.importbackup.savedata.ImportSaveDataViewModel
import studio.lunabee.onesafe.feature.importbackup.savedelegate.ImportSaveDataDelegateImpl
import studio.lunabee.onesafe.importexport.usecase.ImportSaveDataUseCase
import studio.lunabee.onesafe.ui.UiConstants.TestTag.Screen.ImportSharingScreen
import studio.lunabee.onesafe.ui.theme.OSUserTheme
import java.io.File
import java.time.Instant
import java.util.UUID

@OptIn(ExperimentalTestApi::class)
@HiltAndroidTest
class ImportSaveDataScreenTest : LbcComposeTest() {

    @get:Rule(order = 0)
    val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    private val navigateBackToSettings: () -> Unit = spyk({ })
    private val navigateBackToFileSelection: () -> Unit = spyk({})
    private val showSnackBar: (SnackbarVisuals) -> Unit = spyk({})

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    private val importSaveDataUseCase: ImportSaveDataUseCase = mockk {
        every { this@mockk.invoke(any(), ImportMode.Append) } returns flowOf(LBFlowResult.Success(UUID.randomUUID()))
        every { this@mockk.invoke(any(), ImportMode.Replace) } returns flowOf(LBFlowResult.Success(UUID.randomUUID()))
    }

    @Test
    fun filled_os_item_count_waiting_for_user_test() {
        val itemCount = 120
        val initialMockkViewModel = getInitialViewModel(itemCount)

        val mockkViewModel = spyk(initialMockkViewModel) {
            every { metadataResult } returns LBResult.Success(
                ImportMetadata(
                    OSArchiveKind.Backup,
                    false,
                    itemCount,
                    0,
                    "Android - Test",
                    1,
                    Instant.now(),
                ),
            )

            every { importSaveDataState } returns MutableStateFlow(value = ImportSaveDataUiState.WaitingForUserChoice)
            every { archiveKind } returns OSArchiveKind.Backup
        }

        invoke {
            setContent {
                OSUserTheme(customPrimaryColor = null) {
                    ImportSaveDataRoute(
                        viewModel = mockkViewModel,
                        navigateBackToSettings = navigateBackToSettings,
                        navigateBackToFileSelection = navigateBackToFileSelection,
                    )
                }
            }

            onNodeWithText(
                getQuantityString(OSPlurals.import_settings_description, itemCount, itemCount).markdownToAnnotatedString().text,
            ).assertIsDisplayed()
            onNodeWithText(getString(OSString.importSettings_card_addButton))
                .assertIsDisplayed()
                .assertIsEnabled()
            onNodeWithText(getString(OSString.importSettings_card_overrideButton))
                .assertIsDisplayed()
                .assertIsEnabled()
            onRoot().printToCacheDir(printRule)
        }

        verify(exactly = 0) { navigateBackToSettings() }
    }

    @Test
    fun empty_os_item_count_waiting_for_user_test() {
        val itemCount = 120
        val initialMockkViewModel = getInitialViewModel(itemCount, 0)

        val mockkViewModel = spyk(initialMockkViewModel) {
            every { metadataResult } returns LBResult.Success(
                ImportMetadata(
                    OSArchiveKind.Backup,
                    false,
                    itemCount,
                    0,
                    "Android - Test",
                    1,
                    Instant.now(),
                ),
            )

            every { importSaveDataState } returns MutableStateFlow(value = ImportSaveDataUiState.WaitingForUserChoice)
            every { archiveKind } returns OSArchiveKind.Backup
        }

        invoke {
            setContent {
                OSUserTheme(customPrimaryColor = null) {
                    ImportSaveDataRoute(
                        viewModel = mockkViewModel,
                        navigateBackToSettings = navigateBackToSettings,
                        navigateBackToFileSelection = navigateBackToFileSelection,
                    )
                }
            }

            onNodeWithText(
                getQuantityString(OSPlurals.import_settings_description, itemCount, itemCount).markdownToAnnotatedString().text,
            ).assertIsDisplayed()
            onNodeWithText(getString(OSString.importSettings_card_home))
                .assertIsDisplayed()
                .assertIsEnabled()
            onNodeWithText(getString(OSString.importSettings_card_addButton))
                .assertIsDisplayed()
                .assertIsEnabled()
            onRoot().printToCacheDir(printRule)
        }

        verify(exactly = 0) { navigateBackToSettings() }
    }

    @Test
    fun append_action_test() {
        val initialMockkViewModel = getInitialViewModel(0)

        val mockkViewModel: ImportSaveDataViewModel = spyk(initialMockkViewModel) {
            every { metadataResult } returns LBResult.Success(
                ImportMetadata(
                    OSArchiveKind.Backup,
                    false,
                    0,
                    0,
                    "Android - Test",
                    1,
                    Instant.now(),
                ),
            )
            every { importSaveDataState } returns MutableStateFlow(value = ImportSaveDataUiState.WaitingForUserChoice)
            every { startImport(ImportMode.AppendInFolder) } returns Unit
            every { archiveKind } returns OSArchiveKind.Backup
        }

        invoke {
            setContent {
                OSUserTheme(customPrimaryColor = null) {
                    ImportSaveDataRoute(
                        viewModel = mockkViewModel,
                        navigateBackToSettings = navigateBackToSettings,
                        navigateBackToFileSelection = navigateBackToFileSelection,
                    )
                }
            }

            onNodeWithText(getString(OSString.importSettings_card_addButton))
                .assertIsDisplayed()
                .assertIsEnabled()
                .performClick()
        }

        verify(exactly = 0) { navigateBackToSettings() }
        verify(exactly = 1) { mockkViewModel.startImport(mode = ImportMode.AppendInFolder) }
    }

    @Test
    fun replace_action_test() {
        val initialMockkViewModel = getInitialViewModel(0)
        val mockkViewModel: ImportSaveDataViewModel = spyk(initialMockkViewModel) {
            every { metadataResult } returns LBResult.Success(
                ImportMetadata(
                    OSArchiveKind.Backup,
                    false,
                    0,
                    0,
                    "Android - Test",
                    1,
                    Instant.now(),
                ),
            )
            every { importSaveDataState } returns MutableStateFlow(value = ImportSaveDataUiState.WaitingForUserChoice)
            every { startImport(ImportMode.Replace) } returns Unit
            every { archiveKind } returns OSArchiveKind.Backup
        }

        invoke {
            setContent {
                OSUserTheme(customPrimaryColor = null) {
                    ImportSaveDataRoute(
                        viewModel = mockkViewModel,
                        navigateBackToSettings = navigateBackToSettings,
                        navigateBackToFileSelection = navigateBackToFileSelection,
                    )
                }
            }

            onNodeWithText(getString(OSString.importSettings_card_overrideButton))
                .assertIsDisplayed()
                .assertIsEnabled()
                .performClick()
        }

        verify(exactly = 0) { navigateBackToSettings() }
        verify(exactly = 1) { mockkViewModel.startImport(mode = ImportMode.Replace) }
    }

    @Test
    fun loading_state_test() {
        val initialMockkViewModel = getInitialViewModel(0)
        val mockkViewModel: ImportSaveDataViewModel = spyk(initialMockkViewModel) {
            every { metadataResult } returns LBResult.Failure()
            every { importSaveDataState } returns MutableStateFlow(ImportSaveDataUiState.ImportInProgress(progress = .0f))
            every { archiveKind } returns OSArchiveKind.Backup
        }

        invoke {
            setContent {
                OSUserTheme(customPrimaryColor = null) {
                    ImportSaveDataRoute(
                        viewModel = mockkViewModel,
                        navigateBackToSettings = navigateBackToSettings,
                        navigateBackToFileSelection = navigateBackToFileSelection,
                    )
                }
            }
            onNodeWithText(getString(OSString.import_settings_description_fallback))
                .assertIsDisplayed()
            onNodeWithText(getString(OSString.importSettings_card_addButton))
                .assertIsDisplayed()
                .assertIsNotEnabled()
            onNodeWithText(getString(OSString.importSettings_card_overrideButton))
                .assertIsDisplayed()
                .assertIsNotEnabled()
            onRoot().printToCacheDir(printRule)
        }

        verify(exactly = 0) { navigateBackToSettings() }
    }

    @Test
    fun success_state_test() {
        val initialMockkViewModel = getInitialViewModel(0)
        val mockkViewModel: ImportSaveDataViewModel = spyk(initialMockkViewModel) {
            every { metadataResult } returns LBResult.Success(
                ImportMetadata(
                    OSArchiveKind.Backup,
                    false,
                    0,
                    0,
                    "Android - Test",
                    1,
                    Instant.now(),
                ),
            )
            every { importSaveDataState } returns MutableStateFlow(ImportSaveDataUiState.Success)
        }

        invoke {
            setContent {
                OSUserTheme(customPrimaryColor = null) {
                    ImportSaveDataRoute(
                        viewModel = mockkViewModel,
                        navigateBackToSettings = navigateBackToSettings,
                        navigateBackToFileSelection = navigateBackToFileSelection,
                    )
                }
            }

            onRoot().printToCacheDir(printRule)
        }
    }

    @Test
    fun confirmation_replace_test() {
        val mockkViewModel = ImportSaveDataViewModel(
            countSafeItemInParentUseCase = mockk {
                coEvery { this@mockk.notDeleted(any()) } returns LBResult.Success(10)
            },
            importGetMetaDataDelegateImpl = mockk {
                every { this@mockk.metadataResult } returns LBResult.Success(
                    ImportMetadata(
                        OSArchiveKind.Backup,
                        false,
                        10,
                        0,
                        "Android-Test",
                        1,
                        Instant.now(),
                    ),
                )
            },
            importSaveDataDelegate = ImportSaveDataDelegateImpl(
                importSaveDataUseCase = importSaveDataUseCase,
                archiveDir = File(""),
            ),
        )

        invoke {
            setContent {
                OSUserTheme(customPrimaryColor = null) {
                    ImportSaveDataRoute(
                        viewModel = mockkViewModel,
                        navigateBackToSettings = navigateBackToSettings,
                        navigateBackToFileSelection = navigateBackToFileSelection,
                    )
                }
            }

            onRoot().printToCacheDir(printRule)
            onNodeWithText(getString(OSString.importSettings_card_overrideButton))
                .assertIsDisplayed()
                .performClick()

            onNode(isDialog())
                .assertIsDisplayed()

            onNodeWithText(getString(OSString.safeItemDetail_cancelAlert_yes))
                .assertIsDisplayed()
                .performClick()

            onNode(isDialog())
                .assertDoesNotExist()
        }
    }

    @Test
    fun import_sharing_test() {
        val itemCount = 10
        val mockkViewModel = getInitialViewModel(itemCount)

        invoke {
            setContent {
                OSUserTheme(customPrimaryColor = null) {
                    ImportSaveDataRoute(
                        viewModel = mockkViewModel,
                        navigateBackToSettings = navigateBackToSettings,
                        navigateBackToFileSelection = navigateBackToFileSelection,
                        showSnackBar = showSnackBar,
                    )
                }
            }

            onNodeWithTag(ImportSharingScreen).assertIsDisplayed()
            onNodeWithText(
                getQuantityString(
                    OSPlurals.import_settings_description_share,
                    itemCount,
                    itemCount,
                ).markdownToAnnotatedString().text,
                useUnmergedTree = true,
            ).assertIsDisplayed()
        }
    }

    private fun getInitialViewModel(itemCount: Int, currentSafeItemCount: Int = 10): ImportSaveDataViewModel {
        return ImportSaveDataViewModel(
            countSafeItemInParentUseCase = mockk {
                coEvery { this@mockk.notDeleted(any()) } returns LBResult.Success(currentSafeItemCount)
            },
            importGetMetaDataDelegateImpl = mockk {
                every { this@mockk.metadataResult } returns LBResult.Success(
                    ImportMetadata(
                        OSArchiveKind.Sharing,
                        false,
                        itemCount,
                        0,
                        "Android-Test",
                        1,
                        Instant.now(),
                    ),
                )
            },
            importSaveDataDelegate = ImportSaveDataDelegateImpl(
                importSaveDataUseCase = importSaveDataUseCase,
                archiveDir = File(""),
            ),
        )
    }
}
