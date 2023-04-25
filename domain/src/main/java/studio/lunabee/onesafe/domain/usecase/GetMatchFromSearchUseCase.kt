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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import studio.lunabee.onesafe.domain.model.safeitem.SafeItem
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemWithIdentifier
import studio.lunabee.onesafe.domain.model.search.ClearIndexWordEntry
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.domain.utils.StringUtils
import javax.inject.Inject

/**
 * Usecase responsible of doing the match between a string and the index to return corresponding the [SafeItem] set
 */
class GetMatchFromSearchUseCase @Inject constructor(
    private val safeItemRepository: SafeItemRepository,
) {
    operator fun invoke(searchValue: String, searchIndex: List<ClearIndexWordEntry>): Flow<LinkedHashSet<SafeItemWithIdentifier>> {
        val searchedWords = StringUtils.getListStringSearch(searchValue)
        val idMatching = searchedWords.flatMap { searchedWord ->
            searchIndex.filter { it.word.contains(searchedWord) }.map { it.itemMatch }
        }
        val finalIds = idMatching.groupingBy { it }.eachCount().filter { it.value >= searchedWords.size }
        return safeItemRepository.getSafeItemWithIdentifier(finalIds.keys.toList()).map { items ->
            val searchResult = LinkedHashSet<SafeItemWithIdentifier>()
            searchResult.addAll(items)
            searchResult
        }
    }
}
