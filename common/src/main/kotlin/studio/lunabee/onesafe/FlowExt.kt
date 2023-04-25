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

package studio.lunabee.onesafe

import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

fun <T : Any, R : Any> Flow<PagingData<T>>.mapPagingValues(transform: suspend (T) -> R): Flow<PagingData<R>> {
    return map { it.map(transform) }
}

/**
 * taken from https://github.com/Kotlin/kotlinx.coroutines/issues/1446
 * Allow to avoid collecting to many value from flow.
 * - If no value collected for [periodMillis], the next value will be emitted directly
 * - If values collected into the [periodMillis], only the last value within the period is emitted, the others are dropped
 */
fun <T> Flow<T>.throttle(periodMillis: Long): Flow<T> {
    return flow {
        conflate().collect { value ->
            emit(value)
            delay(periodMillis)
        }
    }
}
