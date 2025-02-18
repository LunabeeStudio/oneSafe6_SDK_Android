package studio.lunabee.onesafe.feature.favorite

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.action.topAppBarOptionNavBack
import studio.lunabee.onesafe.molecule.OSTopAppBar
import studio.lunabee.onesafe.ui.theme.OSTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun FavoriteTopBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OSTopAppBar(
        title = LbcTextSpec.StringResource(id = OSString.common_favorites),
        modifier = modifier,
        options = listOf(topAppBarOptionNavBack(onBackClick)),
    )
}

@OsDefaultPreview
@Composable
private fun FavoriteTopBarPreview() {
    OSTheme {
        FavoriteTopBar(
            onBackClick = {},
        )
    }
}
