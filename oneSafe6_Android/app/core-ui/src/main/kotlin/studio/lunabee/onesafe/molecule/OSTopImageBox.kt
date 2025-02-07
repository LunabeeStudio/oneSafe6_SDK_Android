package studio.lunabee.onesafe.molecule

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.compose.ui.zIndex
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.LocalCardContentExtraSpace
import studio.lunabee.onesafe.atom.OSRegularSpacer
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.coreui.R
import studio.lunabee.onesafe.extension.loremIpsumSpec
import studio.lunabee.onesafe.organism.card.OSMessageCard
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme

@Composable
fun OSTopImageBox(
    @DrawableRes imageRes: Int,
    modifier: Modifier = Modifier,
    offset: Dp? = OSDimens.Card.DefaultImageCardOffset,
    xImageOffset: Dp? = null,
    content: @Composable () -> Unit,
) {
    if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        content()
    } else {
        BoxWithConstraints(modifier) {
            val painter = painterResource(id = imageRes)
            val density = LocalDensity.current
            val widthBox = constraints.maxWidth
            val imageSourceWidth = painter.intrinsicSize.width
            val imageSourceHeight = painter.intrinsicSize.height
            val scaleImage: Float = widthBox / imageSourceWidth
            val imageDisplayedHeight = with(density) { (scaleImage * imageSourceHeight).toDp() }
            val offsetDisplayed = offset?.let { scaleImage * it } ?: 0.dp
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .zIndex(1f)
                    .offset(xImageOffset ?: 0.dp),
            ) {
                Image(
                    modifier = Modifier.fillMaxWidth(),
                    painter = painter,
                    contentScale = ContentScale.FillWidth,
                    contentDescription = null,
                )
            }

            Column {
                Spacer(modifier = Modifier.height(imageDisplayedHeight - offsetDisplayed))
                CompositionLocalProvider(LocalCardContentExtraSpace.provides(offset)) {
                    content()
                }
            }
        }
    }
}

@Composable
@Preview
private fun OSTopImageBoxPreview() {
    OSPreviewBackgroundTheme {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .requiredHeight(200.dp)
                    .background(Color.Red),
                contentAlignment = Alignment.Center,
            ) {
                OSText(LbcTextSpec.Raw("Other items"))
            }

            OSRegularSpacer()

            OSTopImageBox(
                imageRes = R.drawable.os_top_image_card_sample,
                offset = 11.dp,
            ) {
                OSMessageCard(
                    description = loremIpsumSpec(words = 10),
                )
            }

            OSRegularSpacer()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .requiredHeight(200.dp)
                    .background(Color.Green),
                contentAlignment = Alignment.Center,
            ) {
                OSText(LbcTextSpec.Raw("Other items"))
            }
        }
    }
}
