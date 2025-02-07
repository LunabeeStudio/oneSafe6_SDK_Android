package studio.lunabee.onesafe.feature.migration

import android.content.Context
import android.content.ServiceConnection
import android.os.Handler
import android.os.Looper
import android.os.Message
import studio.lunabee.onesafe.AppConstants
import com.lunabee.lblogger.LBLogger
import java.lang.ref.WeakReference

private val logger = LBLogger.get<MigrationMessengerHandler>()

class MigrationMessengerHandler(
    private val context: WeakReference<Context>,
    private val setEncPassword: (encPassword: ByteArray) -> Unit,
) : Handler(Looper.getMainLooper()) {
    var serviceConnectionRef: WeakReference<ServiceConnection>? = null

    override fun handleMessage(msg: Message) {
        val serviceConnection = serviceConnectionRef?.get() ?: return

        if (msg.what == AppConstants.Migration.MsgEncPasswordWhat) {
            // Get encrypted password
            msg.data?.getByteArray(DATA_ENC_PASSWORD_KEY)?.let {
                setEncPassword(it)
            }
            context.get()?.unbindService(serviceConnection)
        } else {
            logger.e("Unexpected msg what = ${msg.what}")
        }
    }

    companion object {
        private const val DATA_ENC_PASSWORD_KEY: String = "DATA_ENC_PASSWORD_KEY"
    }
}
