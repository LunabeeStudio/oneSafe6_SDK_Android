package studio.lunabee.onesafe.common

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.lunabee.lblogger.LBLogger
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.usecase.authentication.IsSafeReadyUseCase
import studio.lunabee.onesafe.domain.usecase.clipboard.ClipboardScheduleClearUseCase
import studio.lunabee.onesafe.domain.usecase.clipboard.ClipboardShouldClearUseCase
import studio.lunabee.onesafe.domain.usecase.settings.SetSecuritySettingUseCase
import studio.lunabee.onesafe.feature.clipboard.model.ClipboardClearDelay
import javax.inject.Inject
import kotlin.time.Duration

private val logger = LBLogger.get<BackgroundCleanerObserver>()

/**
 * Global app lifecycle observer to trigger stuff on events
 */
class BackgroundCleanerObserver @Inject constructor() : DefaultLifecycleObserver {

    @Inject lateinit var clipboardShouldClearUseCase: ClipboardShouldClearUseCase

    @Inject lateinit var setSecuritySettingUseCase: SetSecuritySettingUseCase

    @Inject lateinit var isSafeReadyUseCase: IsSafeReadyUseCase

    @Inject lateinit var clipboardScheduleClearUseCase: ClipboardScheduleClearUseCase

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        owner.lifecycleScope.launch {
            val safeId = isSafeReadyUseCase.safeIdFlow().firstOrNull()
            if (safeId != null) {
                handleClipboardClearing(safeId)
            } else {
                logger.e("Cannot start clipboard cleaner (no safe id loaded)")
            }
        }
    }

    private suspend fun handleClipboardClearing(safeId: SafeId) {
        val clearDelay = clipboardShouldClearUseCase(safeId)
        if (clearDelay != null) {
            val fixedDelay = realignDelayValueIfNeeded(clearDelay)
            clipboardScheduleClearUseCase.setup(fixedDelay, safeId)
        }
    }

    private suspend fun realignDelayValueIfNeeded(clearDelay: Duration): Duration {
        return if (clearDelay !in ClipboardClearDelay.filteredValues().map { it.value }) {
            val delay = ClipboardClearDelay.valueForDuration(clearDelay)
            setSecuritySettingUseCase.setClipboardClearDelay(delay.value)
            delay.value
        } else {
            clearDelay
        }
    }
}
