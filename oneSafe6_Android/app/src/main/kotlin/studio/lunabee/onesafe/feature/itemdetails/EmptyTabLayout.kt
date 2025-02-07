package studio.lunabee.onesafe.feature.itemdetails

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.molecule.OSRow
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme

@Composable
fun EmptyTabLayout(
    text: LbcTextSpec,
    showImage: Boolean,
    modifier: Modifier = Modifier,
) {
    val rowPaddingValue = if (showImage) {
        PaddingValues(
            start = OSDimens.SystemSpacing.Regular,
            end = OSDimens.SystemSpacing.Regular,
            top = OSDimens.SystemSpacing.Regular,
            bottom = OSDimens.SystemSpacing.Small,
        )
    } else {
        PaddingValues(
            horizontal = OSDimens.SystemSpacing.Regular,
            vertical = OSDimens.SystemSpacing.ExtraLarge,
        )
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        OSRow(
            text = text,
            modifier = Modifier.padding(rowPaddingValue),
            textMaxLines = Int.MAX_VALUE,
        )

        if (showImage) {
            Image(
                painter = painterResource(id = OSDrawable.character_sabine_oups_center),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(ImageWidthRatio),
            )
        }
    }
}

private const val ImageWidthRatio: Float = 0.43f // Determined with Figma

@Composable
@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
fun EmptyTabLayoutPreview() {
    OSPreviewBackgroundTheme {
        EmptyTabLayout(
            text = LbcTextSpec.Raw("Text"),
            showImage = true,
        )
    }
}
