package studio.lunabee.onesafe.molecule

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.shimmer
import studio.lunabee.onesafe.model.OSSafeItemStyle
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme
import studio.lunabee.onesafe.ui.theme.OSTypography.labelXSmall

@Composable
fun OSShimmerItemRow(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(paddingValues)
            .padding(horizontal = OSDimens.SystemSpacing.Regular),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val shimmerColor = MaterialTheme.colorScheme.primary
        Box(
            modifier = Modifier
                .size(OSSafeItemStyle.Small.elementSize)
                .clip(CircleShape)
                .shimmer()
                .drawBehind { drawRect(shimmerColor) },
        )

        Spacer(modifier = Modifier.size(OSDimens.SystemSpacing.Regular))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(
                    with(LocalDensity.current) {
                        MaterialTheme.typography.labelMedium.fontSize.toDp() + MaterialTheme.typography.labelXSmall.fontSize.toDp()
                    },
                )
                .shimmer()
                .drawBehind { drawRect(shimmerColor) },
        )
    }
}

@Composable
@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
fun OSShimmerItemRowPreview() {
    OSPreviewBackgroundTheme {
        OSShimmerItemRow(
            paddingValues = PaddingValues(0.dp),
        )
    }
}
