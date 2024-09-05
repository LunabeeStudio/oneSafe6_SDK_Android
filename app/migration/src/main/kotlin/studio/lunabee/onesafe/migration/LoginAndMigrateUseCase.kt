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
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.getOrThrow
import studio.lunabee.onesafe.migration.migration.MigrationFromV0ToV1
import studio.lunabee.onesafe.migration.migration.MigrationFromV10ToV11
import studio.lunabee.onesafe.migration.migration.MigrationFromV11ToV12
import studio.lunabee.onesafe.migration.migration.MigrationFromV13ToV14
import studio.lunabee.onesafe.migration.migration.MigrationFromV14ToV15
import studio.lunabee.onesafe.migration.migration.MigrationFromV2ToV3
import studio.lunabee.onesafe.migration.migration.MigrationFromV3ToV4
import studio.lunabee.onesafe.migration.migration.MigrationFromV4ToV5
import studio.lunabee.onesafe.migration.migration.MigrationFromV5ToV6
import studio.lunabee.onesafe.migration.migration.MigrationFromV6ToV7
import studio.lunabee.onesafe.migration.migration.MigrationFromV7ToV8
import studio.lunabee.onesafe.migration.migration.MigrationFromV8ToV9
import studio.lunabee.onesafe.migration.migration.MigrationFromV9ToV10
import studio.lunabee.onesafe.migration.utils.MigrationCryptoUseCase
import studio.lunabee.onesafe.migration.utils.MigrationGetSafeCryptoUseCase
import studio.lunabee.onesafe.migration.utils.MigrationSafeData
import studio.lunabee.onesafe.use
import javax.crypto.Cipher
import javax.inject.Inject

// TODO <multisafe> unit test (especially login part)

private val logger = LBLogger.get<LoginAndMigrateUseCase>()

/**
 * Handle migrations required at sign in, and then do the sign in
 */
class LoginAndMigrateUseCase @Inject constructor(
    private val migrationFromV0ToV1: Lazy<MigrationFromV0ToV1>,
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
    private val migrationFromV13ToV14: Lazy<MigrationFromV13ToV14>,
    private val migrationFromV14ToV15: Lazy<MigrationFromV14ToV15>,
    private val mainCryptoRepository: MainCryptoRepository,
    @CryptoDispatcher private val dispatcher: CoroutineDispatcher,
    private val safeRepository: SafeRepository,
    private val migrationCryptoUseCase: MigrationCryptoUseCase,
    private val migrationGetSafeCryptoUseCase: MigrationGetSafeCryptoUseCase,
    private val loadSafeUseCase: LoadSafeUseCase,
) : LoginUseCase {

    override suspend operator fun invoke(password: CharArray): LBResult<Unit> = withContext(dispatcher) {
        val masterKeyResult = migrationGetSafeCryptoUseCase(password)
        when (masterKeyResult) {
            is LBResult.Failure -> LBResult.Failure(masterKeyResult.throwable)
            is LBResult.Success -> {
                val migrationSafeData = masterKeyResult.successData
                val masterKey = migrationSafeData.masterKey

                masterKey.use {
                    migrationSafeData.encBubblesKey?.let { bubblesKey ->
                        val bubblesMasterKey = migrationCryptoUseCase.decrypt(bubblesKey, masterKey, migrationSafeData.version)
                        doMigrations(migrationSafeData, bubblesMasterKey)
                    } ?: doMigrations(migrationSafeData, null)
                }
            }
        }
    }

    override suspend operator fun invoke(cipher: Cipher): LBResult<Unit> = withContext(dispatcher) {
        val migrationSafeData = migrationGetSafeCryptoUseCase(cipher).data
        val masterKey = migrationSafeData?.masterKey

        masterKey?.use {
            migrationSafeData.encBubblesKey?.let { bubblesKey ->
                val bubblesMasterKey = migrationCryptoUseCase.decrypt(bubblesKey, masterKey, migrationSafeData.version)
                doMigrations(migrationSafeData, bubblesMasterKey)
            } ?: doMigrations(migrationSafeData, null)
        } ?: LBResult.Failure()
    }

    private suspend fun doMigrations(
        migrationSafeData: MigrationSafeData,
        bubblesMasterKey: ByteArray?,
    ): LBResult<Unit> = OSError.runCatching {
        var version = migrationSafeData.version
        var failure: LBResult.Failure<Unit>? = null

        val runMigration: suspend (migration: suspend () -> LBResult<Unit>) -> Unit = { migration ->
            val result = migration()
            when (result) {
                is LBResult.Failure -> {
                    logger.e("Migration from $version to ${version + 1} failed")
                    failure = result
                }
                is LBResult.Success -> {
                    logger.i("Migration from $version to ${version + 1} succeeded")
                    version++
                    safeRepository.setSafeVersion(migrationSafeData.id, version)
                }
            }
        }

        if (version == 0) {
            runMigration {
                migrationFromV0ToV1.get()(migrationSafeData.masterKey)
            }
        }

        if (version == 1 && failure == null) {
            runMigration {
                // Deprecated migration due to datastore to room migration
                LBResult.Success(Unit)
            }
        }

        if (version == 2 && failure == null) {
            runMigration {
                migrationFromV2ToV3.get()()
            }
        }

        if (version == 3 && failure == null) {
            runMigration {
                migrationFromV3ToV4.get()()
            }
        }

        if (version == 4 && failure == null) {
            runMigration {
                migrationFromV4ToV5.get()()
            }
        }

        if (version == 5 && failure == null) {
            runMigration {
                migrationFromV5ToV6.get()(migrationSafeData.id)
            }
        }

        if (version == 6 && failure == null) {
            runMigration {
                migrationFromV6ToV7.get()(migrationSafeData.masterKey, migrationSafeData.id)
            }
        }

        if (version == 7 && failure == null) {
            runMigration {
                migrationFromV7ToV8.get()(migrationSafeData.id)
            }
        }

        if (version == 8 && failure == null) {
            runMigration {
                migrationFromV8ToV9.get()()
            }
        }

        if (version == 9 && failure == null) {
            runMigration {
                migrationFromV9ToV10.get()(migrationSafeData.masterKey, migrationSafeData.id)
            }
        }

        if (version == 10 && failure == null) {
            runMigration {
                migrationFromV10ToV11.get()(migrationSafeData.id)
            }
        }

        if (version == 11 && failure == null) {
            runMigration {
                migrationFromV11ToV12.get()(migrationSafeData.masterKey, migrationSafeData.id)
            }
        }

        if (version == 12 && failure == null) {
            runMigration {
                // Deprecated migration due to datastore to room migration (see MigrationToMultiSafeHelper::legacyMigrationFromV12ToV13)
                LBResult.Success(Unit)
            }
        }

        if (version == 13 && failure == null) {
            runMigration {
                migrationFromV13ToV14.get()(bubblesMasterKey, migrationSafeData.id)
            }
        }

        if (version == 14 && failure == null) {
            runMigration {
                migrationFromV14ToV15.get()(migrationSafeData)
            }
        }

        // ⚠️ Add further migration here, don't forget to bump MigrationConstant.LastVersion

        if (failure == null) {
            safeRepository.setSafeVersion(migrationSafeData.id, MigrationConstant.LastVersion)
            loadSafeUseCase(migrationSafeData.id)
            mainCryptoRepository.loadMasterKeyExternal(migrationSafeData.masterKey)

            if (migrationSafeData.version != version) {
                logger.i("Migration from v${migrationSafeData.version} to v$version succeeded")
            }
        } else {
            failure?.getOrThrow()
        }
    }
}
