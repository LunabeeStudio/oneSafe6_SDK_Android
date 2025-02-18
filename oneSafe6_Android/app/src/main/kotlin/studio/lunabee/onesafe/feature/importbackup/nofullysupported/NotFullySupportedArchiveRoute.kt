package studio.lunabee.onesafe.feature.importbackup.nofullysupported

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.animation.OSTopBarAnimatedVisibility
import studio.lunabee.onesafe.animation.rememberOSTopBarVisibilityNestedScrollConnection
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.atom.button.OSFilledButton
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.action.topAppBarOptionNavBack
import studio.lunabee.onesafe.molecule.OSTopAppBar
import studio.lunabee.onesafe.molecule.OSTopImageBox
import studio.lunabee.onesafe.organism.card.OSMessageCard
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens

@Composable
fun NotFullySupportedArchiveRoute(
    navigateToImportAuthDestination: () -> Unit,
    navigateBack: () -> Unit,
) {
    NotFullySupportedArchiveScreen(
        navigateBack = navigateBack,
        onClickNext = navigateToImportAuthDestination,
    )
}

@Composable
fun NotFullySupportedArchiveScreen(
    navigateBack: () -> Unit,
    onClickNext: () -> Unit,
) {
    OSScreen(
        testTag = UiConstants.TestTag.Screen.ImportFileScreen,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
            .fillMaxSize(),
    ) {
        val scrollState = rememberScrollState()
        val nestedScrollConnection = rememberOSTopBarVisibilityNestedScrollConnection(scrollState)

        Column(
            modifier = Modifier
                .imePadding()
                .nestedScroll(nestedScrollConnection)
                .verticalScroll(scrollState)
                .padding(top = OSDimens.ItemTopBar.Height)
                .padding(horizontal = OSDimens.SystemSpacing.Regular, vertical = OSDimens.SystemSpacing.Large),
        ) {
            OSTopImageBox(
                imageRes = OSDrawable.character_jamy_cool,
                offset = OSDimens.Card.DefaultImageCardOffset,
            ) {
                OSMessageCard(
                    title = LbcTextSpec.StringResource(id = OSString.common_warning),
                    description = LbcTextSpec.StringResource(OSString.import_notFullySupportedArchive_warning_message),
                )
            }
            OSFilledButton(
                text = LbcTextSpec.StringResource(OSString.common_next),
                onClick = onClickNext,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(OSDimens.SystemSpacing.Regular),
            )
        }

        OSTopBarAnimatedVisibility(visible = nestedScrollConnection.isTopBarVisible) {
            OSTopAppBar(
                modifier = Modifier
                    .statusBarsPadding()
                    .align(Alignment.TopCenter),
                options = listOf(
                    topAppBarOptionNavBack(navigateBack),
                ),
            )
        }
    }
}
