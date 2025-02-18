package studio.lunabee.onesafe.commonui.utils

import android.app.ActivityManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.os.Process
import com.jakewharton.processphoenix.ProcessPhoenix
import kotlin.system.exitProcess

/**
 * Wrapper around [ProcessPhoenix] to support multi-process kill
 */
object OSProcessPhoenix {
    /**
     * Kill others processes then call [ProcessPhoenix.triggerRebirth]
     *
     * @see ProcessPhoenix.triggerRebirth
     */
    fun triggerRebirth(context: Context) {
        killAllProcesses(context = context)
        ProcessPhoenix.triggerRebirth(context)
    }

    /**
     * Kill all processes and finally the app.
     */
    fun triggerKill(context: Context) {
        killAllProcesses(context = context)
        exitProcess(status = 0)
    }

    /**
     * Kill all other processes associated to the application.
     */
    private fun killAllProcesses(context: Context) {
        val am = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val processesInfo = am.runningAppProcesses
        processesInfo?.forEach { process ->
            if (process.pid != Process.myPid()) {
                Process.killProcess(process.pid)
            }
        }
    }
}
