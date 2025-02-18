package studio.lunabee.onesafe.feature.enterpassword

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.accessibility.rememberOSAccessibilityState
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.button.OSFilledButton
import studio.lunabee.onesafe.atom.textfield.OSTextField
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.action.VisibilityTrailingAction
import studio.lunabee.onesafe.commonui.bottomsheet.BottomSheetHolder
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.model.TopAppBarOptionNav
import studio.lunabee.onesafe.molecule.OSTopAppBar
import studio.lunabee.onesafe.organism.card.OSCustomCard
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem
import studio.lunabee.onesafe.ui.theme.OSTheme
import studio.lunabee.onesafe.ui.theme.OSTypography
import studio.lunabee.onesafe.utils.OsDefaultPreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnterPasswordBottomSheet(
    isVisible: Boolean,
    onBottomSheetClosed: () -> Unit,
    onConfirm: () -> Unit,
    viewModel: EnterPasswordViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val localFocusManager = LocalFocusManager.current

    LaunchedEffect(uiState) {
        if (uiState.screenResult == EnterPasswordScreenResultState.Success) {
            viewModel.setPassword("")
            viewModel.resetScreenResultState()
            onConfirm()
        }
    }

    BottomSheetHolder(
        isVisible = isVisible,
        onBottomSheetClosed = onBottomSheetClosed,
        bottomOverlayBrush = LocalDesignSystem.current.navBarOverlayBackgroundGradientBrush,
        skipPartiallyExpanded = true,
    ) { closeBottomSheet, paddingValues ->
        EnterPasswordBottomSheetContent(
            uiState = uiState,
            isVisible = isVisible,
            closeBottomSheet = closeBottomSheet,
            setPassword = viewModel::setPassword,
            resetScreenResultState = viewModel::resetScreenResultState,
            checkPasswordIsCorrect = viewModel::checkPasswordIsCorrect,
            localFocusManager = localFocusManager,
            paddingValues = paddingValues,
        )
    }
}

@Composable
fun EnterPasswordBottomSheetContent(
    uiState: EnterPasswordUiState,
    isVisible: Boolean,
    closeBottomSheet: () -> Unit,
    setPassword: (String) -> Unit,
    resetScreenResultState: () -> Unit,
    checkPasswordIsCorrect: (String) -> Unit,
    localFocusManager: FocusManager,
    paddingValues: PaddingValues,
) {
    val accessibilityState = rememberOSAccessibilityState()
    val bottomSheetHeight = LocalConfiguration.current.screenHeightDp.dp +
        WindowInsets.systemBars.asPaddingValues().calculateBottomPadding() -
        OSDimens.SystemSpacing.Regular
    val background = LocalDesignSystem.current.backgroundGradient()
    val alreadyRequestedFocus = rememberSaveable { mutableStateOf(false) }

    val isConfirmEnabled = remember(uiState.password, uiState.screenResult) {
        uiState.password.isNotEmpty() && uiState.screenResult == EnterPasswordScreenResultState.Idle
    }
    var isPasswordVisible by remember { mutableStateOf(false) }
    val focusRequester = FocusRequester()

    Column(
        modifier = Modifier
            .testTag(UiConstants.TestTag.BottomSheet.EnterPasswordBottomSheet)
            .height(bottomSheetHeight)
            .fillMaxWidth()
            .drawBehind { drawRect(brush = background) }
            .padding(paddingValues),
    ) {
        if (!accessibilityState.isTouchExplorationEnabled) {
            OSTopAppBar(
                options = listOf(
                    TopAppBarOptionNav(
                        image = OSImageSpec.Drawable(OSDrawable.ic_arrow_down),
                        contentDescription = LbcTextSpec.StringResource(OSString.common_accessibility_back),
                        onClick = {
                            localFocusManager.clearFocus()
                            closeBottomSheet()
                        },
                        state = OSActionState.Enabled,
                    ),
                ),
            )
        }
        OSCustomCard(
            content = {
                OSTextField(
                    value = uiState.password,
                    label = LbcTextSpec.StringResource(id = OSString.backup_protectBackup_passwordCard_passwordInputLabel),
                    onValueChange = {
                        resetScreenResultState()
                        setPassword(it)
                    },
                    placeholder = null,
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .fillMaxWidth()
                        .padding(top = OSDimens.SystemSpacing.ExtraSmall, bottom = OSDimens.SystemSpacing.Regular)
                        .padding(horizontal = OSDimens.SystemSpacing.Regular),
                    inputTextStyle = LocalTextStyle.current.copy(fontFamily = OSTypography.Legibility),
                    errorLabel = LbcTextSpec.StringResource(id = OSString.login_passwordField_errorMessage),
                    visualTransformation = if (isPasswordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingAction = {
                        VisibilityTrailingAction(
                            isSecuredVisible = isPasswordVisible,
                            onClick = { isPasswordVisible = !isPasswordVisible },
                            contentDescription = null,
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = if (isConfirmEnabled) {
                        KeyboardActions(onDone = { checkPasswordIsCorrect(uiState.password) })
                    } else {
                        KeyboardActions.Default
                    },
                    isError = uiState.screenResult == EnterPasswordScreenResultState.Error,
                )

                if (uiState.screenResult == EnterPasswordScreenResultState.Loading) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .padding(horizontal = OSDimens.SystemSpacing.Regular)
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.large),
                    )
                }
            },
            modifier = Modifier.padding(
                horizontal = OSDimens.SystemSpacing.Regular,
                vertical = OSDimens.SystemSpacing.Regular,
            ),
            title = LbcTextSpec.StringResource(OSString.settings_passwordVerification_title),
        )

        OSFilledButton(
            onClick = { checkPasswordIsCorrect(uiState.password) },
            modifier = Modifier
                .align(Alignment.End)
                .padding(horizontal = OSDimens.SystemSpacing.Regular),
            text = LbcTextSpec.StringResource(OSString.common_confirm),
            state = if (isConfirmEnabled) OSActionState.Enabled else OSActionState.Disabled,
        )
    }

    LaunchedEffect(isVisible) {
        if (!isVisible) {
            setPassword("")
            resetScreenResultState()
        }
        if (isVisible && !alreadyRequestedFocus.value) {
            focusRequester.requestFocus()
            alreadyRequestedFocus.value = true
        } else {
            alreadyRequestedFocus.value = false
            localFocusManager.clearFocus()
        }
    }
}

@Composable
@OsDefaultPreview
fun EnterPasswordBottomSheetContentPreview() {
    OSTheme {
        val localFocusManager = LocalFocusManager.current

        EnterPasswordBottomSheetContent(
            uiState = EnterPasswordUiState("abc", EnterPasswordScreenResultState.Success),
            isVisible = true,
            closeBottomSheet = {},
            setPassword = {},
            resetScreenResultState = {},
            checkPasswordIsCorrect = {},
            localFocusManager = localFocusManager,
            paddingValues = PaddingValues(0.dp),
        )
    }
}
