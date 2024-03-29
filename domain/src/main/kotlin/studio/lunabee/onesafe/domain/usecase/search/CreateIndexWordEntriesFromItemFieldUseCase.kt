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

import studio.lunabee.onesafe.domain.model.search.PlainIndexWordEntry
import studio.lunabee.onesafe.domain.model.search.IndexWordEntry
import studio.lunabee.onesafe.domain.model.search.ItemFieldDataToIndex
import studio.lunabee.onesafe.domain.repository.MainCryptoRepository
import javax.inject.Inject

class CreateIndexWordEntriesFromItemFieldUseCase @Inject constructor(
    private val cryptoRepository: MainCryptoRepository,
) {
    suspend operator fun invoke(
        data: List<ItemFieldDataToIndex>,
    ): List<IndexWordEntry> {
        val plainIndexWordEntry = data.filter {
            !it.isSecured && it.value.isNotEmpty()
        }.flatMap {
            SearchStringUtils.getListStringSearch(it.value).map { word ->
                PlainIndexWordEntry(
                    word = word,
                    itemMatch = it.itemId,
                    fieldMatch = it.fieldId,
                )
            }
        }
        return cryptoRepository.encryptIndexWord(plainIndexWordEntry)
    }
}
