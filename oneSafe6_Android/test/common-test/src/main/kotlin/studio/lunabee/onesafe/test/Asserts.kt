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
import studio.lunabee.onesafe.error.OSError
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.typeOf
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

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
@Deprecated("Use assertFailure(value, expectedCode, message) overload")
@OptIn(ExperimentalContracts::class)
inline fun <reified T> assertFailure(value: LBResult<T>?, message: String? = null): LBResult.Failure<T> {
    contract { returns() implies (value is LBResult.Failure<T>) }
    return assertIs(value, listOfNotNull(message, value?.toString()).joinToString("\n"))
}

/**
 * Wrapper for assertIs<LBResult.Failure> which prints the value of the result in case of assertion failure
 *
 * @see assertIs
 */
@OptIn(ExperimentalContracts::class)
inline fun <reified T, reified Err : OSError, ErrCode : Enum<ErrCode>> assertFailure(
    value: LBResult<T>?,
    expectedCode: OSError.ErrorCode<ErrCode, Err>? = null,
    message: String? = null,
): LBResult.Failure<T> {
    contract { returns() implies (value is LBResult.Failure<T>) }
    val errMessage = listOfNotNull(message, value?.toString()).joinToString("\n")
    val throwable = assertIs<LBResult.Failure<T>>(value, errMessage).throwable
    expectedCode?.let {
        val actualCode = assertIs<Err>(throwable, errMessage).code
        assertEquals(expectedCode, actualCode, errMessage)
    }
    return value
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

inline fun <reified T : Any> assertPropertiesEquals(
    expected: T,
    actual: T,
    properties: Collection<KProperty1<T, *>> = T::class.memberProperties,
) {
    val byteArrayType = typeOf<ByteArray?>()
    properties.forEach { property ->
        if (property.returnType == byteArrayType) {
            val expectedProp = property.get(expected) as ByteArray?
            val actualProp = property.get(actual) as ByteArray?

            if (expectedProp != null) {
                assertContentEquals(expectedProp, actualProp, "Property ${property.name} equality failed")
            } else {
                assertNull(actualProp, "Property ${property.name} nullity failed")
            }
        } else {
            assertEquals(
                property.get(expected),
                property.get(actual),
                "Property ${property.name} equality failed",
            )
        }
    }
}

inline fun <reified T : Throwable> assertThrows(bloc: () -> Any?): T {
    val error = runCatching(bloc).exceptionOrNull()
    assertNotNull(error)
    assertIs<T>(error)
    return error
}

inline fun <T> assertDoesNotThrow(message: String? = null, bloc: () -> T): T {
    val result = runCatching(bloc)
    val error = result.exceptionOrNull()
    assertNull(error, message ?: error?.localizedMessage)
    return result.getOrThrow()
}
