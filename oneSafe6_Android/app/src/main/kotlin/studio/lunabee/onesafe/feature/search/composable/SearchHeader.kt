package studio.lunabee.onesafe.feature.search.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.TextFieldValue
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.accessibility.rememberOSAccessibilityState
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.model.TopAppBarOptionNav
import studio.lunabee.onesafe.molecule.OSTopAppBar
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun SearchHeader(
    textFieldValue: TextFieldValue,
    focusRequester: FocusRequester,
    focusManager: FocusManager,
    navigateBack: () -> Unit,
    itemCount: Int,
    onValueChange: (TextFieldValue, Boolean) -> Unit,
    onClear: () -> Unit,
) {
    val accessibilityState = rememberOSAccessibilityState()
    val option = if (!accessibilityState.isTouchExplorationEnabled) {
        listOf(
            TopAppBarOptionNav(
                image = OSImageSpec.Drawable(OSDrawable.ic_arrow_down),
                contentDescription = LbcTextSpec.StringResource(OSString.common_accessibility_back),
                onClick = {
                    focusManager.clearFocus()
                    navigateBack()
                },
                state = OSActionState.Enabled,
            ),
        )
    } else {
        emptyList()
    }
    Column {
        OSTopAppBar(
            title = LbcTextSpec.StringResource(OSString.searchScreen_title),
            options = option,
            modifier = Modifier.padding(top = OSDimens.SystemSpacing.Small),
        )

        DefaultSearchTextFieldContainer(
            textFieldValue,
            focusRequester,
            focusManager,
            itemCount,
            onValueChange,
            onClear,
        )
    }
}

@OsDefaultPreview
@Composable
fun SearchHeaderPreview() {
    OSTheme {
        SearchHeader(
            textFieldValue = TextFieldValue(),
            focusRequester = FocusRequester(),
            focusManager = LocalFocusManager.current,
            navigateBack = {},
            onValueChange = { _, _ -> },
            itemCount = 20,
            onClear = {},
        )
    }
}
