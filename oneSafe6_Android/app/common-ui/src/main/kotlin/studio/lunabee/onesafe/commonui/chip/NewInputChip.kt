package studio.lunabee.onesafe.commonui.chip

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSChipStyle
import studio.lunabee.onesafe.atom.OSChipType
import studio.lunabee.onesafe.atom.OSInputChip
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.commonui.OSString

@Composable
fun NewInputChip(
    modifier: Modifier = Modifier,
) {
    OSInputChip(
        modifier = modifier,
        selected = true,
        onClick = null,
        type = OSChipType.New,
        style = OSChipStyle.Small,
        label = {
            OSText(
                text = LbcTextSpec.StringResource(OSString.common_new),
            )
        },
    )
}
