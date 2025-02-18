package studio.lunabee.onesafe.feature.camera.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSSmallSpacer
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.feature.camera.model.CameraMode
import studio.lunabee.onesafe.ui.res.OSDimens

@Composable
fun CameraModeSelector(
    selectedMode: CameraMode,
    onSelectMode: (CameraMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        Selector(
            text = LbcTextSpec.StringResource(OSString.common_photo),
            isSelected = selectedMode == CameraMode.PHOTO,
            onClick = { onSelectMode(CameraMode.PHOTO) },
        )
        OSSmallSpacer()
        Selector(
            text = LbcTextSpec.StringResource(OSString.common_video),
            isSelected = selectedMode == CameraMode.VIDEO,
            onClick = { onSelectMode(CameraMode.VIDEO) },
        )
    }
}

@Composable
fun Selector(
    text: LbcTextSpec,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    OSText(
        text = text,
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .background(if (isSelected) MaterialTheme.colorScheme.background else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = OSDimens.SystemSpacing.ExtraSmall, horizontal = OSDimens.SystemSpacing.Small),
        color = if (isSelected) {
            MaterialTheme.colorScheme.onBackground
        } else {
            MaterialTheme.colorScheme.background
        },
        style = MaterialTheme.typography.bodySmall,
    )
}
