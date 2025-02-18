package studio.lunabee.onesafe.feature.exportbackup.auth

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.animation.OSTopBarAnimatedVisibility
import studio.lunabee.onesafe.animation.rememberOSTopBarVisibilityNestedScrollConnection
import studio.lunabee.onesafe.atom.OSRegularSpacer
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.atom.OSSmallSpacer
import studio.lunabee.onesafe.atom.button.OSFilledButton
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.atom.textfield.OSTextField
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.action.VisibilityTrailingAction
import studio.lunabee.onesafe.commonui.action.topAppBarOptionNavBack
import studio.lunabee.onesafe.commonui.extension.markdownToAnnotatedString
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.molecule.OSTopAppBar
import studio.lunabee.onesafe.organism.card.OSLoadingCard
import studio.lunabee.onesafe.organism.card.OSMessageCard
import studio.lunabee.onesafe.organism.card.param.OSCardProgressParam
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSTheme
import studio.lunabee.onesafe.ui.theme.OSTypography

@Composable
fun ExportAuthScreen(
    itemCount: Int,
    contactCount: Int,
    isCheckingPassword: Boolean,
    resetCredentialsError: (() -> Unit)?,
    checkPassword: (password: String) -> Unit,
    navigateBack: () -> Unit,
) {
    OSScreen(
        testTag = UiConstants.TestTag.Screen.ExportAuthScreen,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
            .imePadding(),
    ) {
        val scrollState = rememberScrollState()
        val nestedScrollConnection = rememberOSTopBarVisibilityNestedScrollConnection(scrollState)

        var passwordTextFieldValue: String by remember { mutableStateOf(value = "") }
        var isPasswordTextFieldValueVisible: Boolean by remember { mutableStateOf(value = false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection)
                .verticalScroll(scrollState)
                .padding(
                    top = OSDimens.ItemTopBar.Height + OSDimens.SystemSpacing.Large,
                    start = OSDimens.SystemSpacing.Regular,
                    end = OSDimens.SystemSpacing.Regular,
                    bottom = OSDimens.SystemSpacing.Large,
                ),
            horizontalAlignment = Alignment.End,
        ) {
            OSMessageCard(
                title = LbcTextSpec.StringResource(OSString.backup_protectBackup_title),
                description = LbcTextSpec.Annotated(
                    stringResource(
                        id = OSString.backup_protectBackup_withBubbles_description,
                        itemCount,
                        contactCount,
                    ).markdownToAnnotatedString(),
                ),
                modifier = Modifier
                    .fillMaxWidth(),
            )

            OSRegularSpacer()

            OSLoadingCard(
                title = null,
                description = LbcTextSpec.StringResource(OSString.backup_protectBackup_passwordCard_description),
                cardProgress = OSCardProgressParam.UndeterminedProgress().takeIf { isCheckingPassword },
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = OSDimens.SystemSpacing.Regular),
                ) {
                    OSTextField(
                        value = passwordTextFieldValue,
                        label = LbcTextSpec.StringResource(id = OSString.backup_protectBackup_passwordCard_passwordInputLabel),
                        placeholder = LbcTextSpec.StringResource(id = OSString.backup_protectBackup_passwordCard_passwordInputLabel),
                        onValueChange = {
                            passwordTextFieldValue = it
                            if (resetCredentialsError != null) resetCredentialsError()
                        },
                        trailingAction = {
                            VisibilityTrailingAction(
                                isSecuredVisible = isPasswordTextFieldValueVisible,
                                onClick = { isPasswordTextFieldValueVisible = !isPasswordTextFieldValueVisible },
                                contentDescription = null,
                            )
                        },
                        visualTransformation = if (isPasswordTextFieldValueVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        inputTextStyle = LocalTextStyle.current.copy(fontFamily = OSTypography.Legibility),
                        keyboardOptions = KeyboardOptions(
                            autoCorrectEnabled = false,
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions {
                            if (passwordTextFieldValue.isEmpty()) {
                                defaultKeyboardAction(imeAction = ImeAction.Done) // hide keyboard
                            } else {
                                checkPassword(passwordTextFieldValue)
                                defaultKeyboardAction(imeAction = ImeAction.Done) // hide keyboard
                            }
                        },
                    )

                    AnimatedVisibility(visible = resetCredentialsError != null) {
                        Column {
                            OSRegularSpacer()
                            OSText(
                                text = LbcTextSpec.StringResource(id = OSString.backup_protectBackup_passwordCard_passwordErrorLabel),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }

            OSSmallSpacer()

            OSFilledButton(
                text = LbcTextSpec.StringResource(OSString.common_next),
                onClick = { checkPassword(passwordTextFieldValue) },
                state = OSActionState.Disabled.takeIf {
                    passwordTextFieldValue.isEmpty() || isCheckingPassword
                } ?: OSActionState.Enabled,
            )
        }

        OSTopBarAnimatedVisibility(visible = nestedScrollConnection.isTopBarVisible) {
            OSTopAppBar(
                modifier = Modifier
                    .statusBarsPadding()
                    .align(Alignment.TopCenter),
                options = listOf(topAppBarOptionNavBack(navigateBack)),
            )
        }
    }
}

@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun ExportAuthPreview() {
    OSTheme {
        ExportAuthScreen(
            itemCount = 73,
            contactCount = 10,
            isCheckingPassword = false,
            resetCredentialsError = null,
            checkPassword = { },
            navigateBack = { },
        )
    }
}
