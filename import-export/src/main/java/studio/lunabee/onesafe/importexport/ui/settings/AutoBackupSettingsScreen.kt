/*
 * Copyright (c) 2023-2023 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 10/3/2023 - for the oneSafe6 SDK.
 * Last modified 10/3/23, 12:41 PM
 */

package studio.lunabee.onesafe.importexport.ui.settings

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.commonui.R
import studio.lunabee.onesafe.commonui.action.topAppBarOptionNavBack
import studio.lunabee.onesafe.commonui.dialog.DefaultAlertDialog
import studio.lunabee.onesafe.commonui.dialog.DialogState
import studio.lunabee.onesafe.commonui.notification.NotificationPermissionRationaleDialogState
import studio.lunabee.onesafe.importexport.model.Backup
import studio.lunabee.onesafe.molecule.ElevatedTopAppBar
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.extensions.topAppBarElevation
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewOnSurfaceTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneOffset

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AutoBackupSettingsRoute(
    navigateBack: () -> Unit,
    viewModel: AutoBackupSettingsViewModel = hiltViewModel(),
    navigateToRestoreBackup: (Uri) -> Unit,
) {
    val context = LocalContext.current
    val uiState: AutoBackupSettingsUiState? by viewModel.uiState.collectAsStateWithLifecycle()

    var permissionDialogState: DialogState? by remember { mutableStateOf(null) }
    permissionDialogState?.DefaultAlertDialog()

    val notificationPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS) {}
    } else {
        null // always granted
    }

    when (val state = uiState) {
        null -> OSScreen(UiConstants.TestTag.Screen.AutoBackupSettingsScreen) {}
        AutoBackupSettingsUiState.Disabled,
        is AutoBackupSettingsUiState.Enabled,
        -> {
            AutoBackupSettingsScreen(
                uiState = state,
                navigateBack = navigateBack,
                toggleAutoBackup = {
                    val isAutoBackupEnabled = viewModel.toggleAutoBackupSetting(context)
                    if (isAutoBackupEnabled) {
                        requestNotificationPermission(notificationPermissionState) { permissionDialogState = it }
                    }
                },
                setAutoBackupFrequency = { viewModel.setAutoBackupFrequency(context, it) },
                navigateToRestoreBackup = navigateToRestoreBackup,
            )
        }
    }
}

@Composable
fun AutoBackupSettingsScreen(
    uiState: AutoBackupSettingsUiState,
    navigateBack: () -> Unit,
    toggleAutoBackup: () -> Unit,
    setAutoBackupFrequency: (AutoBackupFrequency) -> Unit,
    navigateToRestoreBackup: (Uri) -> Unit,
) {
    val mainCardUiState = when (uiState) {
        AutoBackupSettingsUiState.Disabled -> AutoBackupSettingsMainCardUiState.Disabled
        is AutoBackupSettingsUiState.Enabled -> {
            var isAutoBackupFrequencyBottomSheetVisible by rememberSaveable { mutableStateOf(value = false) }

            AutoBackupFrequencyBottomSheet(
                isVisible = isAutoBackupFrequencyBottomSheetVisible,
                onSelect = setAutoBackupFrequency,
                onBottomSheetClosed = { isAutoBackupFrequencyBottomSheetVisible = false },
                selectedAutoBackupFrequency = uiState.autoBackupFrequency,
            )

            AutoBackupSettingsMainCardUiState.Enabled(
                selectAutoBackupFrequency = { isAutoBackupFrequencyBottomSheetVisible = true },
                autoBackupFrequency = uiState.autoBackupFrequency,
            )
        }
    }

    OSScreen(
        testTag = UiConstants.TestTag.Screen.AutoBackupSettingsScreen,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
            .fillMaxSize(),
    ) {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(top = OSDimens.ItemTopBar.Height)
                .fillMaxSize()
                .padding(
                    horizontal = OSDimens.SystemSpacing.Regular,
                    vertical = OSDimens.SystemSpacing.ExtraLarge,
                ),
            verticalArrangement = Arrangement.spacedBy(OSDimens.SystemSpacing.Regular),
        ) {
            AutoBackupSettingsMainCard(
                toggleAutoBackup = toggleAutoBackup,
                uiState = mainCardUiState,
            )

            if (uiState is AutoBackupSettingsUiState.Enabled) {
                if (uiState.backups.isNotEmpty()) {
                    AutoBackupSettingsRestoreCard(
                        onRestoreBackupClick = { navigateToRestoreBackup(uiState.backups.first().file.toUri()) },
                    )
                }
                AutoBackupSettingsInformationCard(
                    date = uiState.backups.firstOrNull()?.date,
                )
            }
        }

        ElevatedTopAppBar(
            title = LbcTextSpec.StringResource(R.string.settings_autoBackupScreen_title),
            options = listOf(topAppBarOptionNavBack(navigateBack)),
            elevation = scrollState.topAppBarElevation,
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
private fun requestNotificationPermission(
    notificationPermissionState: PermissionState?,
    setPermissionDialogState: (DialogState?) -> Unit,
) {
    when (notificationPermissionState?.status) {
        is PermissionStatus.Denied -> {
            if (notificationPermissionState.status.shouldShowRationale) {
                setPermissionDialogState(
                    NotificationPermissionRationaleDialogState(
                        launchPermissionRequest = {
                            notificationPermissionState.launchPermissionRequest()
                            setPermissionDialogState(null)
                        },
                        dismiss = { setPermissionDialogState(null) },
                    ),
                )
            } else {
                notificationPermissionState.launchPermissionRequest()
            }
        }
        PermissionStatus.Granted,
        null,
        -> {
            /* no-op */
        }
    }
}

@Composable
@OsDefaultPreview
private fun AutoBackupSettingsScreenOnPreview() {
    OSPreviewOnSurfaceTheme {
        AutoBackupSettingsScreen(
            uiState = AutoBackupSettingsUiState.Enabled(
                AutoBackupFrequency.WEEKLY,
                listOf(
                    Backup(LocalDateTime.now(), File("")),
                    Backup(LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC), File("")),
                ),
            ),
            navigateBack = {},
            toggleAutoBackup = {},
            setAutoBackupFrequency = {},
            navigateToRestoreBackup = {},
        )
    }
}

@Composable
@OsDefaultPreview
private fun AutoBackupSettingsScreenOffPreview() {
    OSPreviewOnSurfaceTheme {
        AutoBackupSettingsScreen(
            uiState = AutoBackupSettingsUiState.Disabled,
            navigateBack = {},
            toggleAutoBackup = {},
            setAutoBackupFrequency = {},
            navigateToRestoreBackup = {},
        )
    }
}
