package studio.lunabee.onesafe.feature.favorite

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.utils.OsDefaultPreview
import studio.lunabee.onesafe.molecule.OSTopImageBox
import studio.lunabee.onesafe.organism.card.OSMessageCard
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme

@Composable
fun FavoriteEmptyCard(
    modifier: Modifier = Modifier,
) {
    OSTopImageBox(
        imageRes = OSDrawable.character_sabine_oups_right,
        modifier = modifier,
        offset = null,
    ) {
        OSMessageCard(
            description = LbcTextSpec.StringResource(OSString.favorites_empty_card_description),
            title = LbcTextSpec.StringResource(OSString.favorites_empty_card_title),
        )
    }
}

@Composable
@OsDefaultPreview
fun FavoriteEmptyCardPreview() {
    OSPreviewBackgroundTheme {
        FavoriteEmptyCard()
    }
}
