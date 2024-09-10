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
 * Created by Lunabee Studio / Date - 7/13/2023 - for the oneSafe6 SDK.
 * Last modified 7/13/23, 8:07 AM
 */

package studio.lunabee.messaging.domain.repository

import studio.lunabee.bubbles.domain.model.DecryptEntry
import studio.lunabee.bubbles.domain.model.EncryptEntry
import studio.lunabee.doubleratchet.model.DRMessageKey

interface MessagingCryptoRepository {
    suspend fun <Data : Any> queueEncrypt(encryptEntry: EncryptEntry<Data>): ByteArray
    suspend fun <Data : Any> queueDecrypt(decryptEntry: DecryptEntry<Data>): Data
    suspend fun decryptMessage(data: ByteArray, key: DRMessageKey): ByteArray
    suspend fun encryptMessage(data: ByteArray, key: DRMessageKey): ByteArray
}
