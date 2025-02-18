package studio.lunabee.onesafe.molecule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.ui.res.OSColorValue

@Composable
fun OSPlaceHolder(
    placeholderName: LbcTextSpec,
    elementSize: Dp,
    placeholderColor: Color,
    placeholderTextStyle: TextStyle,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = Modifier
            .size(size = elementSize)
            .clip(shape = CircleShape)
            .background(color = placeholderColor)
            .then(other = modifier),
        contentAlignment = Alignment.Center,
    ) {
        OSText(
            text = placeholderName,
            color = if (placeholderColor.luminance() >= 0.5f) OSColorValue.Black else OSColorValue.White,
            style = placeholderTextStyle,
        )
    }
}
