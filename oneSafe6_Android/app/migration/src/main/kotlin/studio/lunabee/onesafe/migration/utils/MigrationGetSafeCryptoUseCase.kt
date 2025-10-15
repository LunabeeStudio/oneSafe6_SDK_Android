/*
 * Copyright (c) 2023-2024 Lunabee Studio
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
 * Last modified 7/9/24, 9:39 AM
 */

package studio.lunabee.onesafe.migration.utils

import com.lunabee.lbcore.model.LBResult
import studio.lunabee.onesafe.cryptography.android.BiometricEngine
import studio.lunabee.onesafe.cryptography.android.PasswordHashEngine
import studio.lunabee.onesafe.domain.model.safe.SafeCrypto
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.repository.MainCryptoRepository.Companion.MasterKeyTestValue
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.domain.usecase.authentication.DisableBiometricUseCase
import studio.lunabee.onesafe.domain.usecase.authentication.IsSignUpUseCase
import studio.lunabee.onesafe.error.OSCryptoError
import studio.lunabee.onesafe.error.OSDomainError
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.error.OSMigrationError
import studio.lunabee.onesafe.jvm.get
import studio.lunabee.onesafe.migration.MigrationConstant
import studio.lunabee.onesafe.migration.MigrationSafeData0
import javax.crypto.Cipher
import javax.inject.Inject

/**
 * Retrieve the master key and other crypto keys manually
 * Notice that DB migrations happens before the Safe migrations so even if safe id has been add in V13, it does not change the master key
 * flow during migrations
 */
class MigrationGetSafeCryptoUseCase @Inject constructor(
    private val biometricEngine: BiometricEngine,
    private val hashEngine: PasswordHashEngine,
    private val safeRepository: SafeRepository,
    private val migrationCryptoUseCase: MigrationCryptoUseCase,
    private val isSignUpUseCase: IsSignUpUseCase,
    private val disableBiometricUseCase: DisableBiometricUseCase,
) {
    suspend operator fun invoke(password: CharArray): LBResult<MigrationSafeData0> = OSError.runCatching {
        val allSafe = safeRepository.getAllSafeOrderByLastOpenAsc()
        if (allSafe.isEmpty()) {
            throw OSDomainError.Code.SIGNIN_NOT_SIGNED_UP.get()
        } else {
            allSafe.firstNotNullOfOrNull { safeCrypto ->
                val key = hashEngine.deriveKey(password, safeCrypto.salt)
                testAndGetCrypto(safeCrypto, key)
            } ?: throw OSCryptoError(OSCryptoError.Code.NO_SAFE_MATCH_KEY)
        }
    }

    suspend operator fun invoke(cipher: Cipher): LBResult<MigrationSafeData0> = OSError.runCatching {
        val biometricSafe = safeRepository.getBiometricSafe()
        val encKey = biometricSafe.biometricCryptoMaterial ?: throw OSDomainError.Code.MISSING_BIOMETRIC_KEY.get()
        val key = try {
            biometricEngine.decryptKey(encKey, cipher)
        } catch (e: OSCryptoError) {
            // Fix corrupted state where biometric process was started but not completed
            if (e.code == OSCryptoError.Code.BIOMETRIC_DECRYPTION_FAIL) {
                disableBiometricUseCase()
            }
            throw e
        }
        safeRepository.getAllSafeOrderByLastOpenAsc().firstNotNullOfOrNull { safeCrypto ->
            testAndGetCrypto(safeCrypto, key)
        } ?: throw OSCryptoError(OSCryptoError.Code.NO_SAFE_MATCH_KEY)
    }

    private suspend fun testAndGetCrypto(
        safeCrypto: SafeCrypto,
        key: ByteArray,
    ): MigrationSafeData0? = try {
        val version = getCurrentVersion(safeCrypto.id)
        val plainMasterKeyTest = migrationCryptoUseCase
            .decrypt(safeCrypto.encTest, key, version)
            .decodeToString()
        if (plainMasterKeyTest == MasterKeyTestValue) {
            MigrationSafeData0(
                masterKey = key,
                version = version,
                id = safeCrypto.id,
                salt = safeCrypto.salt,
                encTest = safeCrypto.encTest,
                encIndexKey = safeCrypto.encIndexKey.takeUnless { it.contentEquals(byteArrayOf(0)) },
                encBubblesKey = safeCrypto.encBubblesKey.takeUnless { it.contentEquals(byteArrayOf(0)) },
                encItemEditionKey = safeCrypto.encItemEditionKey.takeUnless { it.contentEquals(byteArrayOf(0)) },
                biometricCryptoMaterial = safeCrypto.biometricCryptoMaterial,
            )
        } else {
            null
        }
    } catch (e: OSMigrationError) {
        if (e.code == OSMigrationError.Code.DECRYPT_FAIL) {
            null
        } else {
            throw (e)
        }
    }

    private suspend fun getCurrentVersion(safeId: SafeId): Int {
        var version = safeRepository.getSafeVersion(safeId)
        if (version == null && isSignUpUseCase()) { // Handle installs before LoginAndMigrateUseCase
            version = 0
        } else if (version == null) {
            version = MigrationConstant.LastVersion
        }
        return version
    }
}
