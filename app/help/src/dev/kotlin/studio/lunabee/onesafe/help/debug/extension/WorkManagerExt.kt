package studio.lunabee.onesafe.help.debug.extension

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import studio.lunabee.onesafe.importexport.worker.LocalBackupWorker

internal fun WorkManager.startLocalBackupWorker() {
    val workRequest = OneTimeWorkRequestBuilder<LocalBackupWorker>()
        .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
        .build()
    enqueueUniqueWork(
        "LocalBackupWorker_DEBUG",
        ExistingWorkPolicy.APPEND_OR_REPLACE,
        workRequest,
    )
}
