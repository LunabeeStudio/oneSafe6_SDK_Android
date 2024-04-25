package studio.lunabee.onesafe.help.lostkeyexplain

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.action.topAppBarOptionNavBack
import studio.lunabee.onesafe.molecule.ElevatedTopAppBar
import studio.lunabee.onesafe.organism.card.OSMessageCard
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.extensions.topAppBarElevation
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem
import studio.lunabee.onesafe.ui.theme.OSTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

context(LostKeyExplainNavigation)
@Composable
fun LostKeyExplainRoute() {
    LostKeyExplainScreen(
        navigateBack = navigateBack,
    )
}

@Composable
private fun LostKeyExplainScreen(
    navigateBack: () -> Unit,
) {
    OSScreen(
        testTag = UiConstants.TestTag.Screen.LostKeyExplainScreen,
        background = LocalDesignSystem.current.warningBackgroundGradient(),
    ) {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .padding(top = OSDimens.ItemTopBar.Height)
                .verticalScroll(scrollState)
                .padding(OSDimens.SystemSpacing.Regular),
        ) {
            OSMessageCard(
                description = LbcTextSpec.StringResource(OSString.lostKeyExplain_card_message),
            )
        }

        ElevatedTopAppBar(
            title = LbcTextSpec.StringResource(OSString.lostKeyExplain_title),
            options = listOf(topAppBarOptionNavBack(navigateBack)),
            elevation = scrollState.topAppBarElevation,
        )
    }
}

@Composable
@OsDefaultPreview
private fun LostKeyExplainScreenPreview() {
    OSTheme {
        LostKeyExplainScreen(
            navigateBack = {},
        )
    }
}
