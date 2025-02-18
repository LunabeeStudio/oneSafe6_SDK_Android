package studio.lunabee.onesafe

import android.app.Service
import android.content.Intent
import android.os.IBinder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import studio.lunabee.onesafe.domain.LoadFileCancelAllUseCase
import javax.inject.Inject

// TODO add test if possible (create file, finish task, check file existence)
/**
 * Attempt to run some cleaning on app task removed. Does not work as expected on some 🇨🇳 devices as they require extra permission to
 * disable battery optimization and launch stuff from background.
 * https://stackoverflow.com/a/42120277/9994620
 */
@AndroidEntryPoint
class ClearFileCacheService : Service() {
    @Inject lateinit var loadFileCancelAllUseCase: LoadFileCancelAllUseCase

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // Don't use LifecycleService as we need to block to make sure we clean stuff
        runBlocking {
            loadFileCancelAllUseCase()
            stopSelf()
        }
    }
}
