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

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import studio.lunabee.onesafe.domain.model.safeitem.SafeItem
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemWithIdentifier
import studio.lunabee.onesafe.domain.model.search.PlainIndexWordEntry
import studio.lunabee.onesafe.domain.repository.ItemSettingsRepository
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import javax.inject.Inject

/**
 * Use case responsible of doing the match and ordering between a string and the index to return corresponding the [SafeItem] set
 */
class GetMatchFromSearchUseCase @Inject constructor(
    private val safeItemRepository: SafeItemRepository,
    private val itemSettingsRepository: ItemSettingsRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(searchValue: String, indexWordEntries: List<PlainIndexWordEntry>): Flow<List<SafeItemWithIdentifier>> {
        val keywords = SearchStringUtils.getListStringSearch(searchValue)

        val scoredMatches = indexWordEntries
            .filter { entry -> keywords.any { keyword -> entry.word.contains(keyword) } } // keep only matching index
            .groupBy { it.itemMatch } // group by item id
            .mapValues { entry ->
                val indexWords = indexWordEntries
                    .filter { it.fieldMatch == null && it.itemMatch == entry.key }
                    .map { wordEntry -> wordEntry.word }

                val wordsMatched = indexWords.count { word -> word in keywords }
                val keywordsMatched = keywords.count { keyword -> keyword in indexWords }
                val ratio = minOf(keywordsMatched.toFloat() / keywords.size, wordsMatched.toFloat() / indexWords.size)

                ScoredItemMatch(
                    titleMatchRate = ratio, // item entry match ratio
                    keywordsMatchScore = entry.value.distinctBy { it.word }.size, // distinct matched keyword
                    totalMatchScore = entry.value.size, // total count of matches
                )
            }

        return itemSettingsRepository.itemOrdering.flatMapLatest { itemOrder ->
            safeItemRepository.getSafeItemWithIdentifier(scoredMatches.keys, itemOrder).map { items ->
                items.sortedByDescending { scoredMatches[it.id] }
            }
        }
    }

    /**
     * @property titleMatchRate Min of keyword matched amount ratio and index word matched ratio
     * @property keywordsMatchScore The number of distinct matched keyword
     * @property totalMatchScore The total count of matches
     */
    private data class ScoredItemMatch(
        val titleMatchRate: Float,
        val keywordsMatchScore: Int,
        val totalMatchScore: Int,
    ) : Comparable<ScoredItemMatch> {
        override fun compareTo(other: ScoredItemMatch): Int = itemMatchComparator.compare(this, other)

        companion object {
            private val itemMatchComparator = compareBy<ScoredItemMatch>(
                { it.titleMatchRate },
                { it.keywordsMatchScore },
                { it.totalMatchScore },
            )
        }
    }
}
