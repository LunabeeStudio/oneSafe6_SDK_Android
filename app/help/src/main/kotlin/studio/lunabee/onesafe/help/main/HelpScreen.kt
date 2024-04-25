package studio.lunabee.onesafe.help.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import studio.lunabee.onesafe.commonui.OSLoadingView

@Composable
fun HelpRoute(
    navController: NavHostController,
    navToMain: () -> Unit,
    showOverrideBackupDialog: Boolean,
    viewModel: HelpViewModel = hiltViewModel(),
) {
    var showDialog by remember { mutableStateOf(showOverrideBackupDialog) }
    if (showDialog) {
        OverrideWithBackupDialog(
            onValid = {
                viewModel.eraseMainStorage()
                showDialog = false
            },
            onCancel = { showDialog = false },
        )
    }

    val uiState: HelpScreenUiState by viewModel.uiState.collectAsStateWithLifecycle()
    if (uiState == HelpScreenUiState.Restart) {
        navToMain()
    }

    HelpScreen(
        navHostController = navController,
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
