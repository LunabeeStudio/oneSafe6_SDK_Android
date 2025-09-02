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
 * Created by Lunabee Studio / Date - 3/29/2024 - for the oneSafe6 SDK.
 * Last modified 3/29/24, 2:10 PM
 */

package studio.lunabee.onesafe.domain.usecase.authentication

import com.lunabee.lbcore.model.LBResult
import studio.lunabee.onesafe.domain.model.crypto.DatabaseKey
import studio.lunabee.onesafe.domain.repository.DatabaseEncryptionManager
import studio.lunabee.onesafe.domain.repository.DatabaseKeyRepository
import studio.lunabee.onesafe.error.OSError
import javax.inject.Inject

/**
 * Try to set the database key from external input. Does nothing if the key is wrong (i.e it cannot decrypt the database)
 */
class SetDatabaseKeyUseCase @Inject constructor(
    private val databaseKeyRepository: DatabaseKeyRepository,
    private val encryptionManager: DatabaseEncryptionManager,
) {
    suspend operator fun invoke(key: String): LBResult<Unit> = OSError.runCatching {
        DatabaseKey(key).use { dbKey ->
            val result = OSError.runCatching { encryptionManager.checkDatabaseAccess(dbKey) }
            when (result) {
                is LBResult.Failure -> result.getOrThrow()
                is LBResult.Success -> databaseKeyRepository.setKey(dbKey, true)
            }
        }
    }
}
