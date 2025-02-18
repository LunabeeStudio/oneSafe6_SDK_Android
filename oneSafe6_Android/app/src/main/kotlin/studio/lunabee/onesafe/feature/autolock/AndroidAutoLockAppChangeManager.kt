package studio.lunabee.onesafe.feature.autolock

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.MainActivity
import studio.lunabee.onesafe.domain.manager.IsAppBlockedUseCase
import studio.lunabee.onesafe.domain.usecase.autolock.AutoLockBackgroundUseCase
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Singleton
class AndroidAutoLockAppChangeManager @Inject constructor(
    private val autoLockBackgroundUseCase: AutoLockBackgroundUseCase,
    @ApplicationContext context: Context,
    private val isAppBlockedUseCase: IsAppBlockedUseCase,
) : LifecycleEventObserver {
    val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var appChangeJob: Job? = null
    private val activityManager = context.getSystemService(ActivityManager::class.java)

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_STOP -> {
                startAutoLockAppChange()
            }
            Lifecycle.Event.ON_START -> {
                appChangeJob?.cancel()
                appChangeJob = null
            }
            else -> {
                /* no-op */
            }
        }
    }

    private fun startAutoLockAppChange() {
        appChangeJob?.cancel()
        appChangeJob = coroutineScope.launch {
            isAppBlockedUseCase.flow().collectLatest { isBlocking ->
                if (!isBlocking) {
                    var forceLockCounter = Duration.ZERO
                    var hasExternalActivityVisible = hasExternalActivityVisible(activityManager)
                    while (hasExternalActivityVisible != ExternalActivityVisibility.Background && forceLockCounter < forceLockThreshold) {
                        delay(5.seconds)

                        if (hasExternalActivityVisible == ExternalActivityVisibility.Unknown) {
                            forceLockCounter += 5.seconds
                        }
                        hasExternalActivityVisible = hasExternalActivityVisible(activityManager)
                    }
                    autoLockBackgroundUseCase.app()
                }
                // else, let collectLatest cancel the autoLockBackgroundUseCase delay
            }
        }
    }

    private fun hasExternalActivityVisible(activityManager: ActivityManager): ExternalActivityVisibility {
        val taskInfo = activityManager.appTasks.firstOrNull()?.taskInfo ?: return ExternalActivityVisibility.Background
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2 -> if (taskInfo.isVisible) {
                ExternalActivityVisibility.Visible
            } else {
                ExternalActivityVisibility.Background
            }
            // isVisible seems to exist but cannot be access (tested on API 31)
            taskInfo.toString().contains("isVisible=") -> if (taskInfo.toString().contains("isVisible=true")) {
                ExternalActivityVisibility.Visible
            } else {
                ExternalActivityVisibility.Background
            }
            // Only check if we have an external activity opened (= not the MainActivity)
            else -> if (taskInfo.topActivity?.className != MainActivity::class.qualifiedName) {
                // Don't request lock immediately (wait forceLockThreshold)
                ExternalActivityVisibility.Unknown
            } else {
                ExternalActivityVisibility.Background
            }
        }
    }

    companion object {
        private val forceLockThreshold: Duration = 2.minutes
    }
}

private enum class ExternalActivityVisibility {
    Visible, Background, Unknown
}
