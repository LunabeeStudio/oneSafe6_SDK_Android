package studio.lunabee.onesafe.commonui.beta

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.CommonUiConstants
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.bottomsheet.BottomSheetHolder
import studio.lunabee.onesafe.commonui.bottomsheet.InfoBottomSheetContent
import studio.lunabee.onesafe.commonui.bottomsheet.InfoBottomSheetContentAttributes
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewOnSurfaceTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBetaVersionBottomSheet(
    isVisible: Boolean,
    onBottomSheetClosed: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    BottomSheetHolder(
        isVisible = isVisible,
        onBottomSheetClosed = onBottomSheetClosed,
        skipPartiallyExpanded = true,
    ) { closeBottomSheet, paddingValues ->
        AppBetaVersionBottomSheetContent(paddingValues, uriHandler, closeBottomSheet)
    }
}

@Composable
private fun AppBetaVersionBottomSheetContent(
    paddingValues: PaddingValues,
    uriHandler: UriHandler,
    closeBottomSheet: () -> Unit,
) {
    InfoBottomSheetContent(
        paddingValues = paddingValues,
        title = LbcTextSpec.StringResource(OSString.appBetaVersion_bottomSheet_title),
        description = LbcTextSpec.StringResource(OSString.appBetaVersion_bottomSheet_description),
        secondaryAction = LbcTextSpec.StringResource(OSString.appBetaVersion_bottomSheet_discordButton) to {
            uriHandler.openUri(CommonUiConstants.ExternalLink.Discord)
            closeBottomSheet()
        },
        attributes = InfoBottomSheetContentAttributes()
            .titleCenter()
            .testTag(UiConstants.TestTag.BottomSheet.AppBetaVersionBottomSheet),
    )
}

@OsDefaultPreview
@Composable
fun AppBetaVersionBottomSheetPreview() {
    OSPreviewOnSurfaceTheme {
        val uriHandler = LocalUriHandler.current
        AppBetaVersionBottomSheetContent(
            paddingValues = PaddingValues(OSDimens.SystemSpacing.Regular),
            uriHandler = uriHandler,
            closeBottomSheet = { },
        )
    }
}
