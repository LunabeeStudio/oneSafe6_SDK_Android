package studio.lunabee.onesafe.feature.password.creation

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.animation.OSTopBarAnimatedVisibility
import studio.lunabee.onesafe.animation.rememberOSTopBarVisibilityNestedScrollConnection
import studio.lunabee.onesafe.atom.OSLinearProgress
import studio.lunabee.onesafe.atom.OSRegularSpacer
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.atom.OSSmallSpacer
import studio.lunabee.onesafe.atom.button.OSFilledButton
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.atom.textfield.OSTextField
import studio.lunabee.onesafe.common.extensions.label
import studio.lunabee.onesafe.common.extensions.requestFocusSafely
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.action.VisibilityTrailingAction
import studio.lunabee.onesafe.commonui.dialog.DefaultAlertDialog
import studio.lunabee.onesafe.commonui.dialog.DialogState
import studio.lunabee.onesafe.domain.model.password.PasswordStrength
import studio.lunabee.onesafe.feature.onboarding.composable.VaultCreationTopBar
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.molecule.OSTopImageBox
import studio.lunabee.onesafe.organism.card.OSCustomCard
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewOnSurfaceTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun OnboardingPasswordCreationRoute(
    navigateBack: () -> Unit,
    onConfirm: () -> Unit,
): Unit = PasswordCreationRoute(labels = PasswordCreationScreenLabels.Onboarding, navigateBack = navigateBack, onConfirm = onConfirm)

@Composable
fun ChangePasswordPasswordCreationRoute(
    navigateBack: () -> Unit,
    onConfirm: () -> Unit,
): Unit = PasswordCreationRoute(labels = PasswordCreationScreenLabels.ChangePassword, navigateBack = navigateBack, onConfirm = onConfirm)

@Composable
fun MultiSafePasswordCreationRoute(
    navigateBack: () -> Unit,
    onConfirm: () -> Unit,
): Unit = PasswordCreationRoute(labels = PasswordCreationScreenLabels.MultiSafe, navigateBack = navigateBack, onConfirm = onConfirm)

@Composable
private fun PasswordCreationRoute(
    labels: PasswordCreationScreenLabels,
    navigateBack: () -> Unit,
    onConfirm: () -> Unit,
    viewModel: PasswordCreationViewModel = hiltViewModel(),
) {
    var passwordValue: String by remember { mutableStateOf("") }
    val isConfirmEnabled: Boolean by remember { derivedStateOf { passwordValue.isNotBlank() } }

    LaunchedEffect(Unit) {
        viewModel.resetCryptoCreation()
    }

    val dialogState: DialogState? by viewModel.dialogState.collectAsStateWithLifecycle()
    val uiState: PasswordCreationUiState by viewModel.uiState.collectAsStateWithLifecycle()
    dialogState?.DefaultAlertDialog()

    (uiState as? PasswordCreationUiState.Success)?.let { state ->
        LaunchedEffect(Unit) {
            onConfirm()
            state.reset(passwordValue)
        }
    }

    PasswordCreationScreen(
        labels = labels,
        navigateBack = navigateBack,
        confirmClick = {
            viewModel.createMasterKey(
                password = passwordValue,
            )
        },
        isConfirmEnabled = isConfirmEnabled && uiState is PasswordCreationUiState.Idle,
        passwordValue = passwordValue,
        passwordLevelText = (uiState as? PasswordCreationUiState.Idle)?.passwordStrength,
        onValueChange = {
            passwordValue = it
            viewModel.resetUiState(passwordValue)
        },
        isLoading = uiState == PasswordCreationUiState.Loading,
        error = (uiState as? PasswordCreationUiState.Error)?.text,
    )
}

@Composable
fun PasswordCreationScreen(
    labels: PasswordCreationScreenLabels,
    navigateBack: () -> Unit,
    confirmClick: () -> Unit,
    isConfirmEnabled: Boolean,
    passwordValue: String,
    onValueChange: (String) -> Unit,
    passwordLevelText: LbcTextSpec?,
    isLoading: Boolean,
    error: LbcTextSpec?,
) {
    val scrollState: ScrollState = rememberScrollState()
    val nestedScrollConnection = rememberOSTopBarVisibilityNestedScrollConnection(scrollState)
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val alreadyRequestedFocus = rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!alreadyRequestedFocus.value) {
            focusRequester.requestFocusSafely()
            alreadyRequestedFocus.value = true
        }
    }

    OSScreen(
        testTag = UiConstants.TestTag.Screen.PasswordCreation,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom)),
    ) {
        Column(
            modifier = Modifier
                .imePadding()
                .nestedScroll(nestedScrollConnection)
                .verticalScroll(scrollState),
        ) {
            OSTopImageBox(
                imageRes = OSDrawable.character_jamy_hide,
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
                        labels.description?.let { description ->
                            OSText(
                                text = description,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = OSDimens.SystemSpacing.Regular),
                            )
                        }
                        OSRegularSpacer()
                        PasswordVerificationBlock(
                            modifier = Modifier.padding(horizontal = OSDimens.SystemSpacing.Regular),
                            passwordValue = passwordValue,
                            onValueChange = onValueChange,
                            focusRequester = focusRequester,
                            passwordLevelText = passwordLevelText,
                            error = error,
                            fieldLabel = labels.fieldLabel,
                        ) {
                            if (isConfirmEnabled) {
                                focusManager.clearFocus()
                                confirmClick()
                            }
                        }
                        if (isLoading) {
                            OSRegularSpacer()
                            OSLinearProgress(
                                modifier = Modifier
                                    .padding(horizontal = OSDimens.SystemSpacing.Regular)
                                    .fillMaxWidth(),
                                progress = null,
                                progressDescription = labels.loading,
                            )
                        }
                    },
                    title = labels.title,
                )
            }
            OSFilledButton(
                text = LbcTextSpec.StringResource(OSString.common_confirm),
                onClick = {
                    focusManager.clearFocus()
                    confirmClick()
                },
                state = if (isConfirmEnabled) OSActionState.Enabled else OSActionState.Disabled,
                modifier = Modifier
                    .padding(horizontal = OSDimens.SystemSpacing.Regular)
                    .align(Alignment.End),
            )
            Spacer(modifier = Modifier.size(OSDimens.SystemSpacing.Regular))
        }

        OSTopBarAnimatedVisibility(visible = nestedScrollConnection.isTopBarVisible) {
            VaultCreationTopBar(
                modifier = Modifier
                    .statusBarsPadding()
                    .align(Alignment.TopCenter),
                navigateBack = navigateBack,
            )
        }
    }
}

@Composable
private fun PasswordVerificationBlock(
    modifier: Modifier = Modifier,
    fieldLabel: LbcTextSpec,
    passwordValue: String,
    onValueChange: (String) -> Unit,
    focusRequester: FocusRequester,
    passwordLevelText: LbcTextSpec?,
    error: LbcTextSpec?,
    onImeDone: () -> Unit,
) {
    var isPasswordValueVisible by remember { mutableStateOf(false) }
    Column(modifier.fillMaxWidth()) {
        OSTextField(
            value = passwordValue,
            label = fieldLabel,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(UiConstants.TestTag.Item.PasswordCreationTextField)
                .focusRequester(focusRequester),
            placeholder = null,
            keyboardOptions = KeyboardOptions(
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            maxLines = 1,
            isError = error != null,
            keyboardActions = KeyboardActions {
                onImeDone()
            },
            visualTransformation = if (isPasswordValueVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingAction = {
                VisibilityTrailingAction(
                    isSecuredVisible = isPasswordValueVisible,
                    onClick = { isPasswordValueVisible = !isPasswordValueVisible },
                    contentDescription = LbcTextSpec.StringResource(
                        if (isPasswordValueVisible) {
                            OSString.safeItemDetail_contentCard_informations_accessibility_securedValue_actionHide
                        } else {
                            OSString.safeItemDetail_contentCard_informations_accessibility_securedValue_actionShow
                        },
                    ),
                )
            },
            errorLabel = error,
        )
        OSSmallSpacer()
        passwordLevelText?.let {
            PasswordLevel(
                it,
            )
        }
    }
}

@Composable
private fun PasswordLevel(
    passwordLevelText: LbcTextSpec,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        OSText(
            text = passwordLevelText,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@OsDefaultPreview
@Composable
private fun PasswordCreationScreenPreview() {
    OSPreviewOnSurfaceTheme {
        PasswordCreationScreen(
            labels = PasswordCreationScreenLabels.Onboarding,
            navigateBack = {},
            confirmClick = {},
            isConfirmEnabled = true,
            passwordValue = "pass",
            onValueChange = {},
            passwordLevelText = PasswordStrength.Strong.label(),
            isLoading = false,
            error = null,
        )
    }
}

@OsDefaultPreview
@Composable
private fun PasswordCreationScreenMultiSafePreview() {
    OSPreviewOnSurfaceTheme {
        PasswordCreationScreen(
            labels = PasswordCreationScreenLabels.MultiSafe,
            navigateBack = {},
            confirmClick = {},
            isConfirmEnabled = true,
            passwordValue = "pass",
            onValueChange = {},
            passwordLevelText = PasswordStrength.Strong.label(),
            isLoading = false,
            error = null,
        )
    }
}

@OsDefaultPreview
@Composable
private fun PasswordCreationScreenLoadingPreview() {
    OSPreviewOnSurfaceTheme {
        PasswordCreationScreen(
            labels = PasswordCreationScreenLabels.Onboarding,
            navigateBack = {},
            confirmClick = {},
            isConfirmEnabled = false,
            passwordValue = "pass",
            onValueChange = {},
            passwordLevelText = null,
            isLoading = true,
            error = null,
        )
    }
}
