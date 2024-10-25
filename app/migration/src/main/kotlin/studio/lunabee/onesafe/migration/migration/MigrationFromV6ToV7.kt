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
 * Last modified 7/9/24, 9:41 AM
 */

package studio.lunabee.onesafe.migration.migration

import com.lunabee.lbcore.model.LBResult
import studio.lunabee.onesafe.cryptography.android.AndroidCryptoDataMapper
import studio.lunabee.onesafe.cryptography.android.CryptoEngine
import studio.lunabee.onesafe.domain.repository.SafeItemKeyRepository
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.domain.usecase.item.SortItemNameUseCase
import studio.lunabee.onesafe.error.OSCryptoError
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.jvm.get
import studio.lunabee.onesafe.migration.MigrationSafeData0
import javax.inject.Inject

/**
 * Compute alpha index for all items
 */
class MigrationFromV6ToV7 @Inject constructor(
    private val safeItemRepository: SafeItemRepository,
    private val safeItemKeyRepository: SafeItemKeyRepository,
    private val cryptoEngine: CryptoEngine,
    private val cryptoDataMapper: AndroidCryptoDataMapper,
    private val sortItemNameUseCase: SortItemNameUseCase,
) : AppMigration0(6, 7) {

    override suspend fun migrate(migrationSafeData: MigrationSafeData0): LBResult<Unit> = OSError.runCatching {
        val safeId = migrationSafeData.id
        val masterKey = migrationSafeData.masterKey
        val items = safeItemRepository.getAllSafeItemIdName(safeId).map { item ->
            val itemKey = safeItemKeyRepository.getSafeItemKey(item.id)
            val itemKeyRaw = cryptoEngine.decrypt(itemKey.encValue, masterKey, null).getOrElse {
                throw OSCryptoError.Code.ITEM_KEY_DECRYPTION_FAIL.get(cause = it)
            }
            val name = item.encName?.let { encName ->
                val plainName = cryptoEngine.decrypt(encName, itemKeyRaw, null).getOrElse {
                    throw OSCryptoError.Code.DECRYPTION_FAILED_WRONG_KEY.get(cause = it)
                }
                cryptoDataMapper(null, plainName, String::class)
            } ?: ""
            Pair(item.id, name)
        }
        val sortedItemsId = sortItemNameUseCase(items)
        safeItemRepository.setAlphaIndices(sortedItemsId)
    }
}
