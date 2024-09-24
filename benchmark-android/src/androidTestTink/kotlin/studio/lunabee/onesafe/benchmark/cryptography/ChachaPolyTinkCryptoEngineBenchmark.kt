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
 * Last modified 4/6/23, 9:06 AM
 */

package studio.lunabee.onesafe.benchmark.cryptography

import androidx.test.filters.LargeTest
import studio.lunabee.onesafe.cryptography.android.ChachaPolyTinkCryptoEngine
import studio.lunabee.onesafe.cryptography.android.CryptoEngine
import dagger.hilt.android.testing.HiltAndroidTest
import kotlin.test.Test

@LargeTest
@HiltAndroidTest
class ChachaPolyTinkCryptoEngineBenchmark : CryptoEngineBenchmark() {

    override val cryptoEngine: CryptoEngine = ChachaPolyTinkCryptoEngine(
        ivProvider = { CryptoBenchUtils.iv12 },
    )

    @Test
    fun small_chachaPoly1305_encrypt_decrypt_benchmark() {
        small_encrypt_decrypt_benchmark()
    }

    @Test
    fun large_chachaPoly1305_encrypt_decrypt_benchmark() {
        large_encrypt_decrypt_benchmark()
    }

    @Test
    fun file_chachaPoly1305_decrypt_benchmark() {
        file_decrypt_benchmark()
    }
}
