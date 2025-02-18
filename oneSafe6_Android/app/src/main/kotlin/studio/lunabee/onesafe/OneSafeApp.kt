package studio.lunabee.onesafe

import android.annotation.SuppressLint
import android.app.Application
import android.os.Build
import android.os.Process
import androidx.emoji2.bundled.BundledEmojiCompatConfig
import androidx.emoji2.text.EmojiCompat
import androidx.hilt.work.HiltWorkerFactory
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import co.touchlab.kermit.Logger
import com.jakewharton.processphoenix.ProcessPhoenix.isPhoenixProcess
import com.lunabee.lblogger.LBLogger
import dagger.hilt.android.HiltAndroidApp
import dev.patrickgold.florisboard.FlorisManager
import dev.patrickgold.florisboard.FlorisManagerProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.common.BackgroundCleanerObserver
import studio.lunabee.onesafe.crashlytics.CrashlyticsHelper
import studio.lunabee.onesafe.domain.common.FeatureFlags
import studio.lunabee.onesafe.ime.InterceptEditorInstance
import studio.lunabee.onesafe.importexport.worker.AutoBackupWorkersHelper
import java.lang.reflect.Method
import javax.inject.Inject
import studio.lunabee.onesafe.ime.BuildConfig as ImeBuildConfig

private val logger = LBLogger.get<OneSafeApp>()

@HiltAndroidApp
class OneSafeApp : Application(), Configuration.Provider, FlorisManagerProvider {

    @Inject lateinit var backgroundCleanerObserver: dagger.Lazy<BackgroundCleanerObserver>

    @Inject lateinit var workerFactory: HiltWorkerFactory

    @Inject lateinit var autoBackupWorkersHelper: dagger.Lazy<AutoBackupWorkersHelper>

    @Inject lateinit var featureFlags: FeatureFlags

    @Inject lateinit var uncaughtExceptionHandler: dagger.Lazy<OSUncaughtExceptionHandler>

    val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val florisManager: FlorisManager by lazy {
        FlorisManager(
            context = lazy { this },
            editorInstance = lazy { InterceptEditorInstance(this) },
        )
    }

    override fun florisManager(): FlorisManager = florisManager

    override fun onCreate() {
        super.onCreate()

        val process = getProcess()
        logger.i("Launching OneSafeApp on process $process")

        if (process.ime && featureFlags.florisBoard()) {
            florisManager.initialize()
        }

        if (process.commonSetup) {
            setupFirebase()
            Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler.get())
            setupLogger()

            coroutineScope.launch {
                autoBackupWorkersHelper.get().ensureAutoBackupScheduled()
            }

            EmojiCompat.init(BundledEmojiCompatConfig(this, Dispatchers.Main.asExecutor()))
        }

        if (process.main) {
            ProcessLifecycleOwner.get().lifecycle.addObserver(backgroundCleanerObserver.get())
        }
    }

    private fun setupFirebase() {
        CrashlyticsHelper.setupFirebase(this)
    }

    private fun setupLogger() {
        if (!BuildConfig.DEBUG) {
            CrashlyticsHelper.setupLogger()
        }
        if (!BuildConfig.IS_DEV || !BuildConfig.DEBUG) {
            Logger.setLogWriters() // disable logs in prod release
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    private fun getProcess(): OSProcess {
        // https://stackoverflow.com/a/55842542/10935947
        val name = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getProcessName()
        } else {
            runCatching {
                @SuppressLint("PrivateApi")
                val activityThread: Class<*> = Class.forName("android.app.ActivityThread")

                @SuppressLint("DiscouragedPrivateApi")
                val getProcessName: Method = activityThread.getDeclaredMethod("currentProcessName")
                getProcessName.invoke(null) as? String
            }.getOrNull()
        }
        val pid = Process.myPid()
        return when {
            name == packageName + BuildConfig.MAIN_PROCESS_NAME -> OSProcess.Main(pid)
            name == packageName + BuildConfig.AUTOFILL_PROCESS_NAME -> OSProcess.Autofill(pid)
            name == packageName + ImeBuildConfig.IME_PROCESS_NAME -> OSProcess.Ime(pid)
            name == packageName + BuildConfig.DATABASE_SETUP_PROCESS_NAME -> OSProcess.DatabaseSetup(pid)
            isPhoenixProcess(this) -> OSProcess.Phoenix(pid)
            else -> OSProcess.Unknown
        }
    }
}
