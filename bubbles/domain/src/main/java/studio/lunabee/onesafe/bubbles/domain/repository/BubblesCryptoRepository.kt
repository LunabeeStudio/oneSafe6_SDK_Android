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
 * Created by Lunabee Studio / Date - 6/14/2023 - for the oneSafe6 SDK.
 * Last modified 6/14/23, 3:15 PM
 */

package studio.lunabee.onesafe.bubbles.domain.repository

import studio.lunabee.onesafe.bubbles.domain.model.ContactLocalKey
import studio.lunabee.onesafe.bubbles.domain.model.ContactSharedKey
import studio.lunabee.onesafe.domain.model.crypto.DecryptEntry
import studio.lunabee.onesafe.domain.model.crypto.EncryptEntry
import java.io.OutputStream

interface BubblesCryptoRepository {
    suspend fun generateLocalKeyForContact(): ContactLocalKey
    suspend fun <Data : Any> localEncrypt(key: ContactLocalKey, encryptEntry: EncryptEntry<Data>): ByteArray
    suspend fun <Data : Any> localDecrypt(key: ContactLocalKey, decryptEntry: DecryptEntry<Data>): Data
    suspend fun sharedEncrypt(outputStream: OutputStream, localKey: ContactLocalKey, sharedKey: ContactSharedKey): OutputStream
    suspend fun sharedDecrypt(data: ByteArray, localKey: ContactLocalKey, sharedKey: ContactSharedKey): ByteArray
    suspend fun <Data : Any> queueEncrypt(encryptEntry: EncryptEntry<Data>): ByteArray
    suspend fun <Data : Any> queueDecrypt(decryptEntry: DecryptEntry<Data>): Data
}
