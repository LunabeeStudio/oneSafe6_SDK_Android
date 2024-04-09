package studio.lunabee.onesafe.help.cipherkeyprompt

import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.text.input.VisualTransformation
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.accessibility.accessibilityMergeDescendants
import studio.lunabee.onesafe.atom.OSRegularSpacer
import studio.lunabee.onesafe.atom.OSSmallSpacer
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.atom.textfield.OSTextField
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.action.VisibilityTrailingAction
import studio.lunabee.onesafe.molecule.OSActionButton
import studio.lunabee.onesafe.organism.card.OSCustomCard
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun CipherKeyTextFieldCard(
    keyValue: String,
    onKeyChange: (String) -> Unit,
    focusRequester: FocusRequester,
    errorText: LbcTextSpec?,
    onConfirm: () -> Unit,
    isLoading: Boolean,
    onWhyClick: () -> Unit,
    onLostKeyClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isPasswordValueVisible by remember { mutableStateOf(false) }
    val alreadyRequestedFocus = rememberSaveable { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    OSCustomCard(
        content = {
            OSText(
                text = LbcTextSpec.StringResource(OSString.cipherRecover_keyCard_message),
                modifier = Modifier.padding(horizontal = OSDimens.SystemSpacing.Regular),
            )
            OSRegularSpacer()
            OSTextField(
                value = keyValue,
                label = LbcTextSpec.StringResource(OSString.cipherRecover_keyCard_keyField_label),
                onValueChange = onKeyChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(UiConstants.TestTag.Item.CipherKeyTextField)
                    .padding(horizontal = OSDimens.SystemSpacing.Regular)
                    .focusRequester(focusRequester)
                    .onPlaced {
                        if (!alreadyRequestedFocus.value) {
                            focusRequester.requestFocus()
                            alreadyRequestedFocus.value = true
                        }
                    },
                placeholder = LbcTextSpec.Raw("0x…"),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrect = false,
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
                isError = errorText != null,
                errorLabel = errorText,
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
            OSSmallSpacer()
            OSActionButton(
                text = LbcTextSpec.StringResource(OSString.cipherRecover_keyCard_whyButton),
                contentPadding = PaddingValues(horizontal = OSDimens.SystemSpacing.Regular),
                onClick = onWhyClick,
            ).Composable()
            OSActionButton(
                text = LbcTextSpec.StringResource(OSString.cipherRecover_keyCard_lostKeyButton),
                contentPadding = PaddingValues(horizontal = OSDimens.SystemSpacing.Regular),
                onClick = onLostKeyClick,
            ).Composable()
        },
        modifier = modifier.accessibilityMergeDescendants(),
        title = LbcTextSpec.StringResource(OSString.cipherRecover_keyCard_title),
    )
}

@Composable
@OsDefaultPreview
private fun CipherKeyTextFieldCardPreview() {
    OSPreviewBackgroundTheme {
        CipherKeyTextFieldCard(
            keyValue = "123",
            onKeyChange = {},
            focusRequester = remember { FocusRequester() },
            errorText = null,
            onConfirm = {},
            isLoading = false,
            onWhyClick = {},
            onLostKeyClick = {},
        )
    }
}
