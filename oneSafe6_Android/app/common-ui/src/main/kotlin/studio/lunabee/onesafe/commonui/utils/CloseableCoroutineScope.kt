package studio.lunabee.onesafe.commonui.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import java.io.Closeable

interface CloseableCoroutineScope : Closeable {
    val coroutineScope: CoroutineScope
}

class CloseableMainCoroutineScope(private vararg val closeables: Closeable) : CloseableCoroutineScope {

    override val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun close() {
        closeables.forEach { it.close() }
        coroutineScope.cancel()
    }
}
