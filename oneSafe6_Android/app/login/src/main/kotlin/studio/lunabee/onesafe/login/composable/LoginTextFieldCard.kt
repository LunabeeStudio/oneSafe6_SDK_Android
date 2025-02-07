package studio.lunabee.onesafe.login.composable

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.accessibility.accessibilityMergeDescendants
import studio.lunabee.onesafe.atom.OSRegularSpacer
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.atom.textfield.OSTextField
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.action.VisibilityTrailingAction
import studio.lunabee.onesafe.login.screen.LoginScreenLabels
import studio.lunabee.onesafe.organism.card.OSCustomCard
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens

@Composable
fun LoginTextFieldCard(
    modifier: Modifier = Modifier,
    labels: LoginScreenLabels,
    passwordValue: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    focusRequester: FocusRequester,
    isError: Boolean,
    onConfirm: () -> Unit,
    isLoading: Boolean,
) {
    var isPasswordValueVisible by remember { mutableStateOf(false) }
    val alreadyRequestedFocus = rememberSaveable { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    OSCustomCard(
        content = {
            labels.message?.let { message ->
                OSText(
                    text = message,
                    modifier = Modifier.padding(horizontal = OSDimens.SystemSpacing.Regular),
                )
                OSRegularSpacer()
            }
            OSTextField(
                textFieldValue = passwordValue,
                label = LbcTextSpec.StringResource(OSString.signInScreen_form_password_title),
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(UiConstants.TestTag.Item.LoginPasswordTextField)
                    .padding(horizontal = OSDimens.SystemSpacing.Regular)
                    .focusRequester(focusRequester)
                    .onPlaced {
                        if (!alreadyRequestedFocus.value) {
                            focusRequester.requestFocus()
                            alreadyRequestedFocus.value = true
                        }
                    },
                placeholder = null,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
                isError = isError,
                errorLabel = LbcTextSpec.StringResource(OSString.signInScreen_form_password_error),
                maxLines = 1,
                keyboardActions = KeyboardActions {
                    focusManager.clearFocus()
                    onConfirm()
                },
                visualTransformation = if (isPasswordValueVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
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
            if (isLoading) {
                OSRegularSpacer()
                LinearProgressIndicator(
                    modifier = Modifier
                        .padding(horizontal = OSDimens.SystemSpacing.Regular)
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.large),
                )
            }
        },
        modifier = modifier.accessibilityMergeDescendants(),
        title = labels.title,
    )
}
