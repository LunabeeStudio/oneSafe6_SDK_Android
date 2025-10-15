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
 * Created by Lunabee Studio / Date - 6/24/2025 - for the oneSafe6 SDK.
 * Last modified 6/24/2025, 6:20 PM
 */

package studio.lunabee.bubbles.domain.usecase

import com.lunabee.lbcore.model.LBResult
import studio.lunabee.bubbles.domain.repository.BubblesCryptoRepository
import studio.lunabee.bubbles.domain.repository.ContactKeyRepository
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.onesafe.di.Inject
import studio.lunabee.onesafe.domain.model.crypto.EncryptEntry
import studio.lunabee.onesafe.error.OSError

/**
 * Encrypt any contact or message data
 */
class ContactLocalEncryptUseCase @Inject constructor(
    private val bubblesCryptoRepository: BubblesCryptoRepository,
    private val contactKeyRepository: ContactKeyRepository,
) {

    /**
     * Encrypt the raw data to type [Data]
     *
     * @param data The data to encrypt
     * @param contactId The ID of the linked contact
     * @param clazz The output type expected
     *
     * @return Encrypted data wrapped in a [LBResult]
     */
    suspend operator fun <Data : Any> invoke(data: Data, contactId: DoubleRatchetUUID): LBResult<ByteArray> = OSError
        .runCatching {
            val key = contactKeyRepository.getContactLocalKey(contactId)
            bubblesCryptoRepository.localEncrypt(key, EncryptEntry(data))
        }
}
