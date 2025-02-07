package studio.lunabee.onesafe.feature.camera.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.atom.OSRegularSpacer
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.feature.camera.model.RecordTimerInfo
import studio.lunabee.onesafe.feature.camera.model.RecordingState
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalColorPalette
import studio.lunabee.onesafe.utils.OsDefaultPreview
import java.util.Locale
import kotlin.time.Duration.Companion.seconds

@Composable
fun TimerCell(
    recordingState: RecordingState,
    timerInfo: RecordTimerInfo,
) {
    Row(
        modifier = Modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(OSDimens.Camera.recordIndicatorSize)
                .clip(CircleShape)
                .background(LocalColorPalette.current.Recording),
        )
        OSRegularSpacer()
        OSText(
            text = LbcTextSpec.StringResource(
                when (recordingState) {
                    RecordingState.Recording -> OSString.cameraScreen_timer_recording
                    RecordingState.Paused, RecordingState.Idle -> OSString.cameraScreen_timer_paused
                },
                String.format(
                    Locale.getDefault(),
                    "%02d:%02d",
                    timerInfo.timer.inWholeMinutes,
                    timerInfo.timer.inWholeSeconds % 60,
                ),
                LbcTextSpec.StringResource(timerInfo.fileSizeInfo.second, timerInfo.fileSizeInfo.first).string,
            ),
            style = LocalTextStyle.current.copy(fontFeatureSettings = AppConstants.FontFeature.mono),
            color = Color.White,
        )
    }
}

@Composable
@OsDefaultPreview
private fun TimerCellPreview() {
    TimerCell(
        RecordingState.Recording,
        RecordTimerInfo(123.seconds, "10" to OSString.fileSize_mega),
    )
}
