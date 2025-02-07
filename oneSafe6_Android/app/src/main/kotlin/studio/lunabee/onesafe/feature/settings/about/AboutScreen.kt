package studio.lunabee.onesafe.feature.settings.about

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.BuildConfig
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.commonui.CommonUiConstants
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.action.topAppBarOptionNavBack
import studio.lunabee.onesafe.commonui.settings.SettingsCard
import studio.lunabee.onesafe.feature.settings.settingcard.action.CardSettingsActionCredits
import studio.lunabee.onesafe.feature.settings.settingcard.action.CardSettingsActionLibraries
import studio.lunabee.onesafe.molecule.ElevatedTopAppBar
import studio.lunabee.onesafe.molecule.OSTopImageBox
import studio.lunabee.onesafe.organism.card.OSMessageCard
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.extensions.topAppBarElevation
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalColorPalette
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme

@Composable
fun AboutRoute(
    navigateBack: () -> Unit,
    navigateToCredits: () -> Unit,
    onClickOnLibraries: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    val termsUrl = stringResource(id = OSString.cgu_url)
    AboutScreen(
        navigateBack = navigateBack,
        onClickOnCredits = navigateToCredits,
        onClickOnLibraries = onClickOnLibraries,
        onClickOnYoutube = { uriHandler.openUri(CommonUiConstants.ExternalLink.Youtube) },
        onClickOnFacebook = { uriHandler.openUri(CommonUiConstants.ExternalLink.Facebook) },
        onClickOnTwitter = { uriHandler.openUri(CommonUiConstants.ExternalLink.Twitter) },
        onClickOnTiktok = { uriHandler.openUri(CommonUiConstants.ExternalLink.Tiktok) },
        onClickOnTerms = { uriHandler.openUri(termsUrl) },
    )
}

@Composable
fun AboutScreen(
    navigateBack: () -> Unit,
    onClickOnCredits: () -> Unit,
    onClickOnLibraries: () -> Unit,
    onClickOnYoutube: () -> Unit,
    onClickOnFacebook: () -> Unit,
    onClickOnTwitter: () -> Unit,
    onClickOnTiktok: () -> Unit,
    onClickOnTerms: () -> Unit,
) {
    val lazyListState = rememberLazyListState()
    OSScreen(
        testTag = UiConstants.TestTag.Screen.AboutScreen,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
            .fillMaxSize(),
    ) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .padding(top = OSDimens.ItemTopBar.Height)
                .testTag(UiConstants.TestTag.Item.AboutScreenList),
            contentPadding = PaddingValues(OSDimens.SystemSpacing.Regular),
            verticalArrangement = Arrangement.spacedBy(OSDimens.SystemSpacing.Large),
        ) {
            item(
                key = TopImageCardKey,
            ) {
                OSTopImageBox(
                    imageRes = OSDrawable.character_hello,
                ) {
                    OSMessageCard(
                        description = LbcTextSpec.StringResource(OSString.aboutScreen_message),
                    )
                }
            }

            item(
                key = InfoCardKey,
            ) {
                AboutInfoCard(
                    onClickOnYoutube = onClickOnYoutube,
                    onClickOnFacebook = onClickOnFacebook,
                    onClickOnTwitter = onClickOnTwitter,
                    onClickOnTiktok = onClickOnTiktok,
                    onClickOnTermsOfUse = onClickOnTerms,
                )
            }

            item(
                key = CreditsCardKey,
            ) {
                SettingsCard(
                    actions = listOf(
                        CardSettingsActionCredits(onClickOnCredits),
                        CardSettingsActionLibraries(onClickOnLibraries),
                    ),
                )
            }

            item(
                key = AppInfoLabel,
            ) {
                OSText(
                    text = LbcTextSpec.Raw(
                        "${stringResource(id = OSString.application_name)} - ${BuildConfig.VERSION_NAME} #${BuildConfig.VERSION_CODE}",
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = LocalColorPalette.current.Neutral60,
                    modifier = Modifier.fillMaxSize(),
                    textAlign = TextAlign.Center,
                )
                OSText(
                    text = LbcTextSpec.StringResource(id = OSString.aboutScreen_companyName),
                    style = MaterialTheme.typography.bodySmall,
                    color = LocalColorPalette.current.Neutral60,
                    modifier = Modifier.fillMaxSize(),
                    textAlign = TextAlign.Center,
                )
            }
        }
        ElevatedTopAppBar(
            title = LbcTextSpec.StringResource(OSString.aboutScreen_title),
            options = listOf(topAppBarOptionNavBack(navigateBack)),
            elevation = lazyListState.topAppBarElevation,
        )
    }
}

private const val TopImageCardKey: String = "TopImageCardKey"
private const val InfoCardKey: String = "InfoCardKey"
private const val CreditsCardKey: String = "CreditsCardKey"
private const val AppInfoLabel: String = "AppInfoLabel"

@Composable
@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
fun AboutScreenPreview() {
    OSPreviewBackgroundTheme {
        AboutScreen(
            navigateBack = { },
            onClickOnCredits = {},
            onClickOnYoutube = { },
            onClickOnFacebook = { },
            onClickOnTwitter = { },
            onClickOnTiktok = { },
            onClickOnTerms = { },
            onClickOnLibraries = { },
        )
    }
}
