package studio.lunabee.onesafe.feature.settings.bubbles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.common.utils.observeIsOSKImeSelectedAsState
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.action.topAppBarOptionNavBack
import studio.lunabee.onesafe.commonui.settings.AutoLockBackgroundDelay
import studio.lunabee.onesafe.commonui.settings.AutoLockInactivityDelay
import studio.lunabee.onesafe.feature.settings.bubbles.composable.BubblesResendChangeDelayBottomSheet
import studio.lunabee.onesafe.molecule.ElevatedTopAppBar
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.extensions.topAppBarElevation
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun BubblesSettingsRoute(
    navigateBack: () -> Unit,
    navigateToKeyboardOnBoarding: () -> Unit,
    viewModel: BubblesSettingsViewModel = hiltViewModel(),
) {
    val uiState: BubblesSettingsUiState by viewModel.uiState.collectAsStateWithLifecycle()

    var isDelayResendBottomSheetVisible by rememberSaveable { mutableStateOf(value = false) }

    BubblesResendChangeDelayBottomSheet(
        isVisible = isDelayResendBottomSheetVisible,
        onSelect = viewModel::setBubblesResendMessageDelay,
        onBottomSheetClosed = { isDelayResendBottomSheetVisible = false },
        selectedAutoLockAppChangeDelay = uiState.bubblesResendMessageDelay,
    )

    BubblesSettingsScreen(
        navigateBack = navigateBack,
        uiState = uiState,
        startOneSafeKOnBoarding = navigateToKeyboardOnBoarding,
        setBubblesPreviewActivation = viewModel::setBubblesPreviewActivation,
        onResendMessageClick = { isDelayResendBottomSheetVisible = true },
        onSelectAutoLockInactivityDelay = viewModel::setAutoLockInactivityDelay,
        onSelectAutoLockHiddenDelay = viewModel::setAutoLockHiddenDelay,
        featureFlagOneSafeK = viewModel.featureFlags.oneSafeK(),
        featureFlagFlorisBoard = viewModel.featureFlags.florisBoard(),
    )
}

@Composable
fun BubblesSettingsScreen(
    navigateBack: () -> Unit,
    uiState: BubblesSettingsUiState,
    startOneSafeKOnBoarding: () -> Unit,
    setBubblesPreviewActivation: (Boolean) -> Unit,
    onResendMessageClick: () -> Unit,
    onSelectAutoLockInactivityDelay: (delay: AutoLockInactivityDelay) -> Unit,
    onSelectAutoLockHiddenDelay: (delay: AutoLockBackgroundDelay) -> Unit,
    featureFlagOneSafeK: Boolean,
    featureFlagFlorisBoard: Boolean,
) {
    val isOSKeyboardSelected: Boolean by observeIsOSKImeSelectedAsState(foregroundOnly = true)
    val scrollState = rememberScrollState()

    OSScreen(
        testTag = UiConstants.TestTag.Screen.ExtensionSettingsScreen,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
            .fillMaxSize(),
    ) {
        ElevatedTopAppBar(
            modifier = Modifier
                .statusBarsPadding()
                .zIndex(1f)
                .align(Alignment.TopCenter),
            options = listOf(topAppBarOptionNavBack(navigateBack)),
            title = LbcTextSpec.StringResource(OSString.bubbles_title),
            elevation = scrollState.topAppBarElevation,
        )

        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(bottom = OSDimens.SystemSpacing.Large)
                .fillMaxSize()
                .padding(
                    top = OSDimens.ItemTopBar.Height,
                    start = OSDimens.SystemSpacing.Regular,
                    end = OSDimens.SystemSpacing.Regular,
                )
                .verticalScroll(scrollState)
                .padding(top = OSDimens.SystemSpacing.Large),
            verticalArrangement = Arrangement.spacedBy(OSDimens.SystemSpacing.Regular),
        ) {
            ExtensionBubblesCard(
                isBubblesPreviewActivated = uiState.isBubblesPreviewActivated,
                toggleBubblesPreview = setBubblesPreviewActivation,
                onResendMessageClick = onResendMessageClick,
                bubblesResendMessageDelay = uiState.bubblesResendMessageDelay,
            )

            if (featureFlagOneSafeK) {
                if (uiState.hasFinishOneSafeKOnBoarding || isOSKeyboardSelected) {
                    ExtensionOneSafeKConfigureCard(
                        onSelectAutoLockInactivityDelay = onSelectAutoLockInactivityDelay,
                        onSelectAutoLockHiddenDelay = onSelectAutoLockHiddenDelay,
                        inactivityDelay = uiState.inactivityDelay,
                        hiddenDelay = uiState.hiddenDelay,
                        featureFlagFlorisBoard = featureFlagFlorisBoard,
                    )
                } else {
                    ExtensionOneSafeKOnBoardingCard(
                        onClickOnStartOnBoarding = startOneSafeKOnBoarding,
                    )
                }
            }
        }
    }
}

@OsDefaultPreview
@Composable
fun BubblesSettingsScreenPreview() {
    OSPreviewBackgroundTheme {
        BubblesSettingsScreen(
            navigateBack = { },
            uiState = BubblesSettingsUiState.default(),
            startOneSafeKOnBoarding = {},
            setBubblesPreviewActivation = {},
            onResendMessageClick = {},
            onSelectAutoLockInactivityDelay = {},
            onSelectAutoLockHiddenDelay = {},
            featureFlagOneSafeK = true,
            featureFlagFlorisBoard = true,
        )
    }
}

@OsDefaultPreview
@Composable
fun BubblesSettingsOnBoardingScreenPreview() {
    OSPreviewBackgroundTheme {
        BubblesSettingsScreen(
            navigateBack = { },
            uiState = BubblesSettingsUiState.default(),
            startOneSafeKOnBoarding = {},
            setBubblesPreviewActivation = {},
            onResendMessageClick = {},
            onSelectAutoLockInactivityDelay = {},
            onSelectAutoLockHiddenDelay = {},
            featureFlagOneSafeK = true,
            featureFlagFlorisBoard = true,
        )
    }
}
