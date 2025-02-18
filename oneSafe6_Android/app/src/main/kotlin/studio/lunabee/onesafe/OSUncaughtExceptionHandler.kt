package studio.lunabee.onesafe

import com.lunabee.lblogger.LBLogger
import com.lunabee.lblogger.e
import kotlinx.coroutines.runBlocking
import studio.lunabee.onesafe.domain.LoadFileCancelAllUseCase
import studio.lunabee.onesafe.domain.common.FeatureFlags
import studio.lunabee.onesafe.domain.repository.DatabaseEncryptionManager
import studio.lunabee.onesafe.domain.repository.DatabaseKeyRepository
import studio.lunabee.onesafe.error.OSDomainError
import studio.lunabee.onesafe.error.osCode
import javax.inject.Inject

private val logger = LBLogger.get<OSUncaughtExceptionHandler>()

class OSUncaughtExceptionHandler @Inject constructor(
    private val loadFileCancelAllUseCase: LoadFileCancelAllUseCase,
    private val featureFlags: FeatureFlags,
    private val databaseEncryptionManager: DatabaseEncryptionManager,
    private val databaseKeyRepository: DatabaseKeyRepository,
) : Thread.UncaughtExceptionHandler {
    private val defHandler = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(t: Thread, e: Throwable) {
        // Cancel & clean file loading
        runBlocking {
            loadFileCancelAllUseCase()
        }

        // Handle early database error
        when {
            isDatabaseKeyMissingError(e) -> logger.e(e) // No-op, don't crash on database key error
            isDatabaseKeyKeystoreError(e) -> {
                logger.e(e)
                runBlocking {
                    databaseKeyRepository.removeKey()
                    databaseKeyRepository.removeBackupKey()
                }
            }
            else -> defHandler?.uncaughtException(t, e)
        }
    }

    private fun isDatabaseKeyMissingError(e: Throwable) =
        featureFlags.sqlcipher() && databaseEncryptionManager.isMissingDatabaseKeyError(e)

    private fun isDatabaseKeyKeystoreError(e: Throwable) =
        featureFlags.sqlcipher() && e.osCode() == OSDomainError.Code.DATABASE_ENCRYPTION_KEY_KEYSTORE_LOST
}
