package studio.lunabee.onesafe.help

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

@Composable
fun HelpRootContent(
    content: @Composable (NavHostController) -> Unit,
) {
    content(rememberNavController())
}
