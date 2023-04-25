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

package studio.lunabee.onesafe.domain.usecase

import studio.lunabee.onesafe.domain.model.search.ClearIndexWordEntry
import studio.lunabee.onesafe.domain.model.search.IndexWordEntry
import studio.lunabee.onesafe.domain.repository.MainCryptoRepository
import studio.lunabee.onesafe.domain.utils.StringUtils
import java.util.UUID
import javax.inject.Inject

class CreateIndexWordEntriesFromItemUseCase @Inject constructor(
    private val cryptoRepository: MainCryptoRepository,
) {
    suspend operator fun invoke(name: String, id: UUID): List<IndexWordEntry> {
        val clearIndexWordEntry = StringUtils.getListStringSearch(name).map { word ->
            ClearIndexWordEntry(
                word = word,
                itemMatch = id,
                fieldMatch = null,
            )
        }

        return cryptoRepository.encryptIndexWord(clearIndexWordEntry)
    }
}
