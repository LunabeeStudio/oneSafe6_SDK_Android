package studio.lunabee.onesafe.molecule

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem
import kotlin.math.ceil

@Composable
fun OSDropdownMenuItem(
    text: LbcTextSpec,
    @DrawableRes icon: Int?,
    onClick: () -> Unit,
) {
    var textWidth by remember { mutableStateOf<Int?>(null) }
    DropdownMenuItem(
        text = {
            // TODO = Change the layout when fixed
            // Text takes too much place if text is long and prevent image to show
            // It's a known bug
            // https://issuetracker.google.com/issues/206039942
            // https://stackoverflow.com/questions/69941390/how-to-draw-a-border-around-multiline-text-in-compose/69947555#69947555
            OSText(
                text = text,
                onTextLayout = { layoutResult ->
                    textWidth = (0 until layoutResult.lineCount)
                        .maxOf {
                            ceil(layoutResult.getLineRight(it)).toInt()
                        }
                },
                modifier = Modifier
                    .width(with(LocalDensity.current) { textWidth?.toDp() ?: Dp.Unspecified })
                    .drawWithContent {
                        // prevent full with text from being drawn
                        if (textWidth != null) {
                            drawContent()
                        }
                    },
            )
        },
        leadingIcon = icon?.let {
            {
                OSRoundImage(
                    image = OSImageSpec.Drawable(
                        drawable = icon,
                        tintColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    ),
                    systemImageDimension = OSDimens.SystemImageDimension.Small,
                    modifier = Modifier
                        .size(size = OSDimens.SystemButton.ExtraSmall),
                    contentDescription = null,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                )
            }
        },
        colors = LocalDesignSystem.current.menuItemColors,
        onClick = onClick,
    )
}
