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

package studio.lunabee.onesafe.cryptography.android.utils

import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import studio.lunabee.onesafe.error.OSCryptoError
import studio.lunabee.onesafe.randomize

@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
class SafeDataMutableStateFlow private constructor(
    private val overrideCode: OSCryptoError.Code,
    private val nullableCode: OSCryptoError.Code,
    private val internalFlow: MutableStateFlow<ByteArray?>,
) : MutableStateFlow<ByteArray?> by internalFlow {

    constructor(
        overrideCode: OSCryptoError.Code,
        nullableCode: OSCryptoError.Code,
    ) : this(overrideCode, nullableCode, MutableStateFlow(null))

    @set:Throws(OSCryptoError::class)
    @get:Throws(OSCryptoError::class)
    override var value: ByteArray?
        get() = internalFlow.value ?: throw OSCryptoError(nullableCode)
        set(value) {
            if (value == null) { // randomize before set null
                internalFlow.value?.randomize()
            } else if (internalFlow.value != null) {
                throw OSCryptoError(overrideCode)
            }

            internalFlow.value = value
        }
}
