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
import com.lunabee.lbcore.model.LBFlowResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

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

/**
 * Combine a [List] of [Flow] of [LBFlowResult] of [T] into a [Flow] of [LBFlowResult] of [List] of [T]
 * The returned result is:
 *   • [LBFlowResult.Loading] if any flow still emit loading result
 *   • [LBFlowResult.Failure] if any flow has emit a failure result and there is no more loading result
 *   • [LBFlowResult.Success] if all flows emit a success result
 * The operator ensure that the flow only emit a final result once (ie. [LBFlowResult.Failure] or [LBFlowResult.Success]) iff the input
 * flows do the same.
 *
 * @receiver list of flow to combine
 * @return combined flow of result
 */
fun <T> List<Flow<LBFlowResult<T>>>.combine(): Flow<LBFlowResult<List<T?>>> {
    return if (isEmpty()) {
        flowOf(LBFlowResult.Success(emptyList()))
    } else {
        kotlinx.coroutines.flow.combine(this) { results ->
            val loading = (size - results.count { it is LBFlowResult.Loading }) / size.toFloat()
            val data = results.map { it.data }
            val error = results.filterIsInstance<LBFlowResult.Failure<List<T>>>().firstOrNull()?.throwable
            when {
                loading < 1f -> LBFlowResult.Loading(partialData = data, progress = loading)
                results.any { it is LBFlowResult.Failure } -> LBFlowResult.Failure(throwable = error, failureData = data)
                results.all { it is LBFlowResult.Success } -> LBFlowResult.Success(successData = data)
                else -> LBFlowResult.Loading()
            }
        }
    }
}

fun <T> Flow<LBFlowResult<T>>.onFailure(block: suspend (LBFlowResult.Failure<T>) -> Unit): Flow<LBFlowResult<T>> =
    onEach {
        if (it is LBFlowResult.Failure) {
            block(it)
        }
    }
