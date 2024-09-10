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

package studio.lunabee.bubbles.error

import co.touchlab.kermit.Logger
import com.lunabee.lbcore.model.LBResult
import com.lunabee.lblogger.LBLogger

@PublishedApi
internal val log: Logger = LBLogger.get<BubblesError>()

sealed class BubblesError(
    message: String,
    cause: Throwable?,
) : Exception(message, cause) {

    companion object {
        inline fun <R> runCatching(
            logger: Logger? = null,
            noinline mapErr: ((BubblesError) -> BubblesError)? = null,
            noinline failureData: ((BubblesError) -> R?)? = null,
            block: () -> R,
        ): LBResult<R> {
            return try {
                LBResult.Success(block())
            } catch (e: BubblesError) {
                val error = mapErr?.invoke(e) ?: e
                logger?.e(error.stackTraceToString())
                LBResult.Failure(throwable = error, failureData = failureData?.invoke(error))
            }
        }
    }
}
