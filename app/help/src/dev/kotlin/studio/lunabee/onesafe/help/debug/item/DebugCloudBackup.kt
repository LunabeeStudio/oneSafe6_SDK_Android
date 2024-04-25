package studio.lunabee.onesafe.help.debug.item

import androidx.compose.foundation.clickable
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.molecule.OSDropdownMenuItem
import studio.lunabee.onesafe.molecule.OSRow

internal class DebugCloudBackup(
    private val fetchBackups: () -> Unit,
    private val uploadBackup: () -> Unit,
    private val deleteBackup: () -> Unit,
    private val synchronizeBackups: () -> Unit,
    private val getOneSafeFolderUri: suspend () -> Unit,
) {
    @Composable
    fun Composable(
        modifier: Modifier,
    ) {
        var expand by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()

        OSRow(
            text = LbcTextSpec.Raw("☁\uFE0F Cloud backup"),
            modifier = Modifier
                .clickable { expand = true }
                .then(modifier),
        ) {
            DropdownMenu(
                expanded = expand,
                onDismissRequest = { expand = false },
            ) {
                OSDropdownMenuItem(text = LbcTextSpec.Raw("Fetch backups (db/logs)"), null) {
                    fetchBackups()
                    expand = false
                }
                OSDropdownMenuItem(text = LbcTextSpec.Raw("Upload last backup (db/drive/logs)"), null) {
                    uploadBackup()
                    expand = false
                }
                OSDropdownMenuItem(text = LbcTextSpec.Raw("Delete oldest backup"), null) {
                    deleteBackup()
                    expand = false
                }
                OSDropdownMenuItem(text = LbcTextSpec.Raw("Trigger cloud sync"), null) {
                    synchronizeBackups()
                    expand = false
                }
                OSDropdownMenuItem(text = LbcTextSpec.Raw("Copy drive url"), null) {
                    coroutineScope.launch {
                        getOneSafeFolderUri()
                    }
                    expand = false
                }
            }
        }
    }
}
