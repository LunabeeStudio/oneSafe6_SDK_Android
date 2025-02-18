package studio.lunabee.onesafe.feature.migration

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import androidx.core.os.bundleOf
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.error.OSAppError
import com.lunabee.lblogger.LBLogger
import com.lunabee.lblogger.e

private val logger = LBLogger.get<MigrationServiceConnection>()

class MigrationServiceConnection(
    private val clientMessenger: Messenger,
    private val pubKey: ByteArray,
    private val onError: (ServiceConnection, OSAppError) -> Unit,
) : ServiceConnection {

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val serverMessenger = Messenger(service)
        val msg: Message = Message.obtain(null, AppConstants.Migration.MsgPublicKeyWhat, 0, 0)

        msg.data = bundleOf(DATA_PUBLIC_KEY_KEY to pubKey)
        msg.replyTo = clientMessenger

        try {
            serverMessenger.send(msg)
        } catch (e: RemoteException) {
            logger.e(e)
        } finally {
            msg.recycle()
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
    }

    override fun onNullBinding(name: ComponentName?) {
        super.onNullBinding(name)
        onError(this, OSAppError(OSAppError.Code.MIGRATION_ONESAFE5_SERVICE_NULL_BINDING))
    }

    companion object {
        private const val DATA_PUBLIC_KEY_KEY: String = "DATA_PUBLIC_KEY_KEY"
    }
}
