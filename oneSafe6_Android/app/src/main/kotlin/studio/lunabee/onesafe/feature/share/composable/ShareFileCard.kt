package studio.lunabee.onesafe.feature.share.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSCardTitle
import studio.lunabee.onesafe.atom.OSRegularSpacer
import studio.lunabee.onesafe.atom.button.OSFilledButton
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.common.extensions.byteToHumanReadable
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.OSPlurals
import studio.lunabee.onesafe.commonui.extension.markdown
import studio.lunabee.onesafe.organism.card.OSCustomCard
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun ShareFileCard(
    itemsNbr: Int,
    fileSizeInfo: Pair<String, Int>,
    onClickOnShare: () -> Unit,
) {
    Column {
        OSCustomCard(
            content = {
                Column(
                    modifier = Modifier.padding(horizontal = OSDimens.SystemSpacing.Regular),
                    verticalArrangement = Arrangement.spacedBy(OSDimens.SystemSpacing.Regular),
                ) {
                    OSText(
                        text = LbcTextSpec.StringResource(id = OSString.share_fileCard_message).markdown(),
                    )
                }
            },
            titleSlot = {
                OSCardTitle(title = LbcTextSpec.StringResource(OSString.share_fileCard_title))
                OSText(
                    text = LbcTextSpec.PluralsResource(
                        id = OSPlurals.share_fileCard_fileSize,
                        count = itemsNbr,
                        stringResource(id = fileSizeInfo.second, fileSizeInfo.first),
                        itemsNbr,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = OSDimens.SystemSpacing.Regular),
                )
                OSRegularSpacer()
            },
        )
        OSFilledButton(
            text = LbcTextSpec.StringResource(OSString.share_fileCard_actionLabel),
            onClick = onClickOnShare,
            modifier = Modifier
                .padding(vertical = OSDimens.SystemSpacing.Regular)
                .align(Alignment.End),
            leadingIcon = {
                Icon(
                    painter = painterResource(id = OSDrawable.ic_share),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            },
        )
    }
}

@Composable
@OsDefaultPreview
fun ShareFileCardPreview() {
    OSPreviewBackgroundTheme {
        ShareFileCard(
            itemsNbr = 12,
            fileSizeInfo = 10_000L.byteToHumanReadable(),
            onClickOnShare = {},
        )
    }
}
