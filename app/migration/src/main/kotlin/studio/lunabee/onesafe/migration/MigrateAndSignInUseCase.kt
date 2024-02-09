/*
 * Copyright (c) 2023 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 4/7/2023 - for the oneSafe6 SDK.
 * Last modified 4/7/23, 12:24 AM
 */

package studio.lunabee.onesafe.migration

import com.lunabee.lbcore.model.LBResult
import com.lunabee.lblogger.LBLogger
import studio.lunabee.onesafe.OSAppSettings
import studio.lunabee.onesafe.cryptography.BiometricEngine
import studio.lunabee.onesafe.cryptography.DatastoreEngine
import studio.lunabee.onesafe.cryptography.PasswordHashEngine
import studio.lunabee.onesafe.cryptography.qualifier.DataStoreType
import studio.lunabee.onesafe.cryptography.qualifier.DatastoreEngineProvider
import studio.lunabee.onesafe.domain.repository.MainCryptoRepository
import studio.lunabee.onesafe.domain.usecase.authentication.IsSignUpUseCase
import studio.lunabee.onesafe.use
import javax.crypto.Cipher
import javax.inject.Inject

private val logger = LBLogger.get<MigrateAndSignInUseCase>()

/**
 * Handle migrations required at sign in, and then do the sign in
 */
class MigrateAndSignInUseCase @Inject constructor(
    private val appSettings: OSAppSettings,
    private val migrationFromV0ToV1: MigrationFromV0ToV1,
    private val migrationFromV1ToV2: MigrationFromV1ToV2,
    private val migrationFromV2ToV3: MigrationFromV2ToV3,
    private val migrationFromV3ToV4: MigrationFromV3ToV4,
    private val migrationFromV4ToV5: MigrationFromV4ToV5,
    private val migrationFromV5ToV6: MigrationFromV5ToV6,
    private val migrationFromV6ToV7: MigrationFromV6ToV7,
    private val migrationFromV7ToV8: MigrationFromV7ToV8,
    private val migrationFromV8ToV9: MigrationFromV8ToV9,
    private val migrationFromV9ToV10: MigrationFromV9ToV10,
    private val migrationFromV10ToV11: MigrationFromV10ToV11,
    private val isSignUpUseCase: IsSignUpUseCase,
    private val mainCryptoRepository: MainCryptoRepository,
    biometricEngine: BiometricEngine,
    hashEngine: PasswordHashEngine,
    @DatastoreEngineProvider(DataStoreType.Plain) dataStoreEngine: DatastoreEngine,
) {
    private val migrationGetMasterKeyV0UseCase: MigrationGetMasterKeyV0UseCase = MigrationGetMasterKeyV0UseCase(
        biometricEngine,
        hashEngine,
        dataStoreEngine,
    )

    suspend fun needToMigrate(): Boolean = getCurrentVersion() < MigrationConstant.LastVersion

    suspend operator fun invoke(password: CharArray): LBResult<Unit> {
        val masterKey = migrationGetMasterKeyV0UseCase(password).data

        return masterKey?.use {
            doMigrations(it)
        } ?: LBResult.Failure()
    }

    suspend operator fun invoke(cipher: Cipher): LBResult<Unit> {
        val masterKey = migrationGetMasterKeyV0UseCase(cipher).data

        return masterKey?.use {
            doMigrations(it)
        } ?: LBResult.Failure()
    }

    private suspend fun doMigrations(masterKey: ByteArray): LBResult<Unit> {
        val initialVersion = getCurrentVersion()
        var version = initialVersion
        val results = mutableListOf<LBResult<Unit>>()

        if (version == 0) {
            results += migrationFromV0ToV1(masterKey)
            version++
        }

        if (version == 1) {
            results += migrationFromV1ToV2()
            version++
        }

        if (version == 2) {
            results += migrationFromV2ToV3()
            version++
        }

        if (version == 3) {
            results += migrationFromV3ToV4()
            version++
        }

        if (version == 4) {
            results += migrationFromV4ToV5()
            version++
        }

        if (version == 5) {
            results += migrationFromV5ToV6()
            version++
        }

        if (version == 6) {
            results += migrationFromV6ToV7(masterKey)
            version++
        }

        if (version == 7) {
            results += migrationFromV7ToV8()
            version++
        }

        if (version == 8) {
            results += migrationFromV8ToV9()
            version++
        }

        if (version == 9) {
            results += migrationFromV9ToV10(masterKey)
            version++
        }

        if (version == 10) {
            results += migrationFromV10ToV11()
            version++
        }

        // ⚠️ Add further migration here, don't forget to bump MigrationConstant.LastVersion

        val result = results.firstOrNull { it is LBResult.Failure } ?: LBResult.Success(Unit)

        if (result is LBResult.Success) {
            mainCryptoRepository.loadMasterKeyExternal(masterKey)
            appSettings.setMigrationVersionSetting(MigrationConstant.LastVersion)

            logger.i("Migration from v$initialVersion to v$version succeeded")
        }

        return result
    }

    private suspend fun getCurrentVersion(): Int {
        var version = appSettings.getMigrationVersionSetting()
        if (version == null && isSignUpUseCase()) { // Handle installs before MigrateAndSignInUseCase
            version = 0
        } else if (version == null) {
            version = MigrationConstant.LastVersion
        }
        return version
    }
}
