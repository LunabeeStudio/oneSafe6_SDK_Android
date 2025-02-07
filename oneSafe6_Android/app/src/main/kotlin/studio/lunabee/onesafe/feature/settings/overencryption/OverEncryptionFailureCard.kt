package studio.lunabee.onesafe.feature.settings.overencryption

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSLinearProgress
import studio.lunabee.onesafe.atom.button.OSTextButton
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.error.codeText
import studio.lunabee.onesafe.jvm.get
import studio.lunabee.onesafe.error.OSStorageError
import studio.lunabee.onesafe.molecule.OSTopImageBox
import studio.lunabee.onesafe.organism.card.OSMessageCard
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun OverEncryptionFailureCard(
    description: LbcTextSpec,
    openDiscord: () -> Unit,
) {
    OSTopImageBox(imageRes = OSDrawable.character_sabine_oups_right, offset = null) {
        OSMessageCard(
            title = LbcTextSpec.StringResource(OSString.error_defaultMessage),
            description = description,
            action = { cardPadding ->
                Column(Modifier.padding(cardPadding)) {
                    OSLinearProgress(
                        progress = 0.33f,
                        modifier = Modifier
                            .padding(top = OSDimens.SystemSpacing.Regular),
                        progressColor = MaterialTheme.colorScheme.error,
                        progressTrackColor = MaterialTheme.colorScheme.errorContainer,
                    )
                    OSTextButton(
                        text = LbcTextSpec.StringResource(OSString.common_askForHelpOndiscord),
                        onClick = openDiscord,
                        modifier = with(this@OSMessageCard) {
                            Modifier.minTouchVerticalButtonOffset()
                        }.align(Alignment.CenterHorizontally),
                    )
                }
            },
        )
    }
}

@OsDefaultPreview
@Composable
private fun OverEncryptionFailureCardPreview() {
    OSPreviewBackgroundTheme {
        OverEncryptionFailureCard(
            description = LbcTextSpec.StringResource(
                OSString.overEncryptionEnabled_errorCard_message,
                OSStorageError.Code.UNKNOWN_DATABASE_ERROR.get().codeText(),
            ),
            openDiscord = {},
        )
    }
}
