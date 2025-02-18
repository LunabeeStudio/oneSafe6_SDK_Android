package studio.lunabee.onesafe.feature.biometric

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.lunabee.lbloading.LoadingBackHandler
import kotlinx.coroutines.launch
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.animation.OSTopBarAnimatedVisibility
import studio.lunabee.onesafe.animation.rememberOSTopBarVisibilityNestedScrollConnection
import studio.lunabee.onesafe.atom.OSImage
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.atom.button.OSFilledButton
import studio.lunabee.onesafe.atom.button.OSTextButton
import studio.lunabee.onesafe.atom.button.defaults.OSTextButtonDefaults
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.action.topAppBarOptionNavBack
import studio.lunabee.onesafe.commonui.biometric.BiometricHardware
import studio.lunabee.onesafe.commonui.biometric.DisplayBiometricLabels
import studio.lunabee.onesafe.commonui.biometric.biometricPrompt
import studio.lunabee.onesafe.commonui.extension.biometricHardware
import studio.lunabee.onesafe.commonui.extension.markdown
import studio.lunabee.onesafe.commonui.snackbar.ErrorSnackbarState
import studio.lunabee.onesafe.error.OSAppError
import studio.lunabee.onesafe.feature.onboarding.SignUpUiState
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.molecule.OSTopAppBar
import studio.lunabee.onesafe.molecule.OSTopImageBox
import studio.lunabee.onesafe.organism.card.OSMessageCard
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewOnSurfaceTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun OnBoardingBiometricCreationRoute(navigateBack: () -> Unit, onFinish: () -> Unit): Unit = BiometricCreationRoute(
    navigateBack = navigateBack,
    onFinish = onFinish,
    labels = BiometricCreationLabels.Onboarding,
    viewModel = hiltViewModel<BiometricCreationViewModel.Onboarding>(),
)

@Composable
fun MultiSafeBiometricCreationRoute(navigateBack: () -> Unit, onFinish: () -> Unit): Unit = BiometricCreationRoute(
    navigateBack = navigateBack,
    onFinish = onFinish,
    labels = BiometricCreationLabels.MultiSafe,
    viewModel = hiltViewModel<BiometricCreationViewModel.Onboarding>(),
)

@Composable
fun ChangePasswordBiometricCreationRoute(onFinish: () -> Unit) {
    LoadingBackHandler {
        onFinish()
    }
    BiometricCreationRoute(
        navigateBack = onFinish,
        onFinish = onFinish,
        labels = BiometricCreationLabels.ChangePassword,
        viewModel = hiltViewModel<BiometricCreationViewModel.ChangePassword>(),
    )
}

@Composable
private fun BiometricCreationRoute(
    navigateBack: () -> Unit,
    onFinish: () -> Unit,
    labels: BiometricCreationLabels,
    viewModel: BiometricCreationViewModel,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val showLocalSnackBar: suspend (visuals: SnackbarVisuals) -> Unit = { snackbarVisuals ->
        snackbarHostState.showSnackbar(snackbarVisuals)
    }

    val context = LocalContext.current
    when (viewModel.state) {
        is SignUpUiState.Success -> LaunchedEffect(viewModel.state) { onFinish() }
        is SignUpUiState.Error -> (viewModel.state as? SignUpUiState.Error)?.error?.let {
            val snackbarVisuals = ErrorSnackbarState(
                error = it,
                onClick = viewModel::resetState,
            ).snackbarVisuals
            LaunchedEffect(snackbarVisuals) { showLocalSnackBar(snackbarVisuals) }
        }
        else -> {}
    }

    val snackbarVisualsFailedBiometric = ErrorSnackbarState(
        error = OSAppError(OSAppError.Code.BIOMETRIC_ERROR),
        onClick = {},
    ).snackbarVisuals

    val authenticate: suspend () -> Unit = biometricPrompt(
        labels = DisplayBiometricLabels.SignUp(context.biometricHardware()),
        getCipher = {
            viewModel.getBiometricCipher()
        },
        onSuccess = {
            viewModel.enableBiometric(it)
        },
        onUserCancel = viewModel::disableBiometric,
        onFailure = {
            viewModel.disableBiometric()
            coroutineScope.launch {
                showLocalSnackBar(snackbarVisualsFailedBiometric)
            }
        },
    )

    BiometricCreationScreen(
        labels = labels,
        navigateBack = navigateBack,
        onClickAccept = {
            snackbarHostState.currentSnackbarData?.dismiss()
            coroutineScope.launch {
                authenticate()
            }
        },
        onClickNo = {
            onFinish()
        },
        snackbarHostState = snackbarHostState,
    )
}

@Composable
fun BiometricCreationScreen(
    labels: BiometricCreationLabels,
    navigateBack: () -> Unit,
    onClickAccept: () -> Unit,
    onClickNo: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    val context = LocalContext.current
    val scrollState: ScrollState = rememberScrollState()
    val nestedScrollConnection = rememberOSTopBarVisibilityNestedScrollConnection(scrollState)
    OSScreen(
        testTag = UiConstants.TestTag.Screen.BiometricSetup,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom)),
    ) {
        Column(
            modifier = Modifier
                .nestedScroll(nestedScrollConnection)
                .verticalScroll(scrollState)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val biometricHardware: BiometricHardware = context.biometricHardware()

            when (labels) {
                is BiometricCreationLabels.MultiSafe -> {
                    OSTopImageBox(
                        imageRes = OSDrawable.character_hello,
                        xImageOffset = OSDimens.SystemSpacing.ExtraLarge,
                        modifier = Modifier
                            .padding(top = OSDimens.OnBoarding.paddingCharacterHello + OSDimens.ItemTopBar.Height),
                    ) {
                        Body(
                            labels = labels,
                            onClickAccept = onClickAccept,
                            onClickNo = onClickNo,
                            invertPrimaryButton = true,
                        )
                    }
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .padding(
                                top = OSDimens.OnBoarding.paddingSecurityIcon + OSDimens.ItemTopBar.Height,
                            )
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(OSDimens.AlternativeSpacing.Dimens12),
                    ) {
                        OSImage(
                            image = OSImageSpec.Drawable(
                                drawable = biometricHardware.icon ?: OSDrawable.ic_fingerprint,
                                tintColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                            contentDescription = null,
                            modifier = Modifier
                                .size(OSDimens.SystemImageDimension.XLarge.dp),
                        )
                    }

                    Spacer(modifier = Modifier.size(OSDimens.SystemSpacing.Regular))

                    Body(
                        labels = labels,
                        onClickAccept = onClickAccept,
                        onClickNo = onClickNo,
                        invertPrimaryButton = false,
                    )
                }
            }
        }
        OSTopBarAnimatedVisibility(visible = nestedScrollConnection.isTopBarVisible) {
            OSTopAppBar(
                modifier = Modifier
                    .statusBarsPadding()
                    .align(Alignment.TopCenter),
                options = listOf(topAppBarOptionNavBack(navigateBack)),
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .zIndex(UiConstants.SnackBar.ZIndex)
                .align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun ColumnScope.Body(
    labels: BiometricCreationLabels,
    onClickAccept: () -> Unit,
    onClickNo: () -> Unit,
    invertPrimaryButton: Boolean,
) {
    OSMessageCard(
        description = labels.sectionMessage.markdown(),
        modifier = Modifier.padding(horizontal = OSDimens.SystemSpacing.Regular),
        title = labels.sectionTitle,
    )
    Spacer(modifier = Modifier.size(OSDimens.SystemSpacing.Regular))
    Column(
        verticalArrangement = Arrangement.spacedBy(OSDimens.SystemSpacing.Regular),
        modifier = Modifier.align(Alignment.End),
    ) {
        val filledButtonContent = if (invertPrimaryButton) {
            Pair(OSString.onBoarding_fastIdScreen_noButton, onClickNo)
        } else {
            Pair(OSString.onBoarding_fastIdScreen_acceptButton, onClickAccept)
        }
        val textButtonContent = if (invertPrimaryButton) {
            Pair(OSString.onBoarding_fastIdScreen_acceptButton, onClickAccept)
        } else {
            Pair(OSString.onBoarding_fastIdScreen_noButton, onClickNo)
        }
        OSFilledButton(
            text = LbcTextSpec.StringResource(filledButtonContent.first),
            onClick = filledButtonContent.second,
            modifier = Modifier
                .padding(horizontal = OSDimens.SystemSpacing.Regular),
        )
        OSTextButton(
            text = LbcTextSpec.StringResource(textButtonContent.first),
            onClick = textButtonContent.second,
            modifier = Modifier
                .padding(horizontal = OSDimens.SystemSpacing.ExtraSmall),
            buttonColors = OSTextButtonDefaults.primaryTextButtonColors(state = OSActionState.Enabled),
        )
    }
}

@OsDefaultPreview
@Composable
private fun BiometricSetupScreenPreview() {
    OSPreviewOnSurfaceTheme {
        BiometricCreationScreen(
            labels = BiometricCreationLabels.Onboarding,
            navigateBack = {},
            onClickAccept = {},
            onClickNo = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

@OsDefaultPreview
@Composable
private fun BiometricSetupScreenMultiSafePreview() {
    OSPreviewOnSurfaceTheme {
        BiometricCreationScreen(
            labels = BiometricCreationLabels.MultiSafe,
            navigateBack = {},
            onClickAccept = {},
            onClickNo = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
