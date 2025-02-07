package studio.lunabee.onesafe.feature.fileviewer.mediaplayer

import android.net.Uri
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.MediaItem
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.action.topAppBarOptionNavBack
import studio.lunabee.onesafe.commonui.extension.findFragmentActivity
import studio.lunabee.onesafe.feature.fileviewer.model.fileViewerTopBarAction
import studio.lunabee.onesafe.feature.itemdetails.model.FileFieldAction
import studio.lunabee.onesafe.molecule.OSTopAppBar
import studio.lunabee.onesafe.ui.theme.OSTheme
import studio.lunabee.onesafe.utils.oSDefaultEnableEdgeToEdge

@Composable
fun MediaViewerScreen(
    onBackClick: () -> Unit,
    title: String,
    uri: Uri,
    actions: List<FileFieldAction>,
) {
    val playerState: MediaPlayerState = rememberMediaPlayerState()
    val context = LocalContext.current

    DisposableEffect(Unit) {
        context.findFragmentActivity().enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(scrim = android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(scrim = android.graphics.Color.TRANSPARENT),
        )
        playerState.player.setMediaItem(MediaItem.fromUri(uri))
        playerState.player.prepare()
        playerState.player.playWhenReady = true
        onDispose {
            context.findFragmentActivity().oSDefaultEnableEdgeToEdge()
        }
    }

    OSTheme(
        isSystemInDarkTheme = true,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
        ) {
            MediaPlayer(
                playerState = playerState,
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding(),
            )

            AnimatedVisibility(
                visible = playerState.isControllerVisible.value,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically(),
            ) {
                OSTopAppBar(
                    title = LbcTextSpec.Raw(title),
                    options = listOf(
                        topAppBarOptionNavBack(onBackClick),
                        fileViewerTopBarAction(actions),
                    ),
                    modifier = Modifier.systemBarsPadding(),
                )
            }
        }
    }
}
