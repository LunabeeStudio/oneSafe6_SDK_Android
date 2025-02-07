package studio.lunabee.onesafe.feature.exportbackup.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.zIndex
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.accessibility.accessibilityMergeDescendants
import studio.lunabee.onesafe.atom.OSRegularSpacer
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.atom.button.OSFilledButton
import studio.lunabee.onesafe.commonui.action.topAppBarOptionNavBack
import studio.lunabee.onesafe.molecule.OSTopAppBar
import studio.lunabee.onesafe.molecule.OSTopImageBox
import studio.lunabee.onesafe.organism.card.OSMessageCard
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme

@Composable
fun ExportEmptyScreen(
    navigateBack: () -> Unit,
) {
    OSScreen(
        testTag = UiConstants.TestTag.Screen.ExportEmptyScreen,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = OSDimens.ItemTopBar.Height + OSDimens.SystemSpacing.Large,
                    start = OSDimens.SystemSpacing.Regular,
                    end = OSDimens.SystemSpacing.Regular,
                    bottom = OSDimens.SystemSpacing.Large,
                ),
        ) {
            OSTopImageBox(
                imageRes = OSDrawable.character_sabine_oups_right,
                offset = null,
                modifier = Modifier
                    // FIXME for accessibility, waiting for something similar https://issuetracker.google.com/issues/186443263
                    .zIndex(1f),
            ) {
                OSMessageCard(
                    title = LbcTextSpec.StringResource(id = OSString.backup_protectBackup_noItems_title),
                    description = LbcTextSpec.StringResource(id = OSString.backup_protectBackup_noItems_message),
                    action = null,
                    modifier = Modifier
                        .accessibilityMergeDescendants(),
                )
            }

            OSRegularSpacer()

            OSFilledButton(
                text = LbcTextSpec.StringResource(id = OSString.common_back),
                onClick = navigateBack,
                modifier = Modifier
                    .align(alignment = Alignment.End),
            )
        }

        OSTopAppBar(
            modifier = Modifier
                .statusBarsPadding()
                .align(Alignment.TopCenter),
            options = listOf(topAppBarOptionNavBack(navigateBack)),
        )
    }
}

@Preview
@Composable
private fun ExportEmptyScreenPreview() {
    OSPreviewBackgroundTheme {
        ExportEmptyScreen(
            navigateBack = { },
        )
    }
}
