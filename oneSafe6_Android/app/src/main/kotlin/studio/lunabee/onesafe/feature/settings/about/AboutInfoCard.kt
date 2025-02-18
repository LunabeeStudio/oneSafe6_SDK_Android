package studio.lunabee.onesafe.feature.settings.about

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.settings.SettingsCard
import studio.lunabee.onesafe.feature.settings.settingcard.action.CardSettingsActionFollowOnFacebook
import studio.lunabee.onesafe.feature.settings.settingcard.action.CardSettingsActionFollowOnTiktok
import studio.lunabee.onesafe.feature.settings.settingcard.action.CardSettingsActionFollowOnTwitter
import studio.lunabee.onesafe.feature.settings.settingcard.action.CardSettingsActionFollowOnYoutube
import studio.lunabee.onesafe.feature.settings.settingcard.action.CardSettingsActionReadTermsOfUse
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme

@Composable
fun AboutInfoCard(
    onClickOnYoutube: () -> Unit,
    onClickOnFacebook: () -> Unit,
    onClickOnTwitter: () -> Unit,
    onClickOnTiktok: () -> Unit,
    onClickOnTermsOfUse: () -> Unit,
) {
    SettingsCard(
        title = LbcTextSpec.StringResource(OSString.aboutScreen_infoCard_title),
        actions = listOf(
            CardSettingsActionFollowOnYoutube(onClickOnYoutube),
            CardSettingsActionFollowOnFacebook(onClickOnFacebook),
            CardSettingsActionFollowOnTwitter(onClickOnTwitter),
            CardSettingsActionFollowOnTiktok(onClickOnTiktok),
            CardSettingsActionReadTermsOfUse(onClickOnTermsOfUse),
        ),
    )
}

@Composable
@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
fun AboutInfoCardPreview() {
    OSPreviewBackgroundTheme {
        AboutInfoCard(
            onClickOnYoutube = {},
            onClickOnFacebook = {},
            onClickOnTwitter = {},
            onClickOnTiktok = {},
            onClickOnTermsOfUse = {},
        )
    }
}
