package studio.lunabee.onesafe.feature.fileviewer

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import studio.lunabee.onesafe.common.extensions.getFileSharingIntent
import studio.lunabee.onesafe.feature.fileviewer.mediaplayer.MediaViewerScreen
import studio.lunabee.onesafe.feature.fileviewer.model.FileViewerUiState
import studio.lunabee.onesafe.feature.fileviewer.model.ViewerType
import studio.lunabee.onesafe.feature.fileviewer.screen.ImageViewerScreen
import studio.lunabee.onesafe.feature.fileviewer.screen.LoadingViewerScreen
import studio.lunabee.onesafe.feature.fileviewer.screen.PdfViewerScreen
import studio.lunabee.onesafe.feature.fileviewer.screen.UnknownViewerScreen
import studio.lunabee.onesafe.feature.fileviewer.viewmodel.FileViewerViewModel
import studio.lunabee.onesafe.feature.itemdetails.model.FileFieldAction
import studio.lunabee.onesafe.ui.UiConstants

@Composable
fun FileViewerRoute(
    navigateBack: () -> Unit,
    viewModel: FileViewerViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.loadFile(context)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
    val snackbarState by viewModel.snackbarState.collectAsState(null)
    val snackbarVisuals = snackbarState?.snackbarVisuals
    LaunchedEffect(snackbarState) {
        snackbarVisuals?.let { snackbarHostState.showSnackbar(it) }
    }

    Box {
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .zIndex(UiConstants.SnackBar.ZIndex)
                .align(Alignment.BottomCenter)
                .navigationBarsPadding(),
        )
        when (val safeUiState = uiState) {
            is FileViewerUiState.Error -> UnknownViewerScreen(
                onBackClick = navigateBack,
                name = safeUiState.name.orEmpty(),
                actions = emptyList(),
                text = safeUiState.error,
            )
            is FileViewerUiState.Data -> {
                val saveFileLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.CreateDocument(safeUiState.mimeType),
                ) { uri ->
                    uri?.let { viewModel.saveFile(uri, context) }
                }
                val actions = remember {
                    listOf(
                        FileFieldAction.Share {
                            val shareIntent = context.getFileSharingIntent(
                                fileToShare = safeUiState.file,
                                mimeType = safeUiState.mimeType,
                            )
                            launcher.launch(Intent.createChooser(shareIntent, safeUiState.name))
                        },
                        FileFieldAction.Download {
                            saveFileLauncher.launch(safeUiState.name)
                        },
                    )
                }

                when (safeUiState.viewerType) {
                    ViewerType.Photo -> ImageViewerScreen(
                        onBackClick = navigateBack,
                        uri = Uri.fromFile(safeUiState.file),
                        title = safeUiState.name,
                        actions = actions,
                    )
                    ViewerType.Pdf -> PdfViewerScreen(
                        onBackClick = navigateBack,
                        uri = Uri.fromFile(safeUiState.file),
                        title = safeUiState.name,
                        actions = actions,
                    )
                    ViewerType.Video,
                    ViewerType.Audio,
                    -> MediaViewerScreen(
                        onBackClick = navigateBack,
                        uri = Uri.fromFile(safeUiState.file),
                        title = safeUiState.name,
                        actions = actions,
                    )
                    ViewerType.Unknown -> UnknownViewerScreen(
                        onBackClick = navigateBack,
                        name = safeUiState.name,
                        actions = actions,
                    )
                }
            }
            is FileViewerUiState.Loading -> LoadingViewerScreen(
                name = safeUiState.name,
                onBackClick = navigateBack,
            )
        }
    }
}
