package studio.lunabee.onesafe.feature.autolock

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.messaging.domain.usecase.RemoveOldSentMessagesUseCase
import studio.lunabee.onesafe.commonui.utils.CloseableCoroutineScope
import studio.lunabee.onesafe.commonui.utils.CloseableMainCoroutineScope
import studio.lunabee.onesafe.domain.usecase.authentication.IsSafeReadyUseCase
import javax.inject.Inject

class DeleteOldSentMessageManager @Inject constructor(
    private val removeOldSentMessagesUseCase: RemoveOldSentMessagesUseCase,
    private val isSafeReadyUseCase: IsSafeReadyUseCase,
) : LifecycleEventObserver,
    CloseableCoroutineScope by CloseableMainCoroutineScope() {
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                coroutineScope.launch {
                    val safeId = isSafeReadyUseCase.safeIdFlow().filterNotNull().first()
                    removeOldSentMessagesUseCase.invoke(safeId = DoubleRatchetUUID(safeId.id))
                }
            }
            else -> {}
        }
    }
}
