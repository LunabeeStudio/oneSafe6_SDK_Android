/*
 * Copyright (c) 2024-2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 9/4/2024 - for the oneSafe6 SDK.
 * Last modified 9/4/24, 9:51 AM
 */

package studio.lunabee.onesafe.jvm

import co.touchlab.kermit.Logger
import com.lunabee.lblogger.LBLogger
import studio.lunabee.onesafe.error.OSError

@PublishedApi
internal val log: Logger = LBLogger.get<OSError>()

// TODO Lint rule to enforce param of OSError subclass's ctor

/**
 * Unsafe getter for default error constructor
 */
inline fun <Code : Enum<Code>, reified Err : OSError> OSError.ErrorCode<Code, Err>.get(
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
