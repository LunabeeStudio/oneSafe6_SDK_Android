package studio.lunabee.onesafe.help.debug.item

import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.molecule.OSDropdownMenuItem

@Composable
internal fun DebugSafeItem(
    modifier: Modifier,
    data: DebugSafeItemData,
) {
    var isExpanded by remember { mutableStateOf(false) }

    Button(
        onClick = {
            isExpanded = true
        },
        modifier = modifier,
    ) {
        OSText(LbcTextSpec.Raw("\uD83D\uDDC3\uFE0F Action on items"))
        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
        ) {
            OSDropdownMenuItem(text = LbcTextSpec.Raw("Create recursive item"), null) {
                data.createRecursiveItem()
                isExpanded = false
            }
            OSDropdownMenuItem(text = LbcTextSpec.Raw("Remove all items ☢️"), null) {
                data.removeAllItems()
                isExpanded = false
            }
            OSDropdownMenuItem(text = LbcTextSpec.Raw("Corrupt first file"), null) {
                data.corruptFile()
                isExpanded = false
            }
        }
    }
}
