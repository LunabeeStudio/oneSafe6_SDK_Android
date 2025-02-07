package studio.lunabee.onesafe.feature.autolock

import android.app.ActivityManager
import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.coroutineScope
import com.lunabee.lblogger.LBLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.domain.usecase.autolock.LockAppUseCase
import javax.inject.Inject
import javax.inject.Singleton

private val logger = LBLogger.get<AndroidAutoLockAppClosedManager>()

/**
 * Lock the application when the main activity is destroy and there is no other activity running. It happens if a background
 * work is running in the same process so it maintains the process alive. Or on  Redmi device with oSK service running.
 */
@Singleton
class AndroidAutoLockAppClosedManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val lockAppUseCase: LockAppUseCase,
) : DefaultLifecycleObserver {
    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        ProcessLifecycleOwner.get().lifecycle.coroutineScope.launch {
            delay(500) // Let some time in case the process die naturally. If not, lock the app.
            val am: ActivityManager = context.getSystemService(ActivityManager::class.java)
            val isLastActivity = am.appTasks.sumOf { it.taskInfo?.numActivities ?: 0 } == 0
            if (isLastActivity) {
                logger.i("Locking app on last activity destroyed but process still run")
                lockAppUseCase(false)
            }
        }
    }
}
