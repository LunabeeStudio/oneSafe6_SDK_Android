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
 * Created by Lunabee Studio / Date - 2/19/2024 - for the oneSafe6 SDK.
 * Last modified 2/19/24, 9:58 AM
 */

package studio.lunabee.onesafe.migration.utils

import androidx.core.util.AtomicFile
import studio.lunabee.onesafe.cryptography.android.CryptoEngine
import studio.lunabee.onesafe.cryptography.android.utils.OSCryptoInputStream
import studio.lunabee.onesafe.error.OSMigrationError
import studio.lunabee.onesafe.jvm.get
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.security.GeneralSecurityException
import javax.inject.Inject

/**
 * Use case to call crypto functions as of V1 migration (i.e without associated data). Catch and rethrow crypto exception for finner
 * handling in migration steps.
 */
class MigrationCryptoV1UseCase @Inject constructor(
    private val cryptoEngine: CryptoEngine,
) {
    fun decrypt(cipherData: ByteArray, key: ByteArray): ByteArray = try {
        cryptoEngine.decrypt(cipherData, key, null).getOrThrow()
    } catch (e: GeneralSecurityException) {
        throw OSMigrationError.Code.DECRYPT_FAIL.get(cause = e)
    }

    fun encrypt(plainData: ByteArray, key: ByteArray): ByteArray = try {
        cryptoEngine.encrypt(plainData, key, null).getOrThrow()
    } catch (e: GeneralSecurityException) {
        throw OSMigrationError.Code.ENCRYPT_FAIL.get(cause = e)
    }

    fun getDecryptStream(aFile: AtomicFile, plainKey: ByteArray): InputStream {
        val cryptoStream = try {
            cryptoEngine.getDecryptStream(aFile, plainKey, null)
        } catch (e: GeneralSecurityException) {
            throw OSMigrationError.Code.GET_DECRYPT_STREAM_FAIL.get(cause = e)
        }
        return OSCryptoInputStream(cryptoStream)
    }

    fun getCipherOutputStream(fileStream: FileOutputStream, plainKey: ByteArray): OutputStream = try {
        cryptoEngine.getCipherOutputStream(fileStream, plainKey, null)
    } catch (e: GeneralSecurityException) {
        throw OSMigrationError.Code.GET_ENCRYPT_STREAM_FAIL.get(cause = e)
    }
}
