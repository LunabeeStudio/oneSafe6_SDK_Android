/*
 * Copyright (c) 2023 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 11/24/2023 - for the oneSafe6 SDK.
 * Last modified 11/24/23, 2:34 PM
 */

package studio.lunabee.onesafe.importexport.settings

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.junit.Test
import org.threeten.extra.MutableClock
import studio.lunabee.compose.androidtest.LbcComposeTest
import studio.lunabee.compose.androidtest.extension.waitUntilAtLeastOneExists
import studio.lunabee.compose.androidtest.extension.waitUntilDoesNotExist
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.dialog.DialogState
import studio.lunabee.onesafe.commonui.dialog.ErrorDialogState
import studio.lunabee.onesafe.commonui.snackbar.ErrorSnackbarState
import studio.lunabee.onesafe.importexport.model.CloudBackup
import studio.lunabee.onesafe.importexport.model.LatestBackups
import studio.lunabee.onesafe.model.OSSwitchState
import studio.lunabee.onesafe.ui.UiConstants
import java.net.URI
import java.time.Instant

@OptIn(androidx.compose.ui.test.ExperimentalTestApi::class)
class AutoBackupSettingsScreenTest : LbcComposeTest() {

    private val testClock = MutableClock.epochUTC()

    @Test
    fun cloud_loading_test() {
        setScreen(
            cloudBackupEnabledState = OSSwitchState.Loading,
        ) {
            hasTestTag(UiConstants.TestTag.Item.LoadingSwitch)
                .waitUntilAtLeastOneExists()
        }
    }

    @Test
    fun error_snackbar_test() {
        setScreen(
            errorSnackbarState = ErrorSnackbarState(LbcTextSpec.Raw("error"), {}),
        ) {
            hasText("error")
                .waitUntilExactlyOneExists()
        }
    }

    @Test
    fun feedback_snackbar_test() {
        setScreen(
            latestBackup = null,
        ) {
            val restoreBtnMatcher = hasText(getString(OSString.settings_autoBackupScreen_restore_button))
            hasScrollAction()
                .waitUntilAtLeastOneExists()
                .onFirst()
                .performScrollToNode(
                    restoreBtnMatcher,
                )

            restoreBtnMatcher
                .waitUntilExactlyOneExists()
                .performClick()
            hasText(getString(OSString.settings_autoBackupScreen_restore_noBackupMessage))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
        }
    }

    @Test
    fun show_access_cloud_test() {
        val driveUri: MutableStateFlow<URI?> = MutableStateFlow(null)
        setScreen(
            driveUri = driveUri,
        ) {
            hasText(getString(OSString.settings_autoBackupScreen_saveAccess_GoogleDriveSaves))
                .waitUntilDoesNotExist()
            driveUri.value = URI.create("uri")
            hasText(getString(OSString.settings_autoBackupScreen_saveAccess_GoogleDriveSaves))
                .waitUntilExactlyOneExists()
        }
    }

    @Test
    fun show_dialog_test() {
        val dialogState: MutableStateFlow<DialogState?> = MutableStateFlow(null)
        setScreen(
            dialogState = dialogState,
        ) {
            isDialog()
                .waitUntilDoesNotExist()
            dialogState.value = ErrorDialogState(null, emptyList()) {}
            isDialog()
                .waitUntilExactlyOneExists()
        }
    }

    private fun setScreen(
        cloudBackupEnabledState: OSSwitchState = OSSwitchState.True,
        errorSnackbarState: ErrorSnackbarState? = null,
        driveUri: StateFlow<URI?> = MutableStateFlow(URI.create("")),
        dialogState: StateFlow<DialogState?> = MutableStateFlow(null),
        latestBackup: CloudBackup? = CloudBackup("", "", Instant.now(testClock)),
        block: ComposeUiTest.() -> Unit,
    ) {
        val viewModel = mockk<AutoBackupSettingsViewModel> {
            every { uiState } returns driveUri.map {
                autoBackupSettingsUiState(latestBackup, cloudBackupEnabledState, it)
            }.stateIn(
                CoroutineScope(Dispatchers.Main.immediate),
                SharingStarted.Eagerly,
                autoBackupSettingsUiState(latestBackup, cloudBackupEnabledState, driveUri.value),
            )
            every { authorizeDrive } returns MutableStateFlow(null)
            every { snackbarState } returns MutableStateFlow(errorSnackbarState)
            every { featureFlagCloudBackup } returns true
            every { this@mockk.dialogState } returns dialogState
        }

        invoke {
            setContent {
                AutoBackupSettingsRoute(
                    navigateBack = {},
                    viewModel = viewModel,
                    navigateToRestoreBackup = {},
                )
            }
            block()
        }
    }

    private fun autoBackupSettingsUiState(
        backup: CloudBackup?,
        cloudBackupEnabledState: OSSwitchState,
        driveUri: URI?,
    ) = AutoBackupSettingsUiState(
        isBackupEnabled = true,
        autoBackupFrequency = AutoBackupFrequency.DAILY,
        latestBackups = LatestBackups(local = null, cloud = backup),
        cloudBackupEnabledState = cloudBackupEnabledState,
        isKeepLocalBackupEnabled = true,
        toggleKeepLocalBackup = {},
        driveUri = driveUri,
        driveAccount = "",
    )
}
