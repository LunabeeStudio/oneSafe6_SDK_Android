package studio.lunabee.onesafe.feature.supportus

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.button.OSFilledButton
import studio.lunabee.onesafe.atom.button.defaults.OSFilledButtonDefaults
import studio.lunabee.onesafe.commonui.bottomsheet.InfoBottomSheetContent
import studio.lunabee.onesafe.commonui.bottomsheet.InfoBottomSheetContentAttributes
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.bottomsheet.BottomSheetHolder
import studio.lunabee.onesafe.ui.UiConstants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpUsTranslateBottomSheet(
    isVisible: Boolean,
    onBottomSheetClosed: () -> Unit,
    onClickHelpUs: () -> Unit,
) {
    BottomSheetHolder(
        isVisible = isVisible,
        onBottomSheetClosed = onBottomSheetClosed,
        skipPartiallyExpanded = true,
    ) { closeBottomSheet, paddingValues ->
        HelpUsTranslateBottomSheetContent(
            paddingValues = paddingValues,
        ) {
            closeBottomSheet()
            onClickHelpUs()
        }
    }
}

@Composable
private fun HelpUsTranslateBottomSheetContent(
    paddingValues: PaddingValues,
    onClickHelpUs: () -> Unit,
) {
    InfoBottomSheetContent(
        paddingValues = paddingValues,
        title = LbcTextSpec.StringResource(OSString.home_translate_help_title),
        description = LbcTextSpec.StringResource(OSString.home_translate_help_description),
        attributes = InfoBottomSheetContentAttributes()
            .testTag(UiConstants.TestTag.BottomSheet.HelpUsTranslateBottomSheet),
    ) {
        OSFilledButton(
            text = LbcTextSpec.StringResource(OSString.home_translate_help_button),
            onClick = onClickHelpUs,
            buttonColors = OSFilledButtonDefaults.primaryButtonColors(),
            modifier = Modifier
                .align(Alignment.CenterEnd),
        )
    }
}
