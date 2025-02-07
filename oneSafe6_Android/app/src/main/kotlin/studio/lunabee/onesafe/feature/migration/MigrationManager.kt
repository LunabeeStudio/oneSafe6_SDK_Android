package studio.lunabee.onesafe.feature.migration

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Messenger
import android.util.Base64
import com.lunabee.lbcore.model.LBFlowResult
import com.lunabee.lbcore.model.LBResult
import com.lunabee.lblogger.LBLogger
import com.lunabee.lblogger.e
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.commonui.extension.getPackageInfoCompat
import studio.lunabee.onesafe.commonui.extension.hasSigningCertificateCompat
import studio.lunabee.onesafe.domain.model.importexport.ImportMode
import studio.lunabee.onesafe.domain.repository.MigrationCryptoRepository
import studio.lunabee.onesafe.error.OSAppError
import studio.lunabee.onesafe.importexport.usecase.MigrateOldArchiveUseCase
import java.lang.ref.WeakReference
import javax.inject.Inject

private val logger = LBLogger.get<MigrationManager>()

/**
 * Helper class for migration from oneSafe5
 */
class MigrationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val migrationCryptoRepository: MigrationCryptoRepository,
    private val migrateOldArchiveUseCase: MigrateOldArchiveUseCase,
) {
    private var encPassword: ByteArray? = null

    /**
     * Create the messenger and bind the service
     */
    fun initMigration(
        onResult: (result: LBResult<Unit>) -> Unit,
    ) {
        val migrationMessengerHandler = MigrationMessengerHandler(WeakReference(context)) { encPassword ->
            this.encPassword = encPassword
            onResult(LBResult.Success(Unit))
        }
        val clientMessenger = Messenger(migrationMessengerHandler)
        val migrationServiceConnection = MigrationServiceConnection(
            clientMessenger,
            migrationCryptoRepository.getMigrationPubKey(),
        ) { serviceConnection, error ->
            logger.e(error)
            onResult(LBResult.Failure(error))
            context.unbindService(serviceConnection)
        }
        migrationMessengerHandler.serviceConnectionRef = WeakReference(migrationServiceConnection)
        val serviceIntent = Intent(AppConstants.Migration.OldOneSafeService).apply {
            `package` = AppConstants.Migration.OldOneSafePackage
        }
        // Use Context.BIND_AUTO_CREATE flag so oneSafe5 service is started and can handle errors (if service was not already running)
        context.bindService(serviceIntent, migrationServiceConnection, Context.BIND_AUTO_CREATE)
    }

    /**
     * Get the migration flow
     */
    fun getMigrationFlow(
        importMode: ImportMode,
        archiveUri: Uri,
    ): Flow<LBFlowResult<Unit>> {
        val archiveStream = context.contentResolver.openInputStream(archiveUri)
        val encPassword = this@MigrationManager.encPassword
        return if (encPassword != null) {
            if (archiveStream != null) {
                flow {
                    archiveStream.use { inputStream ->
                        emitAll(
                            migrateOldArchiveUseCase(
                                importMode = importMode,
                                inputStream = inputStream,
                                encPassword = encPassword,
                            ),
                        )
                    }
                }
            } else {
                flowOf(LBFlowResult.Failure(OSAppError(OSAppError.Code.URI_INVALID)))
            }
        } else {
            flowOf(LBFlowResult.Failure(OSAppError(OSAppError.Code.MIGRATION_MISSING_PASSWORD)))
        }
    }

    companion object {
        /**
         * Check if the [intent] contains migration stuff and caller is allowed to run the migration
         */
        fun isAllowedMigrationIntent(activity: Activity, intent: Intent): Boolean {
            val actionCheck = intent.action == AppConstants.Migration.NewOneSafeMigrationIntentAction
            return if (actionCheck) {
                val allowMigration = activity.referrer?.host?.let { callingPackage ->
                    isMigrationAllowed(callingPackage, activity.packageManager)
                }

                if (allowMigration == null) {
                    logger.e("Unable to get calling package")
                }

                allowMigration ?: false
            } else {
                false
            }
        }

        private fun isMigrationAllowed(otherPackage: String, packageManager: PackageManager): Boolean {
            val packageMatch = otherPackage == AppConstants.Migration.OldOneSafePackage
            val packageInfo = packageManager.getPackageInfoCompat(
                packageName = otherPackage,
                flags = PackageManager.GET_PERMISSIONS,
            )
            val signature = Base64.decode(AppConstants.Migration.OldOneSafeSignature, Base64.NO_WRAP)
            val signatureMatch = packageManager.hasSigningCertificateCompat(otherPackage, signature)
            val isVersionSupported = packageInfo?.permissions?.any { it.name == AppConstants.Migration.OldOneSafeServicePermission }
                ?: false

            val allowMigration = packageMatch && signatureMatch && isVersionSupported
            if (!allowMigration) {
                val errMessage = "Could not migrate\n" +
                    "\tpackageMatch=$packageMatch\n" +
                    "\tpackageInfo=$packageInfo\n" +
                    "\tsignatureMatch=$signatureMatch\n" +
                    "\tisVersionSupported=$isVersionSupported\n"
                logger.e(errMessage)
            }

            return allowMigration
        }
    }
}
