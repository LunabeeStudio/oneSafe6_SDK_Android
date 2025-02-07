package studio.lunabee.onesafe.commonui.beta

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSButtonChip
import studio.lunabee.onesafe.atom.OSChipStyle
import studio.lunabee.onesafe.atom.OSChipType
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewOnSurfaceTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun AppBetaVersionChip(
    modifier: Modifier = Modifier,
) {
    var isAppBetaVersionBottomSheetVisible by remember { mutableStateOf(false) }
    AppBetaVersionBottomSheet(
        isVisible = isAppBetaVersionBottomSheetVisible,
        onBottomSheetClosed = {
            isAppBetaVersionBottomSheetVisible = false
        },
    )
    OSButtonChip(
        modifier = modifier,
        onClick = {
            isAppBetaVersionBottomSheetVisible = true
        },
        type = OSChipType.New,
        style = OSChipStyle.Small,
        label = {
            OSText(text = LbcTextSpec.StringResource(OSString.appBetaVersion_chip))
        },
    )
}

@OsDefaultPreview
@Composable
private fun AppBetaVersionChipPreview() {
    OSPreviewOnSurfaceTheme {
        Box(modifier = Modifier.padding(OSDimens.SystemSpacing.Regular)) {
            AppBetaVersionChip()
        }
    }
}
