package studio.lunabee.onesafe.feature.search.composable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.atom.textfield.OSBasicTextField
import studio.lunabee.onesafe.commonui.OSPlurals
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun SearchTextField(
    textFieldValue: TextFieldValue,
    focusRequester: FocusRequester,
    focusManager: FocusManager,
    itemCount: Int,
    onValueChange: (TextFieldValue, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.onBackground)
    OSBasicTextField(
        textStyle = textStyle,
        value = textFieldValue,
        modifier = modifier
            .padding(OSDimens.SystemSpacing.Regular)
            .testTag(UiConstants.TestTag.Item.SearchTextField)
            .focusRequester(focusRequester),
        onValueChange = {
            onValueChange(it, false)
        },
        keyboardActions = KeyboardActions(
            onSearch = {
                focusManager.clearFocus()
                onValueChange(textFieldValue, true)
            },
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        decorationBox = { innerTextField ->
            Box {
                if (textFieldValue.text.isEmpty()) {
                    OSText(
                        style = textStyle,
                        color = MaterialTheme.colorScheme.onBackground,
                        text = LbcTextSpec.PluralsResource(
                            id = OSPlurals.searchScreen_search_placeholder,
                            itemCount,
                            itemCount,
                        ),
                        modifier = Modifier.align(Alignment.BottomStart),
                    )
                }
                innerTextField()
            }
        },
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
    )
}

@OsDefaultPreview
@Composable
fun SearchTextFieldPreview() {
    OSTheme {
        SearchTextField(
            textFieldValue = TextFieldValue(),
            focusRequester = FocusRequester(),
            focusManager = LocalFocusManager.current,
            onValueChange = { _, _ -> },
            itemCount = 20,
        )
    }
}
