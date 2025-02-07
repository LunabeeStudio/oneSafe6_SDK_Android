package studio.lunabee.onesafe.feature.fileviewer.mediaplayer

import androidx.compose.runtime.State
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

interface MediaPlayerState {
    val player: ExoPlayer
    val isPlaying: State<Boolean>
    val playerState: State<Int>
    val isControllerVisible: State<Boolean>
    val controllerVisibilityListener: PlayerView.ControllerVisibilityListener
}
