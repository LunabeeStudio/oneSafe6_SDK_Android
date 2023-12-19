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
 * Created by Lunabee Studio / Date - 12/13/2023 - for the oneSafe6 SDK.
 * Last modified 12/13/23, 4:29 PM
 */

package studio.lunabee.onesafe.domain.usecase.item

import java.util.UUID
import javax.inject.Inject

/**
 * Sort a list of item names according to the natural ascending order, after [CleanForAlphaIndexingUseCase] transformation
 */
class SortItemNameUseCase @Inject constructor(
    private val cleanForAlphaIndexingUseCase: CleanForAlphaIndexingUseCase,
) {
    operator fun invoke(idNameList: List<Pair<UUID, String>>): List<Pair<UUID, Double>> {
        return idNameList
            .groupBy { cleanForAlphaIndexingUseCase(it.second) } // group to get equals index in case of equal clean name
            .toSortedMap() // use natural asc order
            .map { (_, value) ->
                value.map { it.first }
            } // map to list and keep id + index only
            .flatMapIndexed { index, ids -> // iterate over group of ids
                ids.map { id -> // iterate over ids
                    id to index.toDouble()
                }
            }
    }
}
