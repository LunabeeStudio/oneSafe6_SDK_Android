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
 * Created by Lunabee Studio / Date - 7/17/2024 - for the oneSafe6 SDK.
 * Last modified 17/07/2024 15:13
 */

package studio.lunabee.messaging.domain

import com.lunabee.lbcore.model.LBResult
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.test.assertIs

// TODO move assertSuccess + assertFailure to lib?

/**
 * Wrapper for assertIs<LBResult.Success> which adds the result failure throwable as cause if applicable
 *
 * @see assertIs
 */
@OptIn(ExperimentalContracts::class)
inline fun <reified T> bubblesAssertSuccess(value: LBResult<T>?, message: String? = null): LBResult.Success<T> {
    contract { returns() implies (value is LBResult.Success<T>) }
    return try {
        assertIs(value, message)
    } catch (e: AssertionError) {
        throw (value as? LBResult.Failure)?.throwable?.let { cause ->
            e
        } ?: e
    }
}

/**
 * Wrapper for assertIs<LBResult.Failure> which prints the value of the result in case of assertion failure
 *
 * @see assertIs
 */
@OptIn(ExperimentalContracts::class)
inline fun <reified T> bubblesAssertFailure(value: LBResult<T>?, message: String? = null): LBResult.Failure<T> {
    contract { returns() implies (value is LBResult.Failure<T>) }
    return assertIs(value, listOfNotNull(message, value?.toString()).joinToString("\n"))
}
