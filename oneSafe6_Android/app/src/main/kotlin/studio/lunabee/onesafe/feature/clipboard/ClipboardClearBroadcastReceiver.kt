package studio.lunabee.onesafe.feature.clipboard

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lunabee.lbextensions.getSerializableExtraCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.usecase.clipboard.ClipboardClearUseCase
import studio.lunabee.onesafe.qualifier.AppScope
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class ClipboardClearBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var clipboardClearUseCase: ClipboardClearUseCase

    @OptIn(DelicateCoroutinesApi::class)
    @Inject
    @AppScope
    lateinit var appScope: CoroutineScope

    override fun onReceive(context: Context?, intent: Intent?) {
        appScope.launch {
            val safeId = intent?.getSerializableExtraCompat<UUID>(SAFE_ID_CLIPBOARD_EXTRA)!!
            clipboardClearUseCase(SafeId(safeId))
        }
    }

    companion object {
        const val SAFE_ID_CLIPBOARD_EXTRA: String = "d5d5d41e-aa42-4907-af14-57d5d2f5ae0f"
    }
}
