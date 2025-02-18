package studio.lunabee.onesafe.feature.verifypassword

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.bottomsheet.BottomSheetHolder
import studio.lunabee.onesafe.commonui.bottomsheet.InfoBottomSheetContent
import studio.lunabee.onesafe.commonui.bottomsheet.InfoBottomSheetContentAttributes
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyPasswordBottomSheet(
    isVisible: Boolean,
    onBottomSheetClosed: () -> Unit,
    onClickOnVerify: () -> Unit,
) {
    BottomSheetHolder(
        isVisible = isVisible,
        onBottomSheetClosed = onBottomSheetClosed,
        skipPartiallyExpanded = true,
    ) { closeBottomSheet, paddingValues ->
        VerifyBottomSheetContent(
            paddingValues = paddingValues,
            onCLickOnLater = closeBottomSheet,
        ) {
            closeBottomSheet()
            onClickOnVerify()
        }
    }
}

@Composable
private fun VerifyBottomSheetContent(
    paddingValues: PaddingValues,
    onCLickOnLater: () -> Unit,
    onClickOnVerify: () -> Unit,
) {
    InfoBottomSheetContent(
        paddingValues = paddingValues,
        title = LbcTextSpec.StringResource(OSString.verifyPassword_bottomSheet_title),
        description = LbcTextSpec.StringResource(OSString.verifyPassword_bottomSheet_description),
        primaryAction = LbcTextSpec.StringResource(OSString.verifyPassword_bottomSheet_verifyButton) to onClickOnVerify,
        secondaryAction = LbcTextSpec.StringResource(OSString.verifyPassword_bottomSheet_laterButton) to onCLickOnLater,
        attributes = InfoBottomSheetContentAttributes()
            .testTag(UiConstants.TestTag.BottomSheet.VerifyPasswordBottomSheet),
    )
}

@Composable
@OsDefaultPreview
fun VerifyPasswordBottomSheetPreview() {
    OSPreviewBackgroundTheme {
        Box(Modifier.background(MaterialTheme.colorScheme.surface)) {
            VerifyBottomSheetContent(
                paddingValues = PaddingValues(0.dp),
                onCLickOnLater = { },
            ) {}
        }
    }
}
