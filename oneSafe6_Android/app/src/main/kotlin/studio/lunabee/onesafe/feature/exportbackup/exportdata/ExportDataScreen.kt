package studio.lunabee.onesafe.feature.exportbackup.exportdata

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSPlurals
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.action.topAppBarOptionNavBack
import studio.lunabee.onesafe.commonui.extension.markdownToAnnotatedString
import studio.lunabee.onesafe.molecule.OSTopAppBar
import studio.lunabee.onesafe.organism.card.OSTopImageLoadingCard
import studio.lunabee.onesafe.organism.card.param.OSCardImageParam
import studio.lunabee.onesafe.organism.card.param.OSCardProgressParam
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSTheme

@Composable
fun ExportDataScreen(
    itemCount: Int,
    contactCount: Int,
    isProcessingExport: Boolean,
    isExportInError: Boolean,
    navigateBack: () -> Unit,
) {
    OSScreen(
        testTag = UiConstants.TestTag.Screen.ExportDataScreen,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
            .imePadding(),
    ) {
        Box(
            modifier = Modifier
                .padding(
                    top = OSDimens.ItemTopBar.Height + OSDimens.SystemSpacing.Large,
                    start = OSDimens.SystemSpacing.Regular,
                    end = OSDimens.SystemSpacing.Regular,
                    bottom = OSDimens.SystemSpacing.Large,
                ),
        ) {
            OSTopImageLoadingCard(
                title = LbcTextSpec.StringResource(OSString.export_progressCard_title),
                description = LbcTextSpec.Annotated(
                    stringResource(
                        id = OSString.export_progressCard_withBubbles_description,
                        pluralStringResource(OSPlurals.export_progressCard_withBubbles_itemDescription, itemCount, itemCount),
                        pluralStringResource(OSPlurals.export_progressCard_withBubbles_bubblesDescription, contactCount, contactCount),
                    ).markdownToAnnotatedString(),
                ),
                cardProgress = when {
                    isProcessingExport -> OSCardProgressParam.UndeterminedProgress()
                    isExportInError -> OSCardProgressParam.DeterminedProgress(
                        progress = .5f, // TODO will be updated when implementing progress. Use for error for now
                        progressDescription = LbcTextSpec.StringResource(OSString.error_defaultMessage),
                    )
                    else -> null
                },
                cardImage = OSCardImageParam(OSDrawable.character_jamy_cool, OSDimens.Card.OffsetJamyCoolImage),
                progressColor = if (isExportInError) MaterialTheme.colorScheme.error else ProgressIndicatorDefaults.linearColor,
            )
        }

        OSTopAppBar(
            modifier = Modifier
                .statusBarsPadding()
                .align(Alignment.TopCenter),
            options = listOf(topAppBarOptionNavBack(navigateBack, !isProcessingExport)),
        )
    }
}

@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun ExportAuthPreview() {
    OSTheme {
        ExportDataScreen(
            itemCount = 100,
            contactCount = 10,
            isProcessingExport = false,
            isExportInError = false,
            navigateBack = { },
        )
    }
}

@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun ExportAuthErrorPreview() {
    OSTheme {
        ExportDataScreen(
            itemCount = 100,
            contactCount = 10,
            isProcessingExport = false,
            isExportInError = true,
            navigateBack = { },
        )
    }
}

@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun ExportAuthLoadingPreview() {
    OSTheme {
        ExportDataScreen(
            itemCount = 100,
            contactCount = 10,
            isProcessingExport = true,
            isExportInError = false,
            navigateBack = { },
        )
    }
}
