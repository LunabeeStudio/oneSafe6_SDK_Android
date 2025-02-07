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
 * Last modified 05/09/2024 13:40
 */

package studio.lunabee.onesafe.feature.importbackup

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsNotEnabled
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
import studio.lunabee.onesafe.feature.importbackup.selectdata.ImportSelectDataNavScope
import studio.lunabee.onesafe.feature.importbackup.selectdata.ImportSelectDataRoute
import studio.lunabee.onesafe.feature.importbackup.selectdata.ImportSelectDataViewModel
import studio.lunabee.onesafe.ui.theme.OSUserTheme

@OptIn(ExperimentalTestApi::class)
@HiltAndroidTest
class ImportSelectDataScreenTest : LbcComposeTest() {
    @get:Rule(order = 0)
    val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    private val mockkNavigateToSaveData: (Boolean) -> Unit = spyk({ it })
    private val selectDataNavScope: ImportSelectDataNavScope = mockk {
        every { this@mockk.continueImport } returns mockkNavigateToSaveData
        every { this@mockk.navigateBack } returns {}
    }

    private val mockkViewModel: ImportSelectDataViewModel = spyk(mockk<ImportSelectDataViewModel>()) {
        every { this@spyk.setImportData() } returns Unit
    }

    @Test
    fun select_items_only_test() {
        every { mockkViewModel.isBubblesImported } returns MutableStateFlow(false)
        every { mockkViewModel.isItemsImported } returns MutableStateFlow(true)

        invoke {
            setContent {
                OSUserTheme(customPrimaryColor = null) {
                    ImportSelectDataRoute(
                        navScope = selectDataNavScope,
                        viewModel = mockkViewModel,
                    )
                }
            }

            onNodeWithText(getString(OSString.import_selectData_confirmAction))
                .performScrollTo()
                .performClick()
            onRoot().printToCacheDir(printRule)
        }

        verify(exactly = 1) { mockkNavigateToSaveData(false) }
    }

    @Test
    fun select_bubbles_only_test() {
        every { mockkViewModel.isBubblesImported } returns MutableStateFlow(true)
        every { mockkViewModel.isItemsImported } returns MutableStateFlow(false)
        invoke {
            setContent {
                OSUserTheme(customPrimaryColor = null) {
                    ImportSelectDataRoute(
                        navScope = selectDataNavScope,
                        viewModel = mockkViewModel,
                    )
                }
            }

            onNodeWithText(getString(OSString.import_selectData_confirmAction))
                .performScrollTo()
                .performClick()
            onRoot().printToCacheDir(printRule)
        }
        verify(exactly = 1) { mockkNavigateToSaveData(true) }
    }

    @Test
    fun select_nothing_test() {
        every { mockkViewModel.isBubblesImported } returns MutableStateFlow(false)
        every { mockkViewModel.isItemsImported } returns MutableStateFlow(false)
        invoke {
            setContent {
                OSUserTheme(customPrimaryColor = null) {
                    ImportSelectDataRoute(
                        navScope = selectDataNavScope,
                        viewModel = mockkViewModel,
                    )
                }
            }

            onNodeWithText(getString(OSString.import_selectData_confirmAction))
                .performScrollTo()
                .assertIsNotEnabled()
                .performClick()
            onRoot().printToCacheDir(printRule)
        }
        verify(exactly = 0) { mockkNavigateToSaveData(any()) }
    }
}
