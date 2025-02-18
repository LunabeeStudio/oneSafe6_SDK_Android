package studio.lunabee.onesafe.feature.search.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.TextFieldValue
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.button.OSIconButton
import studio.lunabee.onesafe.atom.button.defaults.OSIconButtonDefaults
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun DefaultSearchTextFieldContainer(
    textFieldValue: TextFieldValue,
    focusRequester: FocusRequester,
    focusManager: FocusManager,
    itemCount: Int,
    onValueChange: (TextFieldValue, Boolean) -> Unit,
    onClear: () -> Unit,
) {
    Row(
        modifier = Modifier
            .padding(horizontal = OSDimens.SystemSpacing.Regular, vertical = OSDimens.SystemSpacing.Small)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surface),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SearchTextField(
            textFieldValue,
            focusRequester,
            focusManager,
            itemCount,
            onValueChange,
            modifier = Modifier.weight(1f),
        )

        if (textFieldValue.text.isNotBlank()) {
            OSIconButton(
                image = OSImageSpec.Drawable(OSDrawable.ic_baseline_close),
                contentDescription = LbcTextSpec.StringResource(OSString.common_cancel),
                onClick = onClear,
                buttonSize = OSDimens.SystemButtonDimension.Small,
                colors = OSIconButtonDefaults.secondaryIconButtonColors(),
                modifier = Modifier
                    .testTag(tag = UiConstants.TestTag.OSAppBarMenu),
            )
            Spacer(modifier = Modifier.size(OSDimens.SystemSpacing.Small))
        }
    }
}

@OsDefaultPreview
@Composable
fun DefaultSearchTextFieldContainerPreview() {
    OSTheme {
        DefaultSearchTextFieldContainer(
            textFieldValue = TextFieldValue(),
            focusRequester = FocusRequester(),
            focusManager = LocalFocusManager.current,
            onValueChange = { _, _ -> },
            itemCount = 20,
            onClear = {},
        )
    }
}
