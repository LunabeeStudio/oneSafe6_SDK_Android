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

package studio.lunabee.onesafe.test

import com.lunabee.lbcore.model.LBFlowResult
import com.lunabee.lbcore.model.LBResult
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.test.assertContentEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

/**
 * Not [assertContentEquals]
 */
fun assertContentNotEquals(illegal: ByteArray?, actual: ByteArray?, message: String? = null) {
    assertNot {
        assertContentEquals(illegal, actual, message)
    }
}

/**
 * Not [assertNot]
 */
fun assertNot(assertBlock: () -> Unit) {
    assertFailsWith(AssertionError::class) {
        assertBlock()
    }
}

// TODO move assertSuccess + assertFailure to lib?
/**
 * Wrapper for assertIs<LBResult.Success> which adds the result failure throwable as cause if applicable
 *
 * @see assertIs
 */
@OptIn(ExperimentalContracts::class)
inline fun <reified T> assertSuccess(value: LBResult<T>?, message: String? = null): LBResult.Success<T> {
    contract { returns() implies (value is LBResult.Success<T>) }
    return try {
        assertIs(value, message)
    } catch (e: AssertionError) {
        throw (value as? LBResult.Failure)?.throwable?.let { cause ->
            e.initCause(cause)
        } ?: e
    }
}

/**
 * Wrapper for assertIs<LBResult.Failure> which prints the value of the result in case of assertion failure
 *
 * @see assertIs
 */
@OptIn(ExperimentalContracts::class)
inline fun <reified T> assertFailure(value: LBResult<T>?, message: String? = null): LBResult.Failure<T> {
    contract { returns() implies (value is LBResult.Failure<T>) }
    return assertIs(value, listOfNotNull(message, value?.toString()).joinToString("\n"))
}

// TODO move assertSuccess + assertFailure to lib?
/**
 * Wrapper for assertIs<LBResult.Success> which adds the result failure throwable as cause if applicable
 *
 * @see assertIs
 */
@OptIn(ExperimentalContracts::class)
inline fun <reified T> assertSuccess(value: LBFlowResult<T>?, message: String? = null): LBFlowResult.Success<T> {
    contract { returns() implies (value is LBFlowResult.Success<T>) }
    return try {
        assertIs(value, message)
    } catch (e: AssertionError) {
        throw (value as? LBFlowResult.Failure)?.throwable?.let { cause ->
            e.initCause(cause)
        } ?: e
    }
}

/**
 * Wrapper for assertIs<LBFlowResult.Failure> which prints the value of the result in case of assertion failure
 *
 * @see assertIs
 */
@OptIn(ExperimentalContracts::class)
inline fun <reified T> assertFailure(value: LBFlowResult<T>?, message: String? = null): LBFlowResult.Failure<T> {
    contract { returns() implies (value is LBFlowResult.Failure<T>) }
    return assertIs(value, listOfNotNull(message, value?.toString()).joinToString("\n"))
}
