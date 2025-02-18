package studio.lunabee.onesafe.feature.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.commonui.biometric.DisplayBiometricLabels
import studio.lunabee.onesafe.commonui.biometric.biometricPrompt
import studio.lunabee.onesafe.commonui.dialog.DefaultAlertDialog
import studio.lunabee.onesafe.commonui.dialog.rememberDialogState
import studio.lunabee.onesafe.feature.enterpassword.EnterPasswordBottomSheet
import studio.lunabee.onesafe.feature.settings.dialog.AccountDeletionDialogState
import studio.lunabee.onesafe.feature.settings.personalization.AppIconUi
import studio.lunabee.onesafe.feature.settings.personalization.IconChoiceBottomSheet
import studio.lunabee.onesafe.feature.settings.prevention.PreventionActionBottomSheet
import studio.lunabee.onesafe.feature.settings.prevention.PreventionSettingsWarningData
import studio.lunabee.onesafe.feature.settings.prevention.UiPreventionAction
import studio.lunabee.onesafe.feature.settings.prevention.UiPreventionSettingsWarning
import studio.lunabee.onesafe.feature.settings.tabs.OneSafeSettingsTab
import studio.lunabee.onesafe.feature.settings.tabs.VaultSettingsTab
import studio.lunabee.onesafe.feature.supportus.SupportUsBottomSheet
import studio.lunabee.onesafe.model.AppIcon
import studio.lunabee.onesafe.organism.card.component.OSCardGlobalAction
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.UiConstants.TestTag.ScrollableContent.SettingsHorizontalPager
import studio.lunabee.onesafe.ui.theme.OSTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun SettingsRoute(
    settingsNavigation: SettingsNavigation,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    var isUpdateIconBottomSheetVisible: Boolean by rememberSaveable { mutableStateOf(value = false) }
    var isSupportBottomSheetVisible by rememberSaveable { mutableStateOf(value = false) }
    var isEnterPasswordBottomSheetVisible by rememberSaveable { mutableStateOf(value = false) }
    var aliasIconSelected: AppIconUi by remember { mutableStateOf(value = AppIconUi.fromAppIcon(AppIcon.Default)) }
    var preventionActionBottomSheet: List<UiPreventionAction> by remember { mutableStateOf(emptyList()) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val settingsData by viewModel.settingsData.collectAsStateWithLifecycle()

    var dialogState by rememberDialogState()
    dialogState?.DefaultAlertDialog()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.updatePanicEnabledWidgetState()
    }

    if (uiState != SettingsUiState.Initializing) {
        when (val state = uiState) {
            SettingsUiState.Idle, SettingsUiState.Initializing -> {
                /* no-op */
            }
            is SettingsUiState.NavigateChangePassword -> {
                settingsNavigation.navigateToChangePasswordSettings()
                state.reset()
            }
            is SettingsUiState.ShowBiometricAuthentication -> {
                val biometricPrompt = biometricPrompt(
                    labels = DisplayBiometricLabels.Login,
                    getCipher = { state.cipher },
                    onSuccess = {
                        viewModel.resetState()
                        settingsNavigation.navigateToChangePasswordSettings()
                    },
                    onFailure = {
                        viewModel.resetState()
                        isEnterPasswordBottomSheetVisible = true
                    },
                    onUserCancel = viewModel::resetState,
                    onNegative = {
                        viewModel.resetState()
                        isEnterPasswordBottomSheetVisible = true
                    },
                )
                LaunchedEffect(Unit) {
                    biometricPrompt()
                }
            }
            SettingsUiState.ShowPasswordAuthentication -> LaunchedEffect(state) {
                isEnterPasswordBottomSheetVisible = true
                viewModel.resetState()
            }
        }

        SupportUsBottomSheet(
            isVisible = isSupportBottomSheetVisible,
            onBottomSheetClosed = { isSupportBottomSheetVisible = false },
            onClickOnSupportUs = {
                isSupportBottomSheetVisible = false
                viewModel.hasRatedOS()
            },
        )

        EnterPasswordBottomSheet(
            isVisible = isEnterPasswordBottomSheetVisible,
            onBottomSheetClosed = { isEnterPasswordBottomSheetVisible = false },
            onConfirm = {
                isEnterPasswordBottomSheetVisible = false
                viewModel.onPasswordConfirmed()
            },
        )

        PreventionActionBottomSheet(
            actions = preventionActionBottomSheet,
            onBottomSheetClosed = { preventionActionBottomSheet = emptyList() },
        )

        IconChoiceBottomSheet(
            isVisible = isUpdateIconBottomSheetVisible,
            aliasIconDisplayed = aliasIconSelected,
            onBottomSheetClosed = { isUpdateIconBottomSheetVisible = false },
            onConfirm = viewModel::changeIcon,
        )

        SettingsScreen(
            showSafeCta = settingsData.showIndependentSafeCard,
            isOverEncryptionEnabled = settingsData.isOverEncryptionEnabled,
            navigateBack = settingsNavigation.navigateBack,
            importData = settingsNavigation.navigateToImportFile,
            exportData = settingsNavigation.navigateToExportAuthDestination,
            navigateToSecuritySettings = settingsNavigation.navigateToSecuritySettings,
            navigateToAbout = settingsNavigation.navigateToAbout,
            navigateToAutoFillScreen = settingsNavigation.navigateToAutofillSettings,
            navigateToBubblesSettings = settingsNavigation.navigateToBubblesSettings,
            navigateToPersonalizationSettings = settingsNavigation.navigateToPersonalizationSettings,
            navigateToOverEncryption = settingsNavigation.navigateToOverEncryption,
            startChangePasswordFlow = viewModel::startChangePasswordFlow,
            onClickOnRateUs = { isSupportBottomSheetVisible = true },
            navigateToAutoBackupSettings = settingsNavigation.navigateToAutoBackupSettings,
            onSafeDeletion = {
                dialogState = AccountDeletionDialogState(dismiss = { dialogState = null }) {
                    settingsNavigation.navigateToLogin()
                    viewModel.deleteSafe()
                }
            },
            onCloseIndependentVaultsMessage = viewModel::hideIndependentSafeCard,
            onClosePreventWarningMessage = viewModel::hidePreventionWarningCard,
            createNewSafe = settingsNavigation.navigateToOnBoardingMultiSafe,
            currentAliasSelected = viewModel.currentAliasSelected,
            showIconBottomSheet = { appIcon ->
                aliasIconSelected = AppIconUi.fromAppIcon(appIcon)
                isUpdateIconBottomSheetVisible = true
            },
            navigateToPanicWidgetSettings = settingsNavigation.navigateToPanicWidget,
            isPanicModeEnabled = settingsData.isPanicDestructionEnabled,
            isWidgetEnabled = settingsData.isWidgetEnabled,
            preventionWarning = settingsData.preventionWarning,
            showPreventionBottomSheet = { actions -> preventionActionBottomSheet = actions },
        )
    } else {
        OSScreen(
            testTag = UiConstants.TestTag.Screen.Settings,
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
                .fillMaxSize(),
            content = { },
        )
    }
}

@Composable
fun SettingsScreen(
    showSafeCta: Boolean,
    preventionWarning: UiPreventionSettingsWarning?,
    isOverEncryptionEnabled: Boolean?,
    navigateBack: () -> Unit,
    importData: () -> Unit,
    exportData: () -> Unit,
    navigateToSecuritySettings: () -> Unit,
    navigateToAbout: () -> Unit,
    navigateToAutoFillScreen: () -> Unit,
    navigateToBubblesSettings: () -> Unit,
    navigateToPersonalizationSettings: () -> Unit,
    navigateToOverEncryption: (Boolean) -> Unit,
    startChangePasswordFlow: () -> Unit,
    onClickOnRateUs: () -> Unit,
    navigateToAutoBackupSettings: () -> Unit,
    onSafeDeletion: () -> Unit,
    onCloseIndependentVaultsMessage: () -> Unit,
    onClosePreventWarningMessage: () -> Unit,
    createNewSafe: () -> Unit,
    currentAliasSelected: AppIcon,
    showIconBottomSheet: (AppIcon) -> Unit,
    navigateToPanicWidgetSettings: () -> Unit,
    isWidgetEnabled: Boolean,
    isPanicModeEnabled: Boolean,
    showPreventionBottomSheet: (actions: List<UiPreventionAction>) -> Unit,
) {
    var currentPage by rememberSaveable { mutableIntStateOf(0) }
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { SettingsTab.entries.size },
    )
    LaunchedEffect(currentPage) {
        pagerState.animateScrollToPage(currentPage)
    }
    LaunchedEffect(pagerState.currentPage) {
        currentPage = pagerState.currentPage
    }

    OSScreen(
        testTag = UiConstants.TestTag.Screen.Settings,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
            .fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            SettingsTopBar(
                onBackClick = navigateBack,
                currentPage = pagerState.currentPage,
                onTabSelected = { idx -> currentPage = idx },
            )
            HorizontalPager(
                state = pagerState,
                verticalAlignment = Alignment.Top,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag(SettingsHorizontalPager),
            ) { pageNumber ->
                when (pageNumber) {
                    0 -> {
                        val preventionSettingsWarningData = preventionWarning?.let {
                            PreventionSettingsWarningData(
                                preventionWarning = preventionWarning,
                                onClosePreventWarningMessage = onClosePreventWarningMessage,
                                onPreventionCardClicked = when (preventionWarning) {
                                    UiPreventionSettingsWarning.PasswordVerification ->
                                        OSCardGlobalAction.Navigation(navigateToSecuritySettings)
                                    UiPreventionSettingsWarning.Backup,
                                    UiPreventionSettingsWarning.PasswordVerificationAndBackup,
                                    -> {
                                        OSCardGlobalAction.Default {
                                            val preventionActions = buildList {
                                                add(UiPreventionAction.CreateBackup(onClick = exportData))
                                                add(UiPreventionAction.EnableAutoBackup(onClick = navigateToAutoBackupSettings))
                                                if (preventionWarning == UiPreventionSettingsWarning.PasswordVerificationAndBackup) {
                                                    add(
                                                        UiPreventionAction.EnablePasswordVerification(
                                                            onClick = navigateToSecuritySettings,
                                                        ),
                                                    )
                                                }
                                            }
                                            showPreventionBottomSheet(preventionActions)
                                        }
                                    }
                                },
                            )
                        }
                        VaultSettingsTab(
                            showSafeCta = showSafeCta,
                            importData = importData,
                            exportData = exportData,
                            navigateToSecuritySettings = navigateToSecuritySettings,
                            navigateToBubblesSettings = navigateToBubblesSettings,
                            navigateToPersonalizationSettings = navigateToPersonalizationSettings,
                            startChangePasswordFlow = startChangePasswordFlow,
                            navigateToAutoBackupSettings = navigateToAutoBackupSettings,
                            onSafeDeletion = onSafeDeletion,
                            onCloseIndependentVaultsMessage = onCloseIndependentVaultsMessage,
                            navigateToPanicWidgetSettings = navigateToPanicWidgetSettings,
                            isPanicModeEnabled = isPanicModeEnabled,
                            isWidgetEnabled = isWidgetEnabled,
                            preventionSettingsWarningData = preventionSettingsWarningData,
                        )
                    }
                    1 -> {
                        OneSafeSettingsTab(
                            isOverEncryptionEnabled = isOverEncryptionEnabled,
                            navigateToAbout = navigateToAbout,
                            onClickOnRateUs = onClickOnRateUs,
                            createNewSafe = createNewSafe,
                            onOverEncryptionClick = navigateToOverEncryption,
                            currentAliasSelected = currentAliasSelected,
                            onIconAliasClick = showIconBottomSheet,
                            navigateToAutoFillScreen = navigateToAutoFillScreen,
                        )
                    }
                }
            }
        }
    }
}

@Composable
@OsDefaultPreview
fun SettingsScreenPreview() {
    OSTheme {
        SettingsScreen(
            showSafeCta = true,
            isOverEncryptionEnabled = null,
            navigateBack = {},
            importData = {},
            exportData = {},
            navigateToSecuritySettings = {},
            navigateToAbout = {},
            navigateToAutoFillScreen = {},
            navigateToBubblesSettings = {},
            navigateToPersonalizationSettings = {},
            startChangePasswordFlow = {},
            onClickOnRateUs = {},
            navigateToAutoBackupSettings = {},
            onSafeDeletion = {},
            onCloseIndependentVaultsMessage = {},
            createNewSafe = {},
            currentAliasSelected = AppIcon.Default,
            showIconBottomSheet = {},
            navigateToOverEncryption = {},
            navigateToPanicWidgetSettings = {},
            isPanicModeEnabled = true,
            isWidgetEnabled = true,
            preventionWarning = null,
            showPreventionBottomSheet = {},
            onClosePreventWarningMessage = {},
        )
    }
}

@Composable
@OsDefaultPreview
fun SettingsScreenWithPreventionPreview() {
    OSTheme {
        SettingsScreen(
            showSafeCta = true,
            isOverEncryptionEnabled = null,
            navigateBack = {},
            importData = {},
            exportData = {},
            navigateToSecuritySettings = {},
            navigateToAbout = {},
            navigateToAutoFillScreen = {},
            navigateToBubblesSettings = {},
            navigateToPersonalizationSettings = {},
            startChangePasswordFlow = {},
            onClickOnRateUs = {},
            navigateToAutoBackupSettings = {},
            onSafeDeletion = {},
            onCloseIndependentVaultsMessage = {},
            createNewSafe = {},
            currentAliasSelected = AppIcon.Default,
            showIconBottomSheet = {},
            navigateToOverEncryption = {},
            navigateToPanicWidgetSettings = {},
            isPanicModeEnabled = true,
            isWidgetEnabled = true,
            preventionWarning = UiPreventionSettingsWarning.PasswordVerification,
            showPreventionBottomSheet = {},
            onClosePreventWarningMessage = {},
        )
    }
}
