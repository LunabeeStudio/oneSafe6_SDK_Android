package studio.lunabee.onesafe.feature.itemdetails

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import studio.lunabee.onesafe.commonui.action.topAppBarOptionNavBack
import studio.lunabee.onesafe.model.OSTopAppBarOption
import studio.lunabee.onesafe.molecule.OSTopAppBar
import studio.lunabee.onesafe.ui.theme.OSTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun ItemDetailsTopBar(
    navigateBack: (() -> Unit),
    modifier: Modifier = Modifier,
    editOption: OSTopAppBarOption? = null,
    options: List<OSTopAppBarOption> = listOfNotNull(
        topAppBarOptionNavBack(navigateBack),
        editOption,
    ),
) {
    OSTopAppBar(
        modifier = modifier,
        options = options,
    )
}

@OsDefaultPreview
@Composable
private fun ItemDetailsTopBarPreview() {
    OSTheme {
        ItemDetailsTopBar(
            navigateBack = {},
        )
    }
}
