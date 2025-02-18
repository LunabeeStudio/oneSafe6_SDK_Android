package studio.lunabee.onesafe.commonui.utils

import android.app.BackgroundServiceStartNotAllowedException
import android.os.Build
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import com.lunabee.lblogger.LBLogger
import com.lunabee.lblogger.e

private val logger = LBLogger.get("CoroutineWorkerExt")

suspend fun CoroutineWorker.setForegroundSafe(foregroundInfo: ForegroundInfo) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                setForeground(foregroundInfo)
            } catch (e: BackgroundServiceStartNotAllowedException) {
                // app is in background
            }
        }
    } catch (e: Exception) {
        logger.e(e)
    }
}
