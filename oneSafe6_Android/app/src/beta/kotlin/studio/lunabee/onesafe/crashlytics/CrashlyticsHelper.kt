package studio.lunabee.onesafe.crashlytics

import android.accounts.NetworkErrorException
import android.content.Context
import co.touchlab.kermit.Logger
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.lunabee.lblogger.LBCrashlyticsLogWriter
import com.lunabee.lblogger.LBLogger
import studio.lunabee.onesafe.BuildConfig
import studio.lunabee.onesafe.R
import studio.lunabee.onesafe.error.OSCryptoError
import java.net.UnknownHostException

private val logger = LBLogger.get<CrashlyticsHelper>()

object CrashlyticsHelper {
    fun setupFirebase(context: Context) {
        val appId: String = context.getString(R.string.google_app_id)
        val firebaseApiKey: String = context.getString(R.string.google_api_key)
        val storageBucket: String = context.getString(R.string.google_storage_bucket)
        val projectId: String = context.getString(R.string.project_id)

        val firebaseOptions = FirebaseOptions.Builder().apply {
            setApplicationId(appId)
            setApiKey(firebaseApiKey)
            setStorageBucket(storageBucket)
            setProjectId(projectId)
        }.build()

        FirebaseApp.initializeApp(context, firebaseOptions)
        Firebase.crashlytics.isCrashlyticsCollectionEnabled = BuildConfig.ENABLE_FIREBASE
    }

    fun setupLogger() {
        val exceptionListToIgnore = listOf(
            NetworkErrorException::class,
            UnknownHostException::class,
        )
        val osErrorCodeToIgnore = listOf(
            OSCryptoError.Code.DERIVATION_WITH_EMPTY_PASSWORD,
        )
        val lbCrashlyticsLogWriter = LBCrashlyticsLogWriter(
            exceptionListToIgnore = exceptionListToIgnore,
            shouldLog = { _, _, _, t ->
                osErrorCodeToIgnore.map { it.message }.contains(t?.message)
            },
        )

        Logger.addLogWriter(lbCrashlyticsLogWriter)
    }

    fun setCustomKey(key: CrashlyticsCustomKeys, value: String) {
        val formatValue = when (key) {
            CrashlyticsCustomKeys.MainNavScreen,
            CrashlyticsCustomKeys.BreadcrumbNavScreen,
            CrashlyticsCustomKeys.AutoFillNavScreen,
            CrashlyticsCustomKeys.MoveNavScreen,
            -> value.substringBefore('?').substringBefore('/')
        }
        logger.v("Set custom key: <$key, $formatValue>")
        Firebase.crashlytics.setCustomKey(key.name, formatValue)
    }
}
