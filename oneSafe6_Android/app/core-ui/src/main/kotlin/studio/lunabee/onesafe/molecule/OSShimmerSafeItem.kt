package studio.lunabee.onesafe.molecule

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import com.valentinilk.shimmer.LocalShimmerTheme
import com.valentinilk.shimmer.shimmer
import studio.lunabee.onesafe.model.OSSafeItemStyle
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.preview.PreviewGroup
import studio.lunabee.onesafe.ui.theme.OSShimmerTheme
import studio.lunabee.onesafe.ui.theme.OSTheme

@Composable
fun OSShimmerSafeItem(
    style: OSSafeItemStyle,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
) {
    CompositionLocalProvider(
        LocalShimmerTheme provides OSShimmerTheme.Theme,
    ) {
        Column(
            modifier = modifier
                .testTag(UiConstants.TestTag.OSShimmerSafeItem),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val shimmerColor = if (color.isSpecified) color else MaterialTheme.colorScheme.primary

            Box(
                modifier = Modifier
                    .size(size = style.elementSize)
                    .clip(CircleShape)
                    .shimmer()
                    .drawBehind { drawRect(shimmerColor) },
            )

            Spacer(modifier = Modifier.padding(top = style.paddingWithLabel))

            Box(
                modifier = Modifier
                    .defaultMinSize(minWidth = style.elementSize)
                    .fillMaxWidth()
                    .height(
                        with(LocalDensity.current) {
                            style.labelTextStyle.fontSize.toDp()
                        },
                    )
                    .shimmer()
                    .drawBehind { drawRect(shimmerColor) },
            )
        }
    }
}

@Preview(name = "Shimmer safe item", group = PreviewGroup.SafeItem)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Shimmer safe item",
    group = PreviewGroup.SafeItem,
)
@Composable
private fun OSLargeSafeItemPreview() {
    OSTheme {
        OSShimmerSafeItem(
            style = OSSafeItemStyle.Large,
        )
    }
}

@Preview(name = "Shimmer safe item", group = PreviewGroup.SafeItem)
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Shimmer safe item custom color",
    group = PreviewGroup.SafeItem,
)
@Composable
private fun OSGreenLargeSafeItemPreview() {
    OSTheme {
        OSShimmerSafeItem(
            style = OSSafeItemStyle.Large,
            color = Color.Green,
        )
    }
}
