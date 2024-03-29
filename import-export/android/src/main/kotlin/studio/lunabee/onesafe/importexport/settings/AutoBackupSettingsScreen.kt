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
 * Created by Lunabee Studio / Date - 10/17/2023 - for the oneSafe6 SDK.
 * Last modified 10/17/23, 3:41 PM
 */

package studio.lunabee.onesafe.importexport.settings

import android.Manifest
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.zIndex
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.android.gms.auth.GoogleAuthUtil
import kotlinx.coroutines.launch
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.action.topAppBarOptionNavBack
import studio.lunabee.onesafe.commonui.dialog.DefaultAlertDialog
import studio.lunabee.onesafe.commonui.dialog.DialogState
import studio.lunabee.onesafe.commonui.extension.findFragmentActivity
import studio.lunabee.onesafe.commonui.notification.NotificationPermissionRationaleDialogState
import studio.lunabee.onesafe.commonui.snackbar.SnackbarState
import studio.lunabee.onesafe.importexport.model.CloudBackup
import studio.lunabee.onesafe.importexport.model.LatestBackups
import studio.lunabee.onesafe.importexport.model.LocalBackup
import studio.lunabee.onesafe.importexport.utils.AccountPermissionRationaleDialogState
import studio.lunabee.onesafe.importexport.utils.BackupFileManagerHelper
import studio.lunabee.onesafe.model.OSSwitchState
import studio.lunabee.onesafe.molecule.ElevatedTopAppBar
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.extensions.topAppBarElevation
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewOnSurfaceTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview
import java.io.File
import java.net.URI
import java.time.Instant

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AutoBackupSettingsRoute(
    navigateBack: () -> Unit,
    viewModel: AutoBackupSettingsViewModel = hiltViewModel(),
    navigateToRestoreBackup: (String) -> Unit,
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

    val accountLauncher = rememberAccountLauncher(
        setupCloudBackup = viewModel::setupCloudBackupAndSync,
        showError = viewModel::showError,
    )

    val accountsPermissionState = accountPermissionState(
        accountLauncher = accountLauncher,
        setPermissionDialogState = { permissionDialogState = it },
    )

    val authorizeDrive: AutoBackupSettingsDriveAuth? by viewModel.authorizeDrive.collectAsStateWithLifecycle()
    authorizeDrive?.let { DriveAuthorize(it) }

    val snackbarHostState = remember { SnackbarHostState() }
    val errorSnackbarState: SnackbarState? by viewModel.snackbarState.collectAsStateWithLifecycle()
    val errorSnackBarVisual = errorSnackbarState?.snackbarVisuals
    LaunchedEffect(errorSnackbarState) {
        errorSnackBarVisual?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    val dialogState by viewModel.dialogState.collectAsStateWithLifecycle()
    dialogState?.DefaultAlertDialog()

    val openFileManager = {
        if (!BackupFileManagerHelper.openFileManager(context)) {
            viewModel.showError(LbcTextSpec.StringResource(OSString.settings_autoBackupScreen_saveAccess_error_noFileManager))
        }
    }

    when (val state = uiState) {
        null -> OSScreen(UiConstants.TestTag.Screen.AutoBackupSettingsScreen) {}
        else -> {
            AutoBackupSettingsScreen(
                uiState = state,
                navigateBack = navigateBack,
                toggleAutoBackup = {
                    val isAutoBackupEnabled = viewModel.toggleAutoBackupSetting()
                    if (isAutoBackupEnabled) {
                        requestNotificationPermission(notificationPermissionState) { permissionDialogState = it }
                    }
                },
                toggleCloudBackup = when (state.cloudBackupEnabledState) {
                    OSSwitchState.True -> {
                        { viewModel.disableCloudBackupSettings() }
                    }
                    OSSwitchState.False -> {
                        {
                            enableCloudBackup(
                                accountsPermissionState = accountsPermissionState,
                                context = context,
                                accountLauncher = accountLauncher,
                                setPermissionDialogState = { permissionDialogState = it },
                                state = state,
                            )
                        }
                    }
                    OSSwitchState.Loading -> {
                        { /* no-op */ }
                    }
                },
                setAutoBackupFrequency = { viewModel.setAutoBackupFrequency(it) },
                navigateToRestoreBackup = navigateToRestoreBackup,
                openFileManager = openFileManager,
                featureFlagCloudBackup = viewModel.featureFlagCloudBackup,
                snackbarHostState = snackbarHostState,
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
private fun enableCloudBackup(
    accountsPermissionState: PermissionState?,
    context: Context,
    accountLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    setPermissionDialogState: (DialogState?) -> Unit,
    state: AutoBackupSettingsUiState,
) {
    when (accountsPermissionState?.status) {
        PermissionStatus.Granted -> enableCloudBackupSettings(state, context, accountLauncher)
        is PermissionStatus.Denied -> {
            if (accountsPermissionState.status.shouldShowRationale) {
                setPermissionDialogState(
                    AccountPermissionRationaleDialogState(
                        launchPermissionRequest = {
                            accountsPermissionState.launchPermissionRequest()
                            setPermissionDialogState(null)
                        },
                        dismiss = { setPermissionDialogState(null) },
                    ),
                )
            } else {
                accountsPermissionState.launchPermissionRequest()
            }
        }
        null -> enableCloudBackupSettings(state, context, accountLauncher)
    }
}

private fun enableCloudBackupSettings(
    uiState: AutoBackupSettingsUiState,
    context: Context,
    accountLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
) {
    launchAccountChooser(context, accountLauncher, uiState.driveAccount)
}

@Composable
private fun rememberAccountLauncher(
    setupCloudBackup: (String) -> Unit,
    showError: (errorMessage: LbcTextSpec) -> Unit,
): ManagedActivityResultLauncher<Intent, ActivityResult> {
    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val accountName = result.data?.extras?.getString(AccountManager.KEY_ACCOUNT_NAME)
                if (accountName != null) {
                    setupCloudBackup(accountName)
                } else {
                    showError(LbcTextSpec.StringResource(OSString.settings_autoBackupScreen_error_unexpectedNullAccount))
                }
            }
        },
    )
}

@Composable
private fun DriveAuthorize(authorizeDrive: AutoBackupSettingsDriveAuth) {
    val driveAuthLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            authorizeDrive.onAuthorize(result.resultCode == Activity.RESULT_OK)
        },
    )

    LaunchedEffect(authorizeDrive) {
        driveAuthLauncher.launch(authorizeDrive.authorizeIntent)
    }
}

@Composable
private fun AutoBackupSettingsScreen(
    uiState: AutoBackupSettingsUiState,
    navigateBack: () -> Unit,
    toggleAutoBackup: () -> Unit,
    toggleCloudBackup: (() -> Unit)?,
    setAutoBackupFrequency: (AutoBackupFrequency) -> Unit,
    navigateToRestoreBackup: (String) -> Unit,
    openFileManager: (() -> Unit)?,
    featureFlagCloudBackup: Boolean,
    snackbarHostState: SnackbarHostState,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val mainCardUiState = if (uiState.isBackupEnabled) {
        var isAutoBackupFrequencyBottomSheetVisible by rememberSaveable { mutableStateOf(value = false) }

        AutoBackupFrequencyBottomSheet(
            isVisible = isAutoBackupFrequencyBottomSheetVisible,
            onSelect = setAutoBackupFrequency,
            onBottomSheetClosed = { isAutoBackupFrequencyBottomSheetVisible = false },
            selectedAutoBackupFrequency = uiState.autoBackupFrequency,
        )

        AutoBackupSettingsMainCardUiState.Enabled(
            toggleAutoBackup = toggleAutoBackup,
            selectAutoBackupFrequency = { isAutoBackupFrequencyBottomSheetVisible = true },
            autoBackupFrequency = uiState.autoBackupFrequency,
            isCloudBackupEnabled = uiState.cloudBackupEnabledState,
            isKeepLocalBackupEnabled = uiState.isKeepLocalBackupEnabled,
            toggleKeepLocalBackup = { uiState.toggleKeepLocalBackup() },
            toggleCloudBackup = toggleCloudBackup,
        )
    } else {
        AutoBackupSettingsMainCardUiState.Disabled(
            toggleAutoBackup = toggleAutoBackup,
        )
    }

    OSScreen(
        testTag = UiConstants.TestTag.Screen.AutoBackupSettingsScreen,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
            .fillMaxSize(),
    ) {
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .zIndex(UiConstants.SnackBar.ZIndex),
        )

        val lazyListState: LazyListState = rememberLazyListState()
        LazyColumn(
            modifier = Modifier
                .padding(top = OSDimens.ItemTopBar.Height)
                .fillMaxSize()
                .testTag(UiConstants.TestTag.ScrollableContent.SettingsLazyColumn),
            contentPadding = PaddingValues(
                horizontal = OSDimens.SystemSpacing.Regular,
                vertical = OSDimens.SystemSpacing.ExtraLarge,
            ),
            state = lazyListState,
            verticalArrangement = Arrangement.spacedBy(OSDimens.SystemSpacing.Regular),
        ) {
            item {
                AutoBackupSettingsMainCard(
                    uiState = mainCardUiState,
                    featureFlagCloudBackup = featureFlagCloudBackup,
                )
            }

            if (uiState.isBackupEnabled) {
                item {
                    AutoBackupSettingsAccessBackupCard(
                        onAccessLocalClick = openFileManager,
                        onAccessRemoteClick = if (uiState.cloudBackupEnabledState.checked && featureFlagCloudBackup) {
                            uiState.driveUri?.let { { context.startActivity(Intent.parseUri(it.toString(), 0)) } }
                        } else {
                            null
                        },
                    )
                }
                item {
                    AutoBackupSettingsRestoreCard(
                        onRestoreBackupClick = {
                            val backupId = uiState.latestBackups?.latest?.id
                            if (backupId != null) {
                                navigateToRestoreBackup(backupId)
                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        context.getString(OSString.settings_autoBackupScreen_restore_noBackupMessage),
                                    )
                                }
                            }
                        },
                    )
                }
                item {
                    AutoBackupSettingsInformationCard(
                        latestBackups = uiState.latestBackups,
                    )
                }
            }
        }

        ElevatedTopAppBar(
            title = LbcTextSpec.StringResource(OSString.settings_autoBackupScreen_title),
            options = listOf(topAppBarOptionNavBack(navigateBack)),
            elevation = lazyListState.topAppBarElevation,
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

private fun launchAccountChooser(
    context: Context,
    accountLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    currentDriveAccount: String?,
) {
    val intent = AccountManager.newChooseAccountIntent(
        AccountManager.get(context).accounts.firstOrNull { it.name == currentDriveAccount },
        null,
        arrayOf(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE),
        context.getString(OSString.android_chooseGoogleAccount_description),
        null,
        null,
        null,
    )

    accountLauncher.launch(intent)
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun accountPermissionState(
    accountLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    setPermissionDialogState: (DialogState?) -> Unit,
): PermissionState? {
    val context = LocalContext.current
    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        rememberPermissionState(Manifest.permission.GET_ACCOUNTS) { isGranted ->
            if (isGranted) {
                launchAccountChooser(context, accountLauncher, null)
            } else {
                // Manually get shouldShowRationale https://github.com/google/accompanist/issues/1690
                val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                    context.findFragmentActivity(),
                    Manifest.permission.GET_ACCOUNTS,
                )
                if (!shouldShowRationale) {
                    setPermissionDialogState(
                        AccountPermissionRationaleDialogState(
                            launchPermissionRequest = { launchAccountChooser(context, accountLauncher, null) },
                            dismiss = { setPermissionDialogState(null) },
                        ),
                    )
                }
            }
        }
    } else {
        null
    }
}

@Composable
@OsDefaultPreview
private fun AutoBackupSettingsScreenOnPreview() {
    OSPreviewOnSurfaceTheme {
        AutoBackupSettingsScreen(
            uiState = AutoBackupSettingsUiState(
                isBackupEnabled = true,
                autoBackupFrequency = AutoBackupFrequency.WEEKLY,
                latestBackups = LatestBackups(
                    LocalBackup(date = Instant.now(), file = File("")),
                    CloudBackup(remoteId = "", name = "", date = Instant.now()),
                ),
                cloudBackupEnabledState = OSSwitchState.True,
                isKeepLocalBackupEnabled = true,
                toggleKeepLocalBackup = {},
                driveUri = URI.create(""),
                driveAccount = "",
            ),
            navigateBack = {},
            toggleAutoBackup = {},
            toggleCloudBackup = {},
            setAutoBackupFrequency = {},
            navigateToRestoreBackup = {},
            openFileManager = {},
            featureFlagCloudBackup = true,
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

@Composable
@OsDefaultPreview
private fun AutoBackupSettingsScreenOffPreview() {
    OSPreviewOnSurfaceTheme {
        AutoBackupSettingsScreen(
            uiState = AutoBackupSettingsUiState.disabled(),
            navigateBack = {},
            toggleAutoBackup = {},
            toggleCloudBackup = {},
            setAutoBackupFrequency = {},
            navigateToRestoreBackup = {},
            openFileManager = {},
            featureFlagCloudBackup = true,
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
