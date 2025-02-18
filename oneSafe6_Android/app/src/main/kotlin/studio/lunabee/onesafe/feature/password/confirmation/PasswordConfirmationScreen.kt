package studio.lunabee.onesafe.feature.password.confirmation

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.animation.OSTopBarAnimatedVisibility
import studio.lunabee.onesafe.animation.rememberOSTopBarVisibilityNestedScrollConnection
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.atom.button.OSFilledButton
import studio.lunabee.onesafe.atom.textfield.OSTextField
import studio.lunabee.onesafe.common.extensions.requestFocusSafely
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.action.VisibilityTrailingAction
import studio.lunabee.onesafe.commonui.error.description
import studio.lunabee.onesafe.error.OSDomainError
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.feature.onboarding.composable.VaultCreationTopBar
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.organism.card.OSTopImageLoadingCard
import studio.lunabee.onesafe.organism.card.param.OSCardImageParam
import studio.lunabee.onesafe.organism.card.param.OSCardProgressParam
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewOnSurfaceTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun PasswordConfirmationScreen(
    labels: PasswordConfirmationScreenLabels,
    navigateBack: () -> Unit,
    confirmClick: () -> Unit,
    isConfirmEnabled: Boolean,
    passwordValue: String,
    onValueChange: (String) -> Unit,
    isLoading: Boolean,
    fieldError: OSError?,
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
        testTag = UiConstants.TestTag.Screen.PasswordConfirmation,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom)),
    ) {
        Column(
            modifier = Modifier
                .imePadding()
                .nestedScroll(nestedScrollConnection)
                .verticalScroll(scrollState),
        ) {
            val progress = if (isLoading) {
                OSCardProgressParam.UndeterminedProgress(labels.loading)
            } else {
                null
            }
            val cardImage = OSCardImageParam(OSDrawable.character_jamy_hide, null)
            OSTopImageLoadingCard(
                title = labels.title,
                description = labels.description,
                cardProgress = progress,
                cardImage = cardImage,
                modifier = Modifier
                    .padding(horizontal = OSDimens.SystemSpacing.Regular)
                    .padding(
                        top = OSDimens.SystemSpacing.Regular + OSDimens.ItemTopBar.Height,
                        bottom = OSDimens.SystemSpacing.Regular,
                    ),
            ) {
                PasswordField(
                    passwordValue = passwordValue,
                    onValueChange = onValueChange,
                    focusRequester = focusRequester,
                    fieldError = fieldError,
                    onImeDone = {
                        if (isConfirmEnabled) {
                            focusManager.clearFocus()
                            confirmClick()
                        }
                    },
                    fieldLabel = labels.fieldLabel,
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
private fun PasswordField(
    passwordValue: String,
    onValueChange: (String) -> Unit,
    focusRequester: FocusRequester,
    fieldError: OSError?,
    onImeDone: () -> Unit,
    fieldLabel: LbcTextSpec,
) {
    var isPasswordValueVisible by remember { mutableStateOf(false) }
    OSTextField(
        value = passwordValue,
        label = fieldLabel,
        onValueChange = onValueChange,
        modifier = Modifier
            .testTag(UiConstants.TestTag.Item.PasswordConfirmationTextField)
            .padding(horizontal = OSDimens.SystemSpacing.Regular)
            .fillMaxWidth()
            .focusRequester(focusRequester),
        placeholder = null,
        keyboardOptions = KeyboardOptions(
            autoCorrectEnabled = false,
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
        ),
        isError = fieldError != null,
        errorLabel = fieldError?.description(),
        maxLines = 1,
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
    )
}

@OsDefaultPreview
@Composable
private fun PasswordConfirmationScreenPreview() {
    OSPreviewOnSurfaceTheme {
        PasswordConfirmationScreen(
            labels = PasswordConfirmationScreenLabels.Onboarding,
            navigateBack = {},
            confirmClick = {},
            isConfirmEnabled = true,
            passwordValue = "pass",
            onValueChange = {},
            isLoading = false,
            fieldError = null,
        )
    }
}

@OsDefaultPreview
@Composable
private fun PasswordConfirmationScreenLoadingPreview() {
    OSPreviewOnSurfaceTheme {
        PasswordConfirmationScreen(
            labels = PasswordConfirmationScreenLabels.ChangePassword,
            navigateBack = {},
            confirmClick = {},
            isConfirmEnabled = false,
            passwordValue = "pass",
            onValueChange = {},
            isLoading = true,
            fieldError = null,
        )
    }
}

@OsDefaultPreview
@Composable
private fun PasswordConfirmationScreenErrorPreview() {
    OSPreviewOnSurfaceTheme {
        PasswordConfirmationScreen(
            labels = PasswordConfirmationScreenLabels.Onboarding,
            navigateBack = {},
            confirmClick = {},
            isConfirmEnabled = false,
            passwordValue = "pass",
            onValueChange = {},
            isLoading = false,
            fieldError = OSDomainError(OSDomainError.Code.WRONG_CONFIRMATION_PASSWORD),
        )
    }
}
