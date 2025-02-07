package studio.lunabee.onesafe.feature.settings.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.atom.OSCard
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.commonui.extension.markdown
import studio.lunabee.onesafe.commonui.action.topAppBarOptionNavBack
import studio.lunabee.onesafe.molecule.ElevatedTopAppBar
import studio.lunabee.onesafe.molecule.OSTopImageBox
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.extensions.topAppBarElevation
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalColorPalette

@Composable
fun CreditsRoute(
    navigateBack: () -> Unit,
) {
    CreditScreen(
        navigateBack = navigateBack,
    )
}

@Composable
fun CreditScreen(
    navigateBack: () -> Unit,
) {
    val scrollState = rememberScrollState()
    OSScreen(
        testTag = UiConstants.TestTag.Screen.CreditsScreen,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
            .fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .padding(top = OSDimens.ItemTopBar.Height)
                .verticalScroll(scrollState)
                .padding(OSDimens.SystemSpacing.Regular),
        ) {
            OSTopImageBox(
                imageRes = OSDrawable.character_team,
                offset = null,
            ) {
                OSCard {
                    Column(
                        modifier = Modifier.padding(OSDimens.SystemSpacing.Regular),
                        verticalArrangement = Arrangement.spacedBy(OSDimens.SystemSpacing.Regular),
                    ) {
                        OSText(
                            text = LbcTextSpec.StringResource(id = OSString.creditsScreen_thanksCard_title),
                            style = MaterialTheme.typography.titleLarge,
                        )
                        OSText(
                            text = LbcTextSpec.StringResource(id = OSString.creditsScreen_thanksCard_message).markdown(),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        OSText(
                            text = LbcTextSpec.StringResource(id = OSString.creditsScreen_thanksCard_people).markdown(),
                            style = MaterialTheme.typography.bodyLarge,
                            color = LocalColorPalette.current.Neutral60,
                        )
                    }
                }
            }
        }
        ElevatedTopAppBar(
            title = LbcTextSpec.StringResource(OSString.creditsScreen_title),
            options = listOf(topAppBarOptionNavBack(navigateBack)),
            elevation = scrollState.topAppBarElevation,
        )
    }
}
