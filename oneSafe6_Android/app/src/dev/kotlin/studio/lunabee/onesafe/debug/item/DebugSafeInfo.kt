package studio.lunabee.onesafe.debug.item

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.debug.model.DebugSafeInfoData
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.molecule.OSDropdownMenuItem
import studio.lunabee.onesafe.molecule.OSRow

context(ColumnScope)
@Composable
fun DebugSafeInfo(modifier: Modifier, debugSafeInfoData: DebugSafeInfoData) {
    var expand by remember { mutableStateOf(false) }

    OSRow(
        text = LbcTextSpec.Raw("🆔 SafeId = ${debugSafeInfoData.currentSafeId?.short()}"),
        modifier = Modifier
            .clickable { expand = true }
            .then(modifier),
    ) {
        DropdownMenu(
            expanded = expand,
            onDismissRequest = { expand = false },
        ) {
            debugSafeInfoData.safeCryptos.forEach {
                OSDropdownMenuItem(
                    text = LbcTextSpec.Raw(it.id.short()),
                    icon = null,
                ) {
                    debugSafeInfoData.switchSafe(it.id)
                    expand = false
                }
            }
        }
    }
    if (debugSafeInfoData.currentSafeId != null) {
        OSRow(
            text = LbcTextSpec.Raw("🧹 Clear SafeId"),
            modifier = Modifier
                .clickable { debugSafeInfoData.switchSafe(null) }
                .then(modifier),
        )
    }
    OSRow(
        text = LbcTextSpec.Raw("📁 File count (safe/all) = ${debugSafeInfoData.fileCount.first}/${debugSafeInfoData.fileCount.second}"),
        modifier = modifier,
    )
    OSRow(
        text = LbcTextSpec.Raw("🖼️ Icon count (safe/all) = ${debugSafeInfoData.iconCount.first}/${debugSafeInfoData.iconCount.second}"),
        modifier = modifier,
    )
    OSRow(
        text = LbcTextSpec.Raw("🆕 Create safe"),
        modifier = Modifier
            .clickable { debugSafeInfoData.createSafe() }
            .then(modifier),
    )
    debugSafeInfoData.deleteAllItems?.let { deleteAllItems ->
        OSRow(
            text = LbcTextSpec.Raw("🗑️ Delete all items"),
            modifier = Modifier
                .clickable { deleteAllItems() }
                .then(modifier),
        )
    }
    debugSafeInfoData.deleteSafe?.let { deleteSafe ->
        OSRow(
            text = LbcTextSpec.Raw("🪦 Delete safe"),
            modifier = Modifier
                .clickable { deleteSafe() }
                .then(modifier),
        )
    }
}

private fun SafeId.short(): String {
    val splits = toString().split('-')
    return splits.first() + "..." + splits.last()
}
