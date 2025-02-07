package studio.lunabee.onesafe.feature.share.composable

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.extension.markdownToAnnotatedString
import studio.lunabee.onesafe.common.extensions.showCopyToast
import studio.lunabee.onesafe.commonui.SharePasswordLayout
import studio.lunabee.onesafe.molecule.OSTopImageBox
import studio.lunabee.onesafe.organism.card.OSMessageCard
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun SharePasswordCard(
    password: String,
    copyText: (label: String, value: String, isSecured: Boolean) -> Unit,
) {
    val context = LocalContext.current
    OSTopImageBox(
        imageRes = OSDrawable.character_jamy_cool,
    ) {
        OSMessageCard(
            title = LbcTextSpec.StringResource(OSString.share_passwordCard_title),
            description = LbcTextSpec.Annotated(
                stringResource(id = OSString.share_passwordCard_message).markdownToAnnotatedString(),
            ),
            action = {
                SharePasswordLayout(
                    password = password,
                    modifier = Modifier
                        .minTouchVerticalButtonOffset()
                        .padding(
                            horizontal = OSDimens.SystemSpacing.Regular,
                            vertical = OSDimens.SystemSpacing.Small,
                        ),
                    onClick = {
                        copyText(
                            context.getString(OSString.fieldName_password),
                            password,
                            true,
                        )
                        context.showCopyToast(
                            context.getString(OSString.fieldName_password),
                        )
                    },
                )
            },
        )
    }
}

@OsDefaultPreview
@Composable
fun SharePasswordCardPreview() {
    OSPreviewBackgroundTheme {
        SharePasswordCard(
            password = "MyPassword",
            copyText = { _, _, _ -> },
        )
    }
}
