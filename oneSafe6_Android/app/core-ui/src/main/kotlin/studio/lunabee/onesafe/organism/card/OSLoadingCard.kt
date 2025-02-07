package studio.lunabee.onesafe.organism.card

import android.content.res.Configuration
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSCard
import studio.lunabee.onesafe.atom.OSCardTitle
import studio.lunabee.onesafe.atom.OSLinearProgress
import studio.lunabee.onesafe.atom.OSRegularSpacer
import studio.lunabee.onesafe.atom.button.OSFilledButton
import studio.lunabee.onesafe.coreui.R
import studio.lunabee.onesafe.extension.loremIpsumSpec
import studio.lunabee.onesafe.molecule.OSTopImageBox
import studio.lunabee.onesafe.organism.card.component.OSCardAction
import studio.lunabee.onesafe.organism.card.component.OSCardDescription
import studio.lunabee.onesafe.organism.card.param.OSCardImageParam
import studio.lunabee.onesafe.organism.card.param.OSCardProgressParam
import studio.lunabee.onesafe.organism.card.scope.OSCardActionScope
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun OSTopImageLoadingCard(
    title: LbcTextSpec,
    description: LbcTextSpec?,
    cardProgress: OSCardProgressParam?,
    cardImage: OSCardImageParam,
    modifier: Modifier = Modifier,
    progressColor: Color = ProgressIndicatorDefaults.linearColor,
    progressTrackColor: Color = ProgressIndicatorDefaults.linearTrackColor,
    action: (@Composable OSCardActionScope.(padding: PaddingValues) -> Unit)? = null,
) {
    OSTopImageBox(
        imageRes = cardImage.imageRes,
        offset = cardImage.offset,
        modifier = modifier,
    ) {
        OSCard {
            CardContent(
                title = title,
                description = description,
                cardProgress = cardProgress,
                action = action,
                progressColor = progressColor,
                progressTrackColor = progressTrackColor,
            )
        }
    }
}

@Composable
fun OSLoadingCard(
    title: LbcTextSpec?,
    description: LbcTextSpec,
    cardProgress: OSCardProgressParam?,
    modifier: Modifier = Modifier,
    progressColor: Color = ProgressIndicatorDefaults.linearColor,
    progressTrackColor: Color = ProgressIndicatorDefaults.linearTrackColor,
    action: (@Composable OSCardActionScope.(padding: PaddingValues) -> Unit)? = null,
) {
    OSCard(
        modifier = modifier,
        content = {
            CardContent(
                title = title,
                description = description,
                cardProgress = cardProgress,
                action = action,
                progressColor = progressColor,
                progressTrackColor = progressTrackColor,
            )
        },
    )
}

@Composable
private fun ColumnScope.CardContent(
    title: LbcTextSpec?,
    description: LbcTextSpec?,
    cardProgress: OSCardProgressParam?,
    progressColor: Color,
    progressTrackColor: Color,
    action: (@Composable OSCardActionScope.(padding: PaddingValues) -> Unit)?,
) {
    OSRegularSpacer()
    title?.let {
        OSCardTitle(title = title)
        OSRegularSpacer()
    }
    description?.let {
        OSCardDescription(
            description = it,
            modifier = Modifier.fillMaxWidth(),
        )
    }

    action?.let {
        OSRegularSpacer()

        OSCardAction(
            contentAlignment = Alignment.Center,
            action = action,
        )
    } ?: OSRegularSpacer()

    if (cardProgress != null) {
        OSLinearProgress(
            progress = cardProgress.progress,
            progressDescription = cardProgress.progressDescription,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = OSDimens.SystemSpacing.Regular)
                .padding(top = OSDimens.SystemSpacing.Small)
                .semantics(mergeDescendants = true) {
                    liveRegion = LiveRegionMode.Polite // automatically read when displayed.
                },
            progressColor = progressColor,
            progressTrackColor = progressTrackColor,
        )
    }

    OSRegularSpacer()
}

@OsDefaultPreview
@Composable
private fun OSTopImageLoadingCardWithoutActionPreview() {
    OSPreviewBackgroundTheme {
        OSTopImageLoadingCard(
            title = loremIpsumSpec(2),
            description = loremIpsumSpec(10),
            cardProgress = OSCardProgressParam.DeterminedProgress(
                progress = .4f,
                progressDescription = loremIpsumSpec(4),
            ),
            cardImage = OSCardImageParam(imageRes = R.drawable.os_top_image_card_sample, offset = 11.dp),
        )
    }
}

@OsDefaultPreview
@Composable
private fun OSTopImageLoadingCardWithActionPreview() {
    OSPreviewBackgroundTheme {
        OSTopImageLoadingCard(
            title = loremIpsumSpec(2),
            description = loremIpsumSpec(10),
            cardProgress = OSCardProgressParam.DeterminedProgress(
                progress = .4f,
                progressDescription = loremIpsumSpec(4),
            ),
            cardImage = OSCardImageParam(imageRes = R.drawable.os_top_image_card_sample, offset = 11.dp),
            action = {
                OSFilledButton(
                    text = loremIpsumSpec(1),
                    onClick = { },
                )
            },
        )
    }
}

@OsDefaultPreview
@Composable
private fun OSLoadingCardWithoutActionPreview() {
    OSPreviewBackgroundTheme {
        OSLoadingCard(
            title = loremIpsumSpec(2),
            description = loremIpsumSpec(10),
            cardProgress = OSCardProgressParam.DeterminedProgress(
                progress = .4f,
                progressDescription = loremIpsumSpec(4),
            ),
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun OSLoadingCardWithActionPreview() {
    OSPreviewBackgroundTheme {
        OSLoadingCard(
            title = loremIpsumSpec(2),
            description = loremIpsumSpec(10),
            cardProgress = OSCardProgressParam.DeterminedProgress(
                progress = .4f,
                progressDescription = loremIpsumSpec(4),
            ),
            action = {
                OSFilledButton(
                    text = loremIpsumSpec(1),
                    onClick = { },
                )
            },
        )
    }
}
