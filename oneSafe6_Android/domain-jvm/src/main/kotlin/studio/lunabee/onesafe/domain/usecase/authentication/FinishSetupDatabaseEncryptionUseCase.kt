/*
 * Copyright (c) 2024 Lunabee Studio
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Created by Lunabee Studio / Date - 3/19/2024 - for the oneSafe6 SDK.
 * Last modified 3/19/24, 5:25 PM
 */

package studio.lunabee.onesafe.domain.usecase.authentication

import com.lunabee.lbcore.model.LBFlowResult
import com.lunabee.lbcore.model.LBFlowResult.Companion.transformResult
import com.lunabee.lbcore.model.LBResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import studio.lunabee.onesafe.domain.model.crypto.DatabaseKey
import studio.lunabee.onesafe.domain.repository.DatabaseEncryptionManager
import studio.lunabee.onesafe.domain.repository.DatabaseKeyRepository
import studio.lunabee.onesafe.domain.usecase.authentication.FinishSetupDatabaseEncryptionUseCase.SuccessState
import studio.lunabee.onesafe.error.OSCryptoError
import studio.lunabee.onesafe.error.OSDomainError
import studio.lunabee.onesafe.jvm.get
import studio.lunabee.onesafe.error.osCode
import javax.inject.Inject

/**
 * Finish the activation or deactivation of the database encryption. Must be call after [StartSetupDatabaseEncryptionUseCase] has run.
 * Whatever the [SuccessState], the database should be readable if a [LBFlowResult.Success] is emitted.
 *
 * @see DatabaseEncryptionManager
 */
class FinishSetupDatabaseEncryptionUseCase @Inject constructor(
    private val databaseKeyRepository: DatabaseKeyRepository,
    private val sqlCipherManager: DatabaseEncryptionManager,
    private val checkDatabaseAccessUseCase: CheckDatabaseAccessUseCase,
) {
    /**
     * @see FinishSetupDatabaseEncryptionUseCase
     */
    operator fun invoke(): Flow<LBFlowResult<SuccessState>> = flow {
        val key = databaseKeyRepository.getKeyFlow().first()
        val backupKey = databaseKeyRepository.getBackupKeyFlow().first()
        emitAll(
            sqlCipherManager.finishMigrationIfNeeded(key, backupKey).transformResult { finishResult ->
                val result = when (finishResult.successData) {
                    DatabaseEncryptionManager.MigrationState.Noop -> checkDatabaseAccess(backupKey)
                    DatabaseEncryptionManager.MigrationState.Done -> clearBackupKey()
                    DatabaseEncryptionManager.MigrationState.Canceled -> rollbackKey(backupKey)
                }
                emit(result)
            },
        )
    }.catch { e ->
        throw if (e.osCode() == OSCryptoError.Code.ANDROID_KEYSTORE_KEY_PERMANENTLY_INVALIDATE) {
            OSDomainError.Code.DATABASE_ENCRYPTION_KEY_KEYSTORE_LOST.get() // re-throw specific error for the OSUncaughtExceptionHandler
        } else {
            e
        }
    }

    private suspend fun checkDatabaseAccess(backupKey: DatabaseKey?): LBFlowResult<SuccessState> {
        val checkResult = checkDatabaseAccessUseCase()
        return when (checkResult) {
            is LBResult.Failure -> {
                // Check if the backup key (or null key) can open the database. This could happen as the key is created prior to the
                // new database, which might not be created if something wrong happens.
                val backupCheckResult = checkDatabaseAccessUseCase(backupKey)
                when (backupCheckResult) {
                    // In case of backup key check fails, considers the result as a real no-op
                    is LBResult.Failure -> LBFlowResult.Success(SuccessState.Noop)
                    is LBResult.Success -> rollbackKey(backupKey)
                }
            }
            is LBResult.Success -> LBFlowResult.Success(SuccessState.Noop)
        }
    }

    private suspend fun clearBackupKey(): LBFlowResult.Success<SuccessState> {
        databaseKeyRepository.removeBackupKey()
        return LBFlowResult.Success(SuccessState.Success)
    }

    private suspend fun rollbackKey(backupKey: DatabaseKey?): LBFlowResult.Success<SuccessState> {
        if (backupKey != null) {
            databaseKeyRepository.setKey(backupKey, true)
        } else {
            databaseKeyRepository.removeKey()
        }
        databaseKeyRepository.removeBackupKey()
        return LBFlowResult.Success(SuccessState.Canceled)
    }

    enum class SuccessState {
        /**
         * No encryption setup to finish
         */
        Noop,

        /**
         * Database encryption change succeeded
         */
        Success,

        /**
         * Database encryption change has been canceled
         */
        Canceled,
    }
}
