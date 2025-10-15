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
import dagger.Lazy
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import studio.lunabee.onesafe.domain.qualifier.CryptoDispatcher
import studio.lunabee.onesafe.domain.repository.MainCryptoRepository
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.domain.usecase.authentication.LoadSafeUseCase
import studio.lunabee.onesafe.domain.usecase.authentication.LoginUseCase
import studio.lunabee.onesafe.domain.usecase.settings.SetSecuritySettingUseCase
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.error.OSMigrationError
import studio.lunabee.onesafe.jvm.get
import studio.lunabee.onesafe.migration.migration.AppMigration0
import studio.lunabee.onesafe.migration.migration.AppMigration15
import studio.lunabee.onesafe.migration.migration.MigrationFromV0ToV1
import studio.lunabee.onesafe.migration.migration.MigrationFromV10ToV11
import studio.lunabee.onesafe.migration.migration.MigrationFromV11ToV12
import studio.lunabee.onesafe.migration.migration.MigrationFromV12ToV13
import studio.lunabee.onesafe.migration.migration.MigrationFromV13ToV14
import studio.lunabee.onesafe.migration.migration.MigrationFromV14ToV15
import studio.lunabee.onesafe.migration.migration.MigrationFromV15ToV16
import studio.lunabee.onesafe.migration.migration.MigrationFromV16ToV17
import studio.lunabee.onesafe.migration.migration.MigrationFromV1ToV2
import studio.lunabee.onesafe.migration.migration.MigrationFromV2ToV3
import studio.lunabee.onesafe.migration.migration.MigrationFromV3ToV4
import studio.lunabee.onesafe.migration.migration.MigrationFromV4ToV5
import studio.lunabee.onesafe.migration.migration.MigrationFromV5ToV6
import studio.lunabee.onesafe.migration.migration.MigrationFromV6ToV7
import studio.lunabee.onesafe.migration.migration.MigrationFromV7ToV8
import studio.lunabee.onesafe.migration.migration.MigrationFromV8ToV9
import studio.lunabee.onesafe.migration.migration.MigrationFromV9ToV10
import studio.lunabee.onesafe.migration.utils.MigrationGetSafeCryptoUseCase
import studio.lunabee.onesafe.use
import javax.crypto.Cipher
import javax.inject.Inject

private val logger = LBLogger.get<LoginAndMigrateUseCase>()

/**
 * Handle migrations required at sign in, and then do the sign in
 */
class LoginAndMigrateUseCase @Inject constructor(
    private val migrationFromV0ToV1: Lazy<MigrationFromV0ToV1>,
    private val migrationFromV1ToV2: Lazy<MigrationFromV1ToV2>,
    private val migrationFromV2ToV3: Lazy<MigrationFromV2ToV3>,
    private val migrationFromV3ToV4: Lazy<MigrationFromV3ToV4>,
    private val migrationFromV4ToV5: Lazy<MigrationFromV4ToV5>,
    private val migrationFromV5ToV6: Lazy<MigrationFromV5ToV6>,
    private val migrationFromV6ToV7: Lazy<MigrationFromV6ToV7>,
    private val migrationFromV7ToV8: Lazy<MigrationFromV7ToV8>,
    private val migrationFromV8ToV9: Lazy<MigrationFromV8ToV9>,
    private val migrationFromV9ToV10: Lazy<MigrationFromV9ToV10>,
    private val migrationFromV10ToV11: Lazy<MigrationFromV10ToV11>,
    private val migrationFromV11ToV12: Lazy<MigrationFromV11ToV12>,
    private val migrationFromV12ToV13: Lazy<MigrationFromV12ToV13>,
    private val migrationFromV13ToV14: Lazy<MigrationFromV13ToV14>,
    private val migrationFromV14ToV15: Lazy<MigrationFromV14ToV15>,
    private val migrationFromV15ToV16: Lazy<MigrationFromV15ToV16>,
    private val migrationFromV16ToV17: Lazy<MigrationFromV16ToV17>,
    private val mainCryptoRepository: MainCryptoRepository,
    @CryptoDispatcher private val dispatcher: CoroutineDispatcher,
    private val safeRepository: SafeRepository,
    private val migrationGetSafeCryptoUseCase: MigrationGetSafeCryptoUseCase,
    private val loadSafeUseCase: LoadSafeUseCase,
    private val setSecuritySettingUseCase: SetSecuritySettingUseCase,
) : LoginUseCase {

    override suspend operator fun invoke(password: CharArray): LBResult<Unit> = withContext(dispatcher) {
        val masterKeyResult = migrationGetSafeCryptoUseCase(password)
        when (masterKeyResult) {
            is LBResult.Failure -> LBResult.Failure(masterKeyResult.throwable)
            is LBResult.Success -> {
                setSecuritySettingUseCase.setLastPasswordVerification(currentSafeId = masterKeyResult.successData.id)

                val migrationSafeData = masterKeyResult.successData
                val masterKey = migrationSafeData.masterKey

                masterKey.use {
                    doMigrations(migrationSafeData)
                }
            }
        }
    }

    override suspend operator fun invoke(cipher: Cipher): LBResult<Unit> = withContext(dispatcher) {
        val migrationSafeData = migrationGetSafeCryptoUseCase(cipher).data
        val masterKey = migrationSafeData?.masterKey

        masterKey?.use {
            doMigrations(migrationSafeData)
        } ?: LBResult.Failure()
    }

    private suspend fun doMigrations(
        migrationSafeData0: MigrationSafeData0,
    ): LBResult<Unit> = OSError.runCatching {
        var version = migrationSafeData0.version
        var failure: LBResult.Failure<Unit>? = null

        // ⚠️ Add further migration here, don't forget to bump MigrationConstant.LastVersion
        val migrations = listOf(
            migrationFromV0ToV1,
            migrationFromV1ToV2,
            migrationFromV2ToV3,
            migrationFromV3ToV4,
            migrationFromV4ToV5,
            migrationFromV5ToV6,
            migrationFromV6ToV7,
            migrationFromV7ToV8,
            migrationFromV8ToV9,
            migrationFromV9ToV10,
            migrationFromV10ToV11,
            migrationFromV11ToV12,
            migrationFromV12ToV13,
            migrationFromV13ToV14,
            migrationFromV14ToV15,
            migrationFromV15ToV16,
            migrationFromV16ToV17,
        )

        while (version < MigrationConstant.LastVersion) {
            val migration = runCatching {
                migrations.first { it.get().startVersion == version }
            }.getOrElse { e ->
                throw OSMigrationError.Code.MISSING_MIGRATION.get(
                    message = "Missing migration from $version to ${MigrationConstant.LastVersion}",
                    cause = e,
                )
            }.get()
            val result = when (migration) {
                is AppMigration0 -> migration.migrate(migrationSafeData = migrationSafeData0)
                is AppMigration15 -> migration.migrate(migrationSafeData = migrationSafeData0.as15())
            }
            when (result) {
                is LBResult.Failure -> {
                    logger
                        .e("Migration from ${migration.startVersion} to ${migration.endVersion} failed", result.throwable)
                    failure = result
                }
                is LBResult.Success -> {
                    logger.i("Migration from ${migration.startVersion} to ${migration.endVersion} succeeded")
                    version = migration.endVersion
                    safeRepository.setSafeVersion(migrationSafeData0.id, version)
                }
            }
        }

        if (failure == null) {
            safeRepository.setSafeVersion(migrationSafeData0.id, MigrationConstant.LastVersion)

            safeRepository.currentSafeIdOrNull()?.let {
                logger.e("Unexpected safe id loaded before login. Clearing it")
                safeRepository.clearSafeId()
            }

            loadSafeUseCase(migrationSafeData0.id)
            mainCryptoRepository.loadMasterKey(migrationSafeData0.masterKey)

            if (migrationSafeData0.version != version) {
                logger.i("Migration from v${migrationSafeData0.version} to v$version succeeded")
            }
        } else {
            failure.getOrThrow()
        }
    }
}
