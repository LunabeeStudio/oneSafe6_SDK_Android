package studio.lunabee.onesafe.feature.onboarding.composable

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import studio.lunabee.onesafe.commonui.action.topAppBarOptionNavBack
import studio.lunabee.onesafe.molecule.OSTopAppBar

@Composable
fun VaultCreationTopBar(
    modifier: Modifier = Modifier,
    navigateBack: () -> Unit,
) {
    OSTopAppBar(
        modifier = modifier,
        options = listOf(topAppBarOptionNavBack(navigateBack)),
    )
}
