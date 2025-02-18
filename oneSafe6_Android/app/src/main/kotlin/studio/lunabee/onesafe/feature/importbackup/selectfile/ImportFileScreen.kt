package studio.lunabee.onesafe.feature.importbackup.selectfile

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
import androidx.compose.ui.composed
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.accessibility.accessibilityClick
import studio.lunabee.onesafe.animation.OSTopBarAnimatedVisibility
import studio.lunabee.onesafe.animation.rememberOSTopBarVisibilityNestedScrollConnection
import studio.lunabee.onesafe.atom.OSRegularSpacer
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.atom.button.OSFilledButton
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.action.topAppBarOptionNavBack
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.molecule.OSTopAppBar
import studio.lunabee.onesafe.organism.card.OSTopImageLoadingCard
import studio.lunabee.onesafe.organism.card.param.OSCardImageParam
import studio.lunabee.onesafe.organism.card.param.OSCardProgressParam
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun ImportFileScreen(
    pickFile: () -> Unit,
    navigateBack: () -> Unit,
    extractProgress: Float?,
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
            OSTopImageLoadingCard(
                title = LbcTextSpec.StringResource(id = OSString.import_selectFile_title),
                description = LbcTextSpec.StringResource(id = OSString.import_selectFile_description),
                cardProgress = extractProgress?.let {
                    OSCardProgressParam.UndeterminedProgress(
                        progressDescription = LbcTextSpec.StringResource(id = OSString.import_selectFile_progress),
                    )
                },
                cardImage = OSCardImageParam(
                    imageRes = OSDrawable.character_jamy_cool,
                    offset = OSDimens.Card.DefaultImageCardOffset,
                ),
                modifier = Modifier
                    .composed {
                        val chooseFileAction: String = stringResource(id = OSString.import_selectFile_button)
                        semantics(mergeDescendants = true) {
                            if (extractProgress == null) { // disable action if we are in a loading state.
                                accessibilityClick(label = chooseFileAction, action = pickFile)
                            }
                        }
                    },
            )
            OSRegularSpacer()
            OSFilledButton(
                text = LbcTextSpec.StringResource(OSString.import_selectFile_button),
                onClick = pickFile,
                state = if (extractProgress != null) OSActionState.Disabled else OSActionState.Enabled,
                modifier = Modifier.align(Alignment.End),
            )
        }

        OSTopBarAnimatedVisibility(visible = nestedScrollConnection.isTopBarVisible) {
            OSTopAppBar(
                modifier = Modifier
                    .statusBarsPadding()
                    .align(Alignment.TopCenter),
                options = listOf(topAppBarOptionNavBack(navigateBack, isEnabled = extractProgress == null)),
            )
        }
    }
}

@OsDefaultPreview
@Composable
private fun ImportFileScreenPreview() {
    OSTheme {
        ImportFileScreen(
            pickFile = {},
            navigateBack = {},
            extractProgress = null,
        )
    }
}

@OsDefaultPreview
@Composable
private fun ImportFileScreenInProgressPreview() {
    OSTheme {
        ImportFileScreen(
            pickFile = {},
            navigateBack = {},
            extractProgress = 0.4f,
        )
    }
}
