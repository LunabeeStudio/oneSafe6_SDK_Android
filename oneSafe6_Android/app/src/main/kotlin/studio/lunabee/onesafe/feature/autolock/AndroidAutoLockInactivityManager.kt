package studio.lunabee.onesafe.feature.autolock

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.domain.manager.IsAppBlockedUseCase
import studio.lunabee.onesafe.domain.usecase.authentication.IsSafeReadyUseCase
import studio.lunabee.onesafe.domain.usecase.autolock.AutoLockInactivityUseCase
import studio.lunabee.onesafe.domain.usecase.autolock.RefreshLastUserInteractionUseCase
import studio.lunabee.onesafe.qualifier.AppScope
import javax.inject.Inject

@OptIn(DelicateCoroutinesApi::class)
class AndroidAutoLockInactivityManager @Inject constructor(
    private val autoLockInactivityUseCase: AutoLockInactivityUseCase,
    private val refreshLastUserInteractionUseCase: RefreshLastUserInteractionUseCase,
    @AppScope private val coroutineScope: CoroutineScope,
    private val isSafeReadyUseCase: IsSafeReadyUseCase,
    private val isAppBlockedUseCase: IsAppBlockedUseCase,
) : LifecycleEventObserver {

    private var inactivityJob: Job? = null

    init {
        val isLockableFlow = combine(
            isSafeReadyUseCase.flow(),
            isAppBlockedUseCase.flow(),
        ) { isSafeReady, isAppBlocked -> isSafeReady && !isAppBlocked }
        coroutineScope.launch {
            isLockableFlow.collectLatest { isLockable ->
                if (isLockable) {
                    inactivityJob?.cancel()
                    inactivityJob = coroutineScope.launch {
                        autoLockInactivityUseCase.app()
                    }
                } else {
                    stopAutoLockInactivity()
                }
            }
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_PAUSE -> {
                stopAutoLockInactivity()
            }
            Lifecycle.Event.ON_RESUME -> {
                startAutoLockInactivity()
            }
            Lifecycle.Event.ON_DESTROY -> {
                // Don't close this scope, we need it on activity recreation
                // when ToggleScreenshotSettingUseCase recreate the activity for example
                // coroutineScope.cancel()
            }
            else -> {
                /* no-op */
            }
        }
    }

    fun refreshLastUserInteraction() {
        refreshLastUserInteractionUseCase()
    }

    private fun startAutoLockInactivity() {
        inactivityJob?.cancel()
        inactivityJob = coroutineScope.launch {
            if (isSafeReadyUseCase() && !isAppBlockedUseCase()) {
                autoLockInactivityUseCase.app()
            }
        }
    }

    private fun stopAutoLockInactivity() {
        inactivityJob?.cancel()
    }
}
