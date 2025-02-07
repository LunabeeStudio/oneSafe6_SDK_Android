package studio.lunabee.onesafe.feature.camera.composable

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.MediaItem
import com.lunabee.lbloading.LoadingBackHandler
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.accessibility.rememberOSAccessibilityState
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.feature.fileviewer.mediaplayer.MediaPlayer
import studio.lunabee.onesafe.feature.fileviewer.mediaplayer.MediaPlayerState
import studio.lunabee.onesafe.feature.fileviewer.mediaplayer.rememberMediaPlayerState
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.model.TopAppBarOptionNav
import studio.lunabee.onesafe.model.TopAppBarOptionTrailing
import studio.lunabee.onesafe.molecule.OSTopAppBar
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem
import java.io.File

@Composable
fun VideoPreviewScreen(
    onNavigateBack: () -> Unit,
    onConfirm: () -> Unit,
    isLoading: Boolean,
    file: File,
) {
    val playerState: MediaPlayerState = rememberMediaPlayerState()
    val accessibilityState = rememberOSAccessibilityState()
    val context = LocalContext.current

    LaunchedEffect(file) {
        if (accessibilityState.isAccessibilityEnabled) {
            Toast.makeText(
                context,
                LbcTextSpec.StringResource(OSString.accessibility_camera_recordSuccess)
                    .string(context),
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    LoadingBackHandler {
        if (!isLoading) {
            onNavigateBack()
        }
    }

    LaunchedEffect(isLoading) {
        if (isLoading) {
            playerState.player.pause()
        }
    }

    LaunchedEffect(Unit) {
        playerState.player.setMediaItem(MediaItem.fromUri(Uri.fromFile(file)))
        playerState.player.prepare()
        playerState.player.playWhenReady = true
    }

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
        OSTopAppBar(
            options = listOf(
                TopAppBarOptionNav(
                    image = OSImageSpec.Drawable(OSDrawable.ic_close),
                    contentDescription = LbcTextSpec.StringResource(OSString.common_accessibility_back),
                    onClick = onNavigateBack,
                    state = if (isLoading) OSActionState.Disabled else OSActionState.Enabled,
                ),
                TopAppBarOptionTrailing.primaryIconAction(
                    image = OSImageSpec.Drawable(OSDrawable.ic_check),
                    onClick = onConfirm,
                    state = if (isLoading) OSActionState.Disabled else OSActionState.Enabled,
                ),
            ),
            modifier = Modifier.systemBarsPadding(),
        )
        if (isLoading) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(LocalDesignSystem.current.scrimColor)
                    .clickable(enabled = false, onClick = {}),
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(OSDimens.Camera.loadingSize),
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}
