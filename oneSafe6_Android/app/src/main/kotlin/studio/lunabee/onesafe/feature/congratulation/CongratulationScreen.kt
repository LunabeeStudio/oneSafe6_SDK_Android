package studio.lunabee.onesafe.feature.congratulation

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.animation.OSTopBarAnimatedVisibility
import studio.lunabee.onesafe.animation.rememberOSTopBarVisibilityNestedScrollConnection
import studio.lunabee.onesafe.atom.OSRegularSpacer
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.atom.button.OSFilledButton
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.molecule.OSTopAppBar
import studio.lunabee.onesafe.molecule.OSTopImageBox
import studio.lunabee.onesafe.organism.card.OSCustomCard
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewOnSurfaceTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun OnboardingCongratulationRoute(
    viewModel: CongratulationViewModel = hiltViewModel(),
    navigateToLogin: () -> Unit,
) {
    val uiState: CongratulationUiState by viewModel.uiState.collectAsStateWithLifecycle()

    CongratulationScreen(
        labels = CongratulationScreenLabels.OnBoarding,
        onFinish = when (uiState) {
            CongratulationUiState.Finishing -> null
            is CongratulationUiState.Idle -> navigateToLogin
        },
    )
}

@Composable
fun MultiSafeCongratulationRoute(
    viewModel: CongratulationViewModel = hiltViewModel(),
    popSafeCreationFlow: () -> Unit,
) {
    val uiState: CongratulationUiState by viewModel.uiState.collectAsStateWithLifecycle()

    CongratulationScreen(
        labels = CongratulationScreenLabels.MultiSafe,
        onFinish = when (val state = uiState) {
            CongratulationUiState.Finishing -> null
            is CongratulationUiState.Idle -> fun() {
                popSafeCreationFlow()
                if (state.isLoggedIn) {
                    viewModel.lock()
                }
            }
        },
    )
}

@Composable
fun CongratulationScreen(
    labels: CongratulationScreenLabels,
    onFinish: (() -> Unit)?,
) {
    val scrollState: ScrollState = rememberScrollState()
    val nestedScrollConnection = rememberOSTopBarVisibilityNestedScrollConnection(scrollState)

    OSScreen(
        testTag = UiConstants.TestTag.Screen.CongratulationOnBoarding,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom)),
    ) {
        Column(
            modifier = Modifier
                .nestedScroll(nestedScrollConnection)
                .verticalScroll(scrollState),
        ) {
            OSTopImageBox(
                imageRes = OSDrawable.character_team,
                offset = null,
                modifier = Modifier
                    .padding(horizontal = OSDimens.SystemSpacing.Regular)
                    .padding(
                        top = OSDimens.SystemSpacing.Regular + OSDimens.ItemTopBar.Height,
                        bottom = OSDimens.SystemSpacing.Regular,
                    ),
            ) {
                OSCustomCard(
                    content = {
                        OSText(
                            text = labels.sectionMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = OSDimens.SystemSpacing.Regular),
                        )
                    },
                    title = labels.sectionTitle,
                )
            }

            val buttonState = if (onFinish != null) {
                OSActionState.Enabled
            } else {
                OSActionState.Disabled
            }

            OSFilledButton(
                text = LbcTextSpec.StringResource(OSString.onBoarding_congratulationScreen_goButton),
                onClick = onFinish ?: {},
                modifier = Modifier
                    .padding(horizontal = OSDimens.SystemSpacing.Regular)
                    .align(Alignment.End),
                state = buttonState,
            )
            OSRegularSpacer()
        }

        OSTopBarAnimatedVisibility(visible = nestedScrollConnection.isTopBarVisible) {
            OSTopAppBar(
                title = LbcTextSpec.StringResource(OSString.onBoarding_congratulationScreen_title),
                modifier = Modifier
                    .statusBarsPadding()
                    .align(Alignment.TopCenter),
            )
        }
    }
}

@OsDefaultPreview
@Composable
private fun CongratulationOnBoardingScreenIdlePreview() {
    OSPreviewOnSurfaceTheme {
        CongratulationScreen(
            labels = CongratulationScreenLabels.OnBoarding,
            onFinish = {},
        )
    }
}

@OsDefaultPreview
@Composable
private fun CongratulationOnBoardingScreenFinishingPreview() {
    OSPreviewOnSurfaceTheme {
        CongratulationScreen(
            labels = CongratulationScreenLabels.OnBoarding,
            onFinish = null,
        )
    }
}
