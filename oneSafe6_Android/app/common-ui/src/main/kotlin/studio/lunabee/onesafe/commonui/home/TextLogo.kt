package studio.lunabee.onesafe.commonui.home

import androidx.compose.foundation.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import studio.lunabee.onesafe.commonui.OSDrawable

@Composable
fun TextLogo(
    modifier: Modifier = Modifier,
) {
    Image(
        painter = painterResource(id = OSDrawable.ic_onesafe_text),
        contentDescription = null,
        modifier = modifier,
        colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.primary),
    )
}
