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
import com.lunabee.lbcore.model.LBFlowResult.Companion.mapResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import studio.lunabee.onesafe.domain.repository.DatabaseEncryptionManager
import studio.lunabee.onesafe.domain.repository.DatabaseKeyRepository
import javax.inject.Inject

/**
 * Finish the activation or deactivation of the database encryption. Must be call after [StartSetupDatabaseEncryptionUseCase] has run.
 *
 * @see DatabaseEncryptionManager
 */
class FinishSetupDatabaseEncryptionUseCase @Inject constructor(
    private val databaseKeyRepository: DatabaseKeyRepository,
    private val sqlCipherManager: DatabaseEncryptionManager,
) {
    /**
     * @see FinishSetupDatabaseEncryptionUseCase
     */
    operator fun invoke(): Flow<LBFlowResult<SuccessState>> = flow {
        val key = databaseKeyRepository.getKeyFlow().first()
        val backupKey = databaseKeyRepository.getBackupKeyFlow().first()
        emitAll(
            sqlCipherManager.finishMigrationIfNeeded(key, backupKey).mapResult { state ->
                when (state) {
                    DatabaseEncryptionManager.MigrationState.Noop -> SuccessState.Noop
                    DatabaseEncryptionManager.MigrationState.Done -> {
                        databaseKeyRepository.removeBackupKey()
                        SuccessState.Success
                    }
                    DatabaseEncryptionManager.MigrationState.Canceled -> {
                        if (backupKey != null) {
                            databaseKeyRepository.setKey(backupKey)
                        } else {
                            databaseKeyRepository.removeKey()
                        }
                        databaseKeyRepository.removeBackupKey()
                        SuccessState.Canceled
                    }
                }
            },
        )
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
