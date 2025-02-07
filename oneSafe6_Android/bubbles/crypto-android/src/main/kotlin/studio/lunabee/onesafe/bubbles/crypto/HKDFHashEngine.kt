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
 * Created by Lunabee Studio / Date - 8/1/2023 - for the oneSafe6 SDK.
 * Last modified 01/08/2023 09:26
 */

package studio.lunabee.onesafe.bubbles.crypto

import org.bouncycastle.crypto.digests.SHA512Digest
import org.bouncycastle.crypto.generators.HKDFBytesGenerator
import org.bouncycastle.crypto.params.HKDFParameters
import studio.lunabee.bubbles.domain.crypto.BubblesDataHashEngine
import javax.inject.Inject

class HKDFHashEngine @Inject constructor() : BubblesDataHashEngine {
    private val hkdf = HKDFBytesGenerator(SHA512Digest())

    override fun deriveKey(key: ByteArray, salt: ByteArray, size: Int): ByteArray {
        val out = ByteArray(size)
        val hkdfParams = HKDFParameters(key, salt, null)
        hkdf.init(hkdfParams)
        hkdf.generateBytes(out, 0, size)
        return out
    }
}
