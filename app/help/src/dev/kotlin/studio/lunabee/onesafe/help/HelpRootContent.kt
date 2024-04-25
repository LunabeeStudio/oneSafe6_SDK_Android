package studio.lunabee.onesafe.help

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import studio.lunabee.onesafe.help.debug.HelpRootDrawer

@Composable
fun HelpRootContent(
    content: @Composable (NavHostController) -> Unit,
) {
    val navController = rememberNavController()
    HelpRootDrawer(
        navController = navController,
    ) {
        content(navController)
    }
}
