/*
 * Copyright (c) 2024-2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 7/9/2024 - for the oneSafe6 SDK.
 * Last modified 7/9/24, 9:55 AM
 */

package studio.lunabee.onesafe.migration.utils

import androidx.core.util.AtomicFile
import kotlinx.coroutines.flow.firstOrNull
import studio.lunabee.onesafe.cryptography.android.DatastoreEngine
import studio.lunabee.onesafe.cryptography.android.qualifier.DataStoreType
import studio.lunabee.onesafe.cryptography.android.qualifier.DatastoreEngineProvider
import studio.lunabee.onesafe.cryptography.android.utils.OSCryptoInputStream
import studio.lunabee.onesafe.error.OSMigrationError
import studio.lunabee.onesafe.jvm.get
import studio.lunabee.onesafe.migration.MigrationConstant
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.security.GeneralSecurityException
import javax.inject.Inject

/**
 * Use case to call crypto functions for any versions
 */
class MigrationCryptoUseCase @Inject constructor(
    @DatastoreEngineProvider(DataStoreType.Plain) private val dataStoreEngine: DatastoreEngine,
    private val migrationCryptoV0UseCase: MigrationCryptoV0UseCase,
    private val migrationCryptoV1UseCase: MigrationCryptoV1UseCase,
) {
    suspend fun decrypt(cipherData: ByteArray, key: ByteArray, safeVersion: Int): ByteArray = try {
        if (safeVersion == 0) {
            dataStoreEngine.retrieveValue(MigrationConstant.DatastoreUsernameV0).firstOrNull()?.let { username ->
                migrationCryptoV0UseCase.decrypt(cipherData, key, username)
            } ?: throw OSMigrationError.Code.MISSING_LEGACY_USERNAME.get()
        } else {
            migrationCryptoV1UseCase.decrypt(cipherData, key)
        }
    } catch (e: GeneralSecurityException) {
        throw OSMigrationError.Code.DECRYPT_FAIL.get(cause = e)
    }

    suspend fun encrypt(plainData: ByteArray, key: ByteArray, safeVersion: Int): ByteArray = try {
        if (safeVersion == 0) {
            dataStoreEngine.retrieveValue(MigrationConstant.DatastoreUsernameV0).firstOrNull()?.let { username ->
                migrationCryptoV0UseCase.encrypt(plainData, key, username)
            } ?: throw OSMigrationError.Code.MISSING_LEGACY_USERNAME.get()
        } else {
            migrationCryptoV1UseCase.encrypt(plainData, key)
        }
    } catch (e: GeneralSecurityException) {
        throw OSMigrationError.Code.DECRYPT_FAIL.get(cause = e)
    }

    suspend fun getDecryptStream(aFile: AtomicFile, key: ByteArray, safeVersion: Int): InputStream {
        val cryptoStream = try {
            if (safeVersion == 0) {
                dataStoreEngine.retrieveValue(MigrationConstant.DatastoreUsernameV0).firstOrNull()?.let { username ->
                    migrationCryptoV0UseCase.getDecryptStream(aFile, key, username)
                } ?: throw OSMigrationError.Code.MISSING_LEGACY_USERNAME.get()
            } else {
                migrationCryptoV1UseCase.getDecryptStream(aFile, key)
            }
        } catch (e: GeneralSecurityException) {
            throw OSMigrationError.Code.GET_DECRYPT_STREAM_FAIL.get(cause = e)
        }
        return OSCryptoInputStream(cryptoStream)
    }

    suspend fun getCipherOutputStream(fileStream: FileOutputStream, key: ByteArray, safeVersion: Int): OutputStream = try {
        if (safeVersion == 0) {
            dataStoreEngine.retrieveValue(MigrationConstant.DatastoreUsernameV0).firstOrNull()?.let { username ->
                migrationCryptoV0UseCase.getCipherOutputStream(fileStream, key, username)
            } ?: throw OSMigrationError.Code.MISSING_LEGACY_USERNAME.get()
        } else {
            migrationCryptoV1UseCase.getCipherOutputStream(fileStream, key)
        }
    } catch (e: GeneralSecurityException) {
        throw OSMigrationError.Code.GET_ENCRYPT_STREAM_FAIL.get(cause = e)
    }
}
