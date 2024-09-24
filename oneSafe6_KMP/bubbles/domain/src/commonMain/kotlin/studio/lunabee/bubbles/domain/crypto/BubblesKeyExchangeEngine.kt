/*
 * Copyright (c) 2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 7/25/2024 - for the oneSafe6 SDK.
 * Last modified 25/07/2024 11:06
 */

package studio.lunabee.bubbles.domain.crypto

import studio.lunabee.bubbles.domain.model.BubblesKeyPair

interface BubblesKeyExchangeEngine {
    fun generateKeyPair(): BubblesKeyPair

    fun createSharedSecret(
        publicKey: ByteArray,
        privateKey: ByteArray,
        size: Int,
    ): ByteArray
}
