package studio.lunabee.onesafe.feature.fileviewer.mediaplayer

import android.content.Context
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@Composable
fun rememberMediaPlayerState(
    context: Context = LocalContext.current,
): MediaPlayerState = remember {
    MediaPlayerStateImpl(
        player = ExoPlayer.Builder(context).build(),
    ).also {
        it.player.addListener(it)
    }
}

class MediaPlayerStateImpl(
    override val player: ExoPlayer,
) : MediaPlayerState, Player.Listener {
    override val isPlaying: MutableState<Boolean> = mutableStateOf(player.isPlaying)
    override val playerState: MutableState<Int> = mutableIntStateOf(player.playbackState)
    override val isControllerVisible: MutableState<Boolean> = mutableStateOf(false)

    override val controllerVisibilityListener: PlayerView.ControllerVisibilityListener = PlayerView.ControllerVisibilityListener {
        this.isControllerVisible.value = it == View.VISIBLE
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        this.isPlaying.value = isPlaying
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        this.playerState.value = playbackState
    }
}
