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
 * Created by Lunabee Studio / Date - 5/24/2023 - for the oneSafe6 SDK.
 * Last modified 5/24/23, 11:16 AM
 */

package studio.lunabee.bubbles.domain.usecase

import com.lunabee.lbcore.model.LBResult
import kotlinx.datetime.Instant
import studio.lunabee.bubbles.domain.repository.BubblesCryptoRepository
import studio.lunabee.bubbles.domain.repository.ContactKeyRepository
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.onesafe.di.Inject
import studio.lunabee.onesafe.domain.model.crypto.DecryptEntry
import studio.lunabee.onesafe.error.OSError
import kotlin.reflect.KClass

// TODO <bubbles> invoke overload to decrypt an array of data without retrieving the key for each data (see EncryptFieldsUseCase)

/**
 * Decrypt any contact or message data
 */
class ContactLocalDecryptUseCase @Inject constructor(
    private val bubblesCryptoRepository: BubblesCryptoRepository,
    private val contactKeyRepository: ContactKeyRepository,
) {

    /**
     * Decrypt the raw encrypted data to type [Data]
     *
     * @param data The data to decrypt
     * @param contactId The ID of the linked contact
     * @param clazz The output type expected
     *
     * @return Plain data wrapped in a [LBResult]
     */
    suspend operator fun <Data : Any> invoke(data: ByteArray, contactId: DoubleRatchetUUID, clazz: KClass<Data>): LBResult<Data> {
        return OSError.runCatching {
            val key = contactKeyRepository.getContactLocalKey(contactId)
            bubblesCryptoRepository.localDecrypt(key, DecryptEntry(data, clazz))
        }
    }

    suspend fun string(data: ByteArray, contactId: DoubleRatchetUUID): LBResult<String> {
        return invoke(data, contactId, String::class)
    }

    suspend fun uuid(data: ByteArray, contactId: DoubleRatchetUUID): LBResult<DoubleRatchetUUID> {
        return invoke(data, contactId, DoubleRatchetUUID::class)
    }

    suspend fun instant(data: ByteArray, contactId: DoubleRatchetUUID): LBResult<Instant> {
        return invoke(data, contactId, Instant::class)
    }

    suspend fun byteArray(data: ByteArray, contactId: DoubleRatchetUUID): LBResult<ByteArray> {
        return invoke(data, contactId, ByteArray::class)
    }
}
