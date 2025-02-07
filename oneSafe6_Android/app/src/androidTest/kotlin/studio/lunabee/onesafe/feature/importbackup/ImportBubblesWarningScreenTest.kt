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
 * Created by Lunabee Studio / Date - 9/5/2024 - for the oneSafe6 SDK.
 * Last modified 05/09/2024 11:31
 */

package studio.lunabee.onesafe.feature.importbackup

import androidx.compose.ui.test.ExperimentalTestApi
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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import studio.lunabee.compose.androidtest.LbcComposeTest
import studio.lunabee.compose.androidtest.extension.printToCacheDir
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.feature.importbackup.bubbleswarning.ImportBubblesWarningNavScope
import studio.lunabee.onesafe.feature.importbackup.bubbleswarning.ImportBubblesWarningRoute
import studio.lunabee.onesafe.feature.importbackup.bubbleswarning.ImportBubblesWarningViewModel
import studio.lunabee.onesafe.feature.importbackup.savedata.ImportSaveDataUiState
import studio.lunabee.onesafe.ui.theme.OSUserTheme

@OptIn(ExperimentalTestApi::class)
@HiltAndroidTest
class ImportBubblesWarningScreenTest : LbcComposeTest() {
    @get:Rule(order = 0)
    val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    private val mockkNavigateBackToSettings: () -> Unit = spyk({ })
    private val mockkNavigateToSaveData: () -> Unit = spyk({ })

    private val mockkViewModel: ImportBubblesWarningViewModel = spyk(mockk<ImportBubblesWarningViewModel>())
    private val navScope: ImportBubblesWarningNavScope = mockk {
        every { this@mockk.navigateBackToSettings } returns mockkNavigateBackToSettings
        every { this@mockk.navigateToSaveData } returns mockkNavigateToSaveData
        every { this@mockk.navigateBack } returns {}
        every { this@mockk.showSnackBar } returns {}
    }

    @Test
    fun backup_without_items_test() {
        every { mockkViewModel.importSaveDataState } returns MutableStateFlow(ImportSaveDataUiState.Success)
        every { mockkViewModel.hasItemsToImports } returns false
        every { mockkViewModel.saveBubblesData() } returns Unit
        invoke {
            setContent {
                OSUserTheme(customPrimaryColor = null) {
                    ImportBubblesWarningRoute(
                        navScope = navScope,
                        viewModel = mockkViewModel,
                    )
                }
            }

            onNodeWithText(getString(OSString.import_bubblesWarning_import))
                .performScrollTo()
                .performClick()
            onRoot().printToCacheDir(printRule)
        }
        verify(exactly = 1) { mockkNavigateBackToSettings() }
        verify(exactly = 0) { mockkNavigateToSaveData() }
        verify(exactly = 1) { mockkViewModel.saveBubblesData() }
    }

    @Test
    fun backup_with_items_test() {
        every { mockkViewModel.importSaveDataState } returns MutableStateFlow(value = ImportSaveDataUiState.WaitingForUserChoice)
        every { mockkViewModel.hasItemsToImports } returns true
        invoke {
            setContent {
                OSUserTheme(customPrimaryColor = null) {
                    ImportBubblesWarningRoute(
                        navScope = navScope,
                        viewModel = mockkViewModel,
                    )
                }
            }
            onNodeWithText(getString(OSString.import_bubblesWarning_confirmButton))
                .performScrollTo()
                .performClick()
            onRoot().printToCacheDir(printRule)
        }
        verify(exactly = 0) { mockkNavigateBackToSettings() }
        verify(exactly = 1) { mockkNavigateToSaveData() }
        verify(exactly = 0) { mockkViewModel.saveBubblesData() }
    }
}
