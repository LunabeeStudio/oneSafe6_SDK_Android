package studio.lunabee.onesafe.feature.supportus

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.bottomsheet.BottomSheetHolder
import studio.lunabee.onesafe.commonui.bottomsheet.InfoBottomSheetContent
import studio.lunabee.onesafe.commonui.bottomsheet.InfoBottomSheetContentAttributes
import studio.lunabee.onesafe.ui.UiConstants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportUsBottomSheet(
    isVisible: Boolean,
    onBottomSheetClosed: () -> Unit,
    onClickOnSupportUs: () -> Unit,
) {
    BottomSheetHolder(
        isVisible = isVisible,
        onBottomSheetClosed = onBottomSheetClosed,
        skipPartiallyExpanded = true,
    ) { closeBottomSheet, paddingValues ->
        InfoBottomSheetContent(
            paddingValues = paddingValues,
            title = LbcTextSpec.StringResource(OSString.supportUs_bottomSheet_title),
            description = LbcTextSpec.StringResource(OSString.supportUs_card_description),
            primaryAction = LbcTextSpec.StringResource(OSString.supportUs_bottomSheet_rateButton) to onClickOnSupportUs,
            secondaryAction = LbcTextSpec.StringResource(OSString.common_later) to closeBottomSheet,
            attributes = InfoBottomSheetContentAttributes()
                .titleCenter()
                .testTag(UiConstants.TestTag.BottomSheet.AskForSupportBottomSheet),
        )
    }
}
