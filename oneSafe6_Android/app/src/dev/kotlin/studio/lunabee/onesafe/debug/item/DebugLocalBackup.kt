package studio.lunabee.onesafe.debug.item

import androidx.compose.foundation.clickable
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.molecule.OSDropdownMenuItem
import studio.lunabee.onesafe.molecule.OSRow

class DebugLocalBackup(
    private val localBackup: () -> Unit,
    private val deleteLocalBackups: () -> Unit,
    private val openBackupStorage: () -> Unit,
) {
    @Composable
    fun Composable(
        modifier: Modifier,
    ) {
        var expand by remember { mutableStateOf(false) }
        OSRow(
            text = LbcTextSpec.Raw("\uD83D\uDCF1 Local backup"),
            modifier = Modifier
                .clickable { expand = true }
                .then(modifier),
        ) {
            DropdownMenu(
                expanded = expand,
                onDismissRequest = { expand = false },
            ) {
                OSDropdownMenuItem(text = LbcTextSpec.Raw("Trigger local backup"), null) {
                    localBackup()
                    expand = false
                }
                OSDropdownMenuItem(text = LbcTextSpec.Raw("Delete all local backups"), null) {
                    deleteLocalBackups()
                    expand = false
                }
                OSDropdownMenuItem(text = LbcTextSpec.Raw("Open backup provider"), null) {
                    openBackupStorage()
                    expand = false
                }
            }
        }
    }
}
