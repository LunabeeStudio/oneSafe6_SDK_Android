package studio.lunabee.onesafe.feature.camera.model

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip

enum class CaptureConfig(val modifier: Modifier = Modifier) {
    ItemIcon(
        Modifier
            .aspectRatio(1f)
            .clip(CircleShape),
    ),
    FieldFile,
}
