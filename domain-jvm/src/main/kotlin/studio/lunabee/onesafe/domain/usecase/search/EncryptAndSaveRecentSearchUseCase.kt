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

package studio.lunabee.onesafe.domain.usecase.search

import com.lunabee.lbcore.model.LBResult
import studio.lunabee.onesafe.domain.repository.MainCryptoRepository
import studio.lunabee.onesafe.domain.repository.RecentSearchRepository
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.domain.utils.ShaEngine
import studio.lunabee.onesafe.error.OSError
import java.time.Clock
import javax.inject.Inject

/**
 * Encrypt and save a recent search with its hash (sha256) and timestamp
 */
class EncryptAndSaveRecentSearchUseCase @Inject constructor(
    private val safeRepository: SafeRepository,
    private val cryptoRepository: MainCryptoRepository,
    private val recentSearchRepository: RecentSearchRepository,
    private val clock: Clock,
    private val shaEngine: ShaEngine,
) {
    suspend operator fun invoke(plainRecentSearch: String): LBResult<Unit> = OSError.runCatching {
        val safeId = safeRepository.currentSafeId()
        val hash = shaEngine.sha256(plainRecentSearch)
        val encValue = cryptoRepository.encryptRecentSearch(plainRecentSearch)
        recentSearchRepository.saveRecentSearch(safeId, hash, encValue, clock.millis(), LimitRecentSearchSaved)
    }
}

private const val LimitRecentSearchSaved: Int = 12
