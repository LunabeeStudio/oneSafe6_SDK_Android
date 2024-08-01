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

package studio.lunabee.onesafe.error

import co.touchlab.kermit.Logger
import com.lunabee.lbcore.model.LBResult
import com.lunabee.lblogger.LBLogger
import com.lunabee.lblogger.e

@PublishedApi
internal val log: Logger = LBLogger.get<OSError>()

sealed class OSError(
    message: String,
    cause: Throwable?,
    open val code: ErrorCode<*, OSError>,
) : Exception(message, cause) {

    interface ErrorCode<Code : Enum<Code>, out Err : OSError> {
        val message: String
        val name: String
    }

    companion object {
        inline fun <R> runCatching(
            logger: Logger? = null,
            noinline mapErr: ((OSError) -> OSError)? = null,
            noinline failureData: ((OSError) -> R?)? = null,
            block: () -> R,
        ): LBResult<R> {
            return try {
                LBResult.Success(block())
            } catch (e: OSError) {
                val error = mapErr?.invoke(e) ?: e
                logger?.e(error)
                LBResult.Failure(throwable = error, failureData = failureData?.invoke(error))
            }
        }

        /**
         * Unsafe getter for default error constructor
         */
        // TODO Lint rule to enforce param of OSError subclass's ctor
        inline fun <Code : Enum<Code>, reified Err : OSError> ErrorCode<Code, Err>.get(
            message: String? = this.message,
            cause: Throwable? = null,
        ): Err {
            val constructor = Err::class.java.constructors.first { !it.isSynthetic }
            val paramsType = constructor.parameterTypes
            val params = Array(paramsType.size) {
                when (paramsType[it]) {
                    this::class.java -> this
                    String::class.java -> message ?: this.message
                    Throwable::class.java -> cause
                    else -> {
                        log.e("Unexpected param in ${this::class.simpleName} constructor")
                        null
                    }
                }
            }
            @Suppress("SpreadOperator")
            return constructor.newInstance(*params) as Err
        }
    }
}
