package studio.lunabee.onesafe.login.screen

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.zIndex
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.atom.button.OSIconButton
import studio.lunabee.onesafe.atom.button.defaults.OSIconButtonDefaults
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.beta.AppBetaVersionChip
import studio.lunabee.onesafe.commonui.extension.biometricHardware
import studio.lunabee.onesafe.commonui.localprovider.LocalIsOneSafeK
import studio.lunabee.onesafe.login.composable.LoginTextFieldCard
import studio.lunabee.onesafe.login.state.CommonLoginUiState
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.molecule.OSTopImageBox
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem
import studio.lunabee.onesafe.ui.theme.OSPreviewOnSurfaceTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun LoginScreenWrapper(
    exitIcon: LoginExitIcon,
    uiState: CommonLoginUiState.Data,
    setPasswordValue: (TextFieldValue) -> Unit,
    versionName: String,
    loginFromPassword: () -> Unit,
    onBiometricClick: (() -> Unit)?,
    @DrawableRes logoRes: Int,
    focusRequester: FocusRequester = remember { FocusRequester() },
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    isIllustrationDisplayed: Boolean,
    onCreateNewSafe: (() -> Unit)? = null,
) {
    val isLoading =
        uiState.loginResult == CommonLoginUiState.LoginResult.Loading ||
            uiState.loginResult is CommonLoginUiState.LoginResult.Success

    LoginScreen(
        labels = if (uiState.isFirstLogin) LoginScreenLabels.FirstTime else LoginScreenLabels.Accustomed,
        exitIcon = exitIcon,
        passwordValue = uiState.currentPasswordValue,
        isError = uiState.loginResult == CommonLoginUiState.LoginResult.Error,
        onValueChange = setPasswordValue,
        onConfirm = loginFromPassword,
        onBiometricClick = onBiometricClick,
        focusRequester = focusRequester,
        isLoading = isLoading,
        snackbarHostState = snackbarHostState,
        appVersionName = versionName,
        isIllustrationDisplayed = isIllustrationDisplayed,
        logoRes = logoRes,
        isBeta = uiState.isBeta,
        onCreateNewSafe = onCreateNewSafe,
    )
}

@Composable
fun LoginScreen(
    labels: LoginScreenLabels,
    exitIcon: LoginExitIcon = LoginExitIcon.None,
    passwordValue: TextFieldValue,
    isError: Boolean,
    onValueChange: (TextFieldValue) -> Unit,
    onConfirm: () -> Unit,
    onBiometricClick: (() -> Unit)?,
    focusRequester: FocusRequester,
    isLoading: Boolean,
    snackbarHostState: SnackbarHostState,
    appVersionName: String,
    isIllustrationDisplayed: Boolean,
    @DrawableRes logoRes: Int,
    isBeta: Boolean,
    onCreateNewSafe: (() -> Unit)?,
) {
    val context = LocalContext.current
    val isOneSafeK = LocalIsOneSafeK.current
    OSScreen(
        testTag = UiConstants.TestTag.Screen.Login,
        modifier = if (isOneSafeK) {
            Modifier
        } else {
            Modifier.windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
        },
        background = if (isOneSafeK) {
            LocalDesignSystem.current.bubblesBackGround()
        } else {
            LocalDesignSystem.current.backgroundGradient()
        },
        applySystemBarPadding = !isOneSafeK,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState()),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(OSDimens.SystemSpacing.Regular),
            ) {
                Image(
                    painter = painterResource(id = logoRes),
                    contentDescription = null,
                    modifier = Modifier
                        .width(width = OSDimens.LayoutSize.LoginLogoTextWidth)
                        .align(Alignment.Center),
                    colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.primary),
                )

                if (labels == LoginScreenLabels.Accustomed && onCreateNewSafe != null) {
                    OSIconButton(
                        image = OSImageSpec.Drawable(OSDrawable.ic_add),
                        onClick = onCreateNewSafe,
                        buttonSize = OSDimens.SystemButtonDimension.NavBarAction,
                        contentDescription = LbcTextSpec.StringResource(OSString.signInScreen_accessibility_newSafe),
                        modifier = Modifier.align(Alignment.CenterEnd),
                        colors = OSIconButtonDefaults.secondaryIconButtonColors(),
                    )
                }
            }
            if (isBeta) {
                AppBetaVersionChip(
                    Modifier
                        .align(Alignment.CenterHorizontally),
                )
            }
            if (isIllustrationDisplayed) {
                OSTopImageBox(
                    imageRes = OSDrawable.hello_colored,
                    modifier = Modifier
                        .padding(horizontal = OSDimens.SystemSpacing.Regular)
                        .padding(
                            top = OSDimens.SystemSpacing.ExtraLarge,
                            bottom = OSDimens.SystemSpacing.Regular,
                        ),
                    offset = OSDimens.Card.OffsetHelloColored,
                ) {
                    LoginTextFieldCard(
                        labels = labels,
                        passwordValue = passwordValue,
                        onValueChange = onValueChange,
                        focusRequester = focusRequester,
                        isError = isError,
                        onConfirm = onConfirm,
                        isLoading = isLoading,
                    )
                }
                ButtonsRow(context, isLoading, onBiometricClick, onConfirm)
            } else {
                Spacer(modifier = Modifier.weight(1f))
                LoginTextFieldCard(
                    labels = labels,
                    passwordValue = passwordValue,
                    onValueChange = onValueChange,
                    focusRequester = focusRequester,
                    isError = isError,
                    onConfirm = onConfirm,
                    isLoading = isLoading,
                    modifier = Modifier
                        .padding(horizontal = OSDimens.SystemSpacing.Regular)
                        .padding(
                            bottom = OSDimens.SystemSpacing.Regular,
                        ),
                )
                ButtonsRow(context, isLoading, onBiometricClick, onConfirm)
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        VersionNameItem(appVersionName)

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .imePadding()
                .zIndex(UiConstants.SnackBar.ZIndex)
                .align(Alignment.BottomCenter),
        )

        exitIcon.Content(
            modifier = Modifier
                .padding(OSDimens.SystemSpacing.Small)
                .align(Alignment.TopStart),
        )
    }
}

@Composable
private fun ButtonsRow(
    context: Context,
    isLoading: Boolean,
    onBiometricClick: (() -> Unit)?,
    onConfirm: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = OSDimens.SystemSpacing.Regular)
            .padding(bottom = OSDimens.SystemSpacing.Regular),
    ) {
        if (onBiometricClick != null) {
            OSIconButton(
                buttonSize = OSDimens.SystemButtonDimension.Regular,
                image = OSImageSpec.Drawable(context.biometricHardware().icon ?: OSDrawable.ic_fingerprint),
                contentDescription = LbcTextSpec.StringResource(OSString.signInScreen_accessibility_biometric),
                state = if (isLoading) OSActionState.Disabled else OSActionState.Enabled,
                onClick = onBiometricClick,
                modifier = Modifier
                    .testTag(UiConstants.TestTag.Item.LoginBiometricIcon)
                    .align(Alignment.CenterStart),
            )
        }
        OSIconButton(
            buttonSize = OSDimens.SystemButtonDimension.Regular,
            image = OSImageSpec.Drawable(studio.lunabee.onesafe.coreui.R.drawable.os_ic_check),
            contentDescription = LbcTextSpec.StringResource(OSString.signInScreen_accessibility_connect),
            onClick = onConfirm,
            state = if (isLoading) OSActionState.Disabled else OSActionState.Enabled,
            modifier = Modifier
                .testTag(UiConstants.TestTag.Item.LoginButtonIcon)
                .align(Alignment.CenterEnd),
        )
    }
}

@Composable
private fun BoxScope.VersionNameItem(versionName: String) {
    OSText(
        text = LbcTextSpec.Raw(versionName),
        modifier = Modifier
            .align(alignment = Alignment.BottomCenter)
            .navigationBarsPadding()
            .padding(bottom = OSDimens.SystemSpacing.Regular),
        style = MaterialTheme.typography.labelSmall,
    )
}

@Composable
@OsDefaultPreview
private fun LoginPreview() {
    OSPreviewOnSurfaceTheme {
        LoginScreen(
            labels = LoginScreenLabels.FirstTime,
            passwordValue = TextFieldValue(),
            isError = false,
            onValueChange = {},
            onConfirm = {},
            onBiometricClick = {},
            focusRequester = FocusRequester(),
            isLoading = false,
            snackbarHostState = remember { SnackbarHostState() },
            appVersionName = "1.10.0",
            isIllustrationDisplayed = true,
            logoRes = OSDrawable.ic_onesafe_text,
            isBeta = true,
            onCreateNewSafe = {},
        )
    }
}

@Composable
@OsDefaultPreview
private fun LoginNoIllustrationPreview() {
    OSPreviewOnSurfaceTheme {
        LoginScreen(
            labels = LoginScreenLabels.FirstTime,
            passwordValue = TextFieldValue(),
            isError = false,
            onValueChange = {},
            onConfirm = {},
            onBiometricClick = {},
            focusRequester = FocusRequester(),
            isLoading = false,
            snackbarHostState = remember { SnackbarHostState() },
            appVersionName = "1.10.0",
            isIllustrationDisplayed = false,
            logoRes = OSDrawable.ic_onesafe_text,
            isBeta = true,
            onCreateNewSafe = {},
        )
    }
}

@Composable
@OsDefaultPreview
private fun LoginPreviewError() {
    OSPreviewOnSurfaceTheme {
        LoginScreen(
            labels = LoginScreenLabels.Accustomed,
            passwordValue = TextFieldValue("***"),
            isError = true,
            onValueChange = {},
            onConfirm = {},
            onBiometricClick = {},
            focusRequester = FocusRequester(),
            isLoading = false,
            snackbarHostState = remember { SnackbarHostState() },
            appVersionName = "1.10.0",
            isIllustrationDisplayed = true,
            logoRes = OSDrawable.ic_onesafe_text,
            isBeta = false,
            onCreateNewSafe = {},
        )
    }
}
