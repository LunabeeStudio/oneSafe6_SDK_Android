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
import com.lunabee.lblogger.e

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
                logger?.e(e)
                LBResult.Failure(throwable = error, failureData = failureData?.invoke(error))
            } catch (e: Exception) {
                // iOS key can be unloaded just after calling KMP code resulting in a crash
                // that we need to catch because we currently can't cancel a coroutine
                if (e::class.qualifiedName == "kotlin.native.internal.ObjCErrorException") {
                    val error = mapErr?.invoke(BubblesCryptoError(BubblesCryptoError.Code.BUBBLES_MASTER_KEY_NOT_LOADED, cause = e)) ?: e
                    logger?.e(e)
                    LBResult.Failure(
                        throwable = error,
                        failureData = failureData?.invoke(
                            BubblesCryptoError(
                                BubblesCryptoError.Code.BUBBLES_MASTER_KEY_NOT_LOADED,
                                cause = e,
                            ),
                        ),
                    )
                } else {
                    throw e
                }
            }
        }
    }
}
