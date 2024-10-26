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
 * Created by Lunabee Studio / Date - 10/7/2024 - for the oneSafe6 SDK.
 * Last modified 9/11/24, 11:02 AM
 */

package studio.lunabee.onesafe.domain.utils

import java.security.MessageDigest
import javax.inject.Inject

class ShaEngine @Inject constructor() {
    fun sha256(input: ByteArray): ByteArray {
        val messageDigest = MessageDigest.getInstance(ALG_SHA_256)
        messageDigest.update(input)
        return messageDigest.digest()
    }

    fun sha256(input: String): ByteArray {
        return sha256(input.toByteArray())
    }

    companion object {
        private const val ALG_SHA_256 = "SHA-256"
    }
}
