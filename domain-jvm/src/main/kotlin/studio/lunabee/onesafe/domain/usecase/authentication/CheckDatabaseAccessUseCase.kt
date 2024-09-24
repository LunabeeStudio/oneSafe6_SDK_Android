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
 * Last modified 3/29/24, 5:40 PM
 */

package studio.lunabee.onesafe.domain.usecase.authentication

import com.lunabee.lbcore.model.LBResult
import kotlinx.coroutines.flow.firstOrNull
import studio.lunabee.onesafe.domain.model.crypto.DatabaseKey
import studio.lunabee.onesafe.domain.repository.DatabaseEncryptionManager
import studio.lunabee.onesafe.domain.repository.DatabaseKeyRepository
import studio.lunabee.onesafe.error.OSCryptoError
import studio.lunabee.onesafe.error.OSDomainError
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.jvm.get
import studio.lunabee.onesafe.error.OSStorageError
import studio.lunabee.onesafe.error.osCode
import javax.inject.Inject

/**
 * Try to open access the main database with stored credentials
 */
class CheckDatabaseAccessUseCase @Inject constructor(
    private val encryptionManager: DatabaseEncryptionManager,
    private val databaseKeyRepository: DatabaseKeyRepository,
) {
    /**
     * @return [LBResult.Success] if the database is accessible or does not exist and the key is null
     */
    suspend operator fun invoke(): LBResult<Unit> {
        val result = OSError.runCatching(
            mapErr = {
                if (it.code == OSCryptoError.Code.ANDROID_KEYSTORE_KEY_PERMANENTLY_INVALIDATE) {
                    OSDomainError.Code.DATABASE_ENCRYPTION_KEY_KEYSTORE_LOST.get()
                } else {
                    it
                }
            },
        ) {
            databaseKeyRepository
                .getKeyFlow()
                .firstOrNull()
        }
        return when (result) {
            is LBResult.Failure -> LBResult.Failure(result.throwable)
            else -> invoke(result.data)
        }
    }

    /**
     * @return [LBResult.Success] if the database is accessible or does not exist and the key is null
     */
    operator fun invoke(key: DatabaseKey?): LBResult<Unit> {
        val result = OSError.runCatching { encryptionManager.checkDatabaseAccess(key) }

        return if ((result as? LBResult.Failure)?.throwable?.osCode() == OSStorageError.Code.DATABASE_NOT_FOUND && key == null) {
            LBResult.Success(Unit)
        } else {
            result
        }
    }
}
