package studio.lunabee.onesafe.help

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import studio.lunabee.onesafe.commonui.OSLoadingView

@Composable
fun HelpRoute() {
    val navHostController = rememberNavController()

    HelpScreen(
        navHostController = navHostController,
    )
}

@Composable
private fun HelpScreen(
    navHostController: NavHostController,
) {
    val containerColor = MaterialTheme.colorScheme.background
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = containerColor,
    ) {
        Box(Modifier.fillMaxSize()) {
            HelpNavGraph(navController = navHostController)
            OSLoadingView()
        }
    }
}
