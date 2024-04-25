package studio.lunabee.onesafe.help.debug.item

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.lunabee.lblogger.LBLogger
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.importexport.ImportExportAndroidConstants
import studio.lunabee.onesafe.importexport.model.AutoBackupError
import studio.lunabee.onesafe.importexport.model.AutoBackupMode
import studio.lunabee.onesafe.importexport.worker.AutoBackupChainWorker
import studio.lunabee.onesafe.molecule.OSDropdownMenuItem
import studio.lunabee.onesafe.molecule.OSRow
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

private val logger = LBLogger.get<DebugAutoBackup>()

internal class DebugAutoBackup(
    private val storeAutoBackupError: (AutoBackupMode?) -> Unit,
    private val autoBackupError: AutoBackupError?,
    private val cancelAutoBackup: () -> Unit,
) {
    @Composable
    fun Composable(modifier: Modifier) {
        var expand by remember { mutableStateOf(false) }
        var errorExpand by remember { mutableStateOf(false) }
        val context = LocalContext.current

        OSRow(
            text = LbcTextSpec.Raw("⏲️ Auto-backup"),
            modifier = Modifier
                .clickable { expand = true }
                .then(modifier),
        ) {
            DropdownMenu(
                expanded = expand,
                onDismissRequest = { expand = false },
            ) {
                OSDropdownMenuItem(text = LbcTextSpec.Raw("Trigger auto-backup"), null) {
                    WorkManager.getInstance(context).enqueue(OneTimeWorkRequestBuilder<AutoBackupChainWorker>().build())
                    expand = false
                }
                OSDropdownMenuItem(text = LbcTextSpec.Raw("Schedule auto-backup every 15min"), null) {
                    val workRequest = PeriodicWorkRequestBuilder<AutoBackupChainWorker>(
                        repeatInterval = 15.minutes.toJavaDuration(), // min
                        flexTimeInterval = 5.minutes.toJavaDuration(), // min
                    )
                        .addTag(ImportExportAndroidConstants.AUTO_BACKUP_WORKER_TAG)
                        .build()
                    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                        "test worker",
                        ExistingPeriodicWorkPolicy.UPDATE,
                        workRequest,
                    )
                    expand = false
                }
                OSDropdownMenuItem(text = LbcTextSpec.Raw("Cancel scheduled auto-backup"), null) {
                    cancelAutoBackup()
                    expand = false
                }
                if (autoBackupError != null) {
                    OSDropdownMenuItem(text = LbcTextSpec.Raw("Print error"), null) {
                        logger.d(autoBackupError.toString())
                        Toast.makeText(context, autoBackupError.code, Toast.LENGTH_SHORT).show()
                        expand = false
                    }
                    OSDropdownMenuItem(text = LbcTextSpec.Raw("Clear error"), null) {
                        storeAutoBackupError(null)
                        expand = false
                    }
                }
                OSDropdownMenuItem(text = LbcTextSpec.Raw("Store error with source..."), null) {
                    expand = false
                    errorExpand = true
                }
            }
            DropdownMenu(
                expanded = errorExpand,
                onDismissRequest = { errorExpand = false },
            ) {
                AutoBackupMode.entries.forEach {
                    OSDropdownMenuItem(text = LbcTextSpec.Raw(it.name), null) {
                        storeAutoBackupError(it)
                        errorExpand = false
                    }
                }
            }
        }
    }
}
