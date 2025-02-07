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
 * Created by Lunabee Studio / Date - 5/5/2023 - for the oneSafe6 SDK.
 * Last modified 5/5/23, 10:41 AM
 */

package studio.lunabee.onesafe.domain.usecase.search

import com.lunabee.lbcore.model.LBResult
import com.lunabee.lblogger.LBLogger
import studio.lunabee.onesafe.domain.model.search.IndexWordEntry
import studio.lunabee.onesafe.domain.model.search.PlainIndexWordEntry
import studio.lunabee.onesafe.domain.repository.MainCryptoRepository
import studio.lunabee.onesafe.error.OSError
import javax.inject.Inject

private val log = LBLogger.get<DecryptIndexWordUseCase>()

class DecryptIndexWordUseCase @Inject constructor(
    private val cryptoRepository: MainCryptoRepository,
) {
    suspend operator fun invoke(encIndexWordEntry: List<IndexWordEntry>): LBResult<List<PlainIndexWordEntry>> = OSError.runCatching(log) {
        val encWords = encIndexWordEntry.map { it.encWord }
        val plainWords = cryptoRepository.decryptIndexWord(encWords)
        encIndexWordEntry.zip(plainWords) { indexWordEntry, plainWord ->
            PlainIndexWordEntry(
                word = plainWord,
                itemMatch = indexWordEntry.itemMatch,
                fieldMatch = indexWordEntry.fieldMatch,
            )
        }
    }
}
