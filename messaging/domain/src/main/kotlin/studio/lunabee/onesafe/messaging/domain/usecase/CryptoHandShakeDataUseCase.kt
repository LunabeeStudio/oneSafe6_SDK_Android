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
 * Created by Lunabee Studio / Date - 6/3/2024 - for the oneSafe6 SDK.
 * Last modified 6/3/24, 9:01 AM
 */

package studio.lunabee.onesafe.messaging.domain.usecase

import studio.lunabee.onesafe.bubbles.domain.repository.BubblesCryptoRepository
import studio.lunabee.onesafe.bubbles.domain.repository.ContactKeyRepository
import studio.lunabee.onesafe.domain.model.crypto.DecryptEntry
import studio.lunabee.onesafe.domain.model.crypto.EncryptEntry
import studio.lunabee.onesafe.messaging.domain.model.EncHandShakeData
import studio.lunabee.onesafe.messaging.domain.model.HandShakeData
import java.util.UUID
import javax.inject.Inject

class CryptoHandShakeDataUseCase @Inject constructor(
    private val bubblesCryptoRepository: BubblesCryptoRepository,
    private val contactKeyRepository: ContactKeyRepository,
) {
    suspend fun encrypt(handShakeData: HandShakeData): EncHandShakeData {
        val key = contactKeyRepository.getContactLocalKey(handShakeData.conversationLocalId)
        val encryptEntries = listOf<EncryptEntry<Any>?>(
            EncryptEntry(handShakeData.conversationSharedId), // +0
            handShakeData.oneSafePrivateKey?.let { EncryptEntry(it) }, // +1
            handShakeData.oneSafePublicKey?.let { EncryptEntry(it) }, // +2
        )
        val encEntries = bubblesCryptoRepository.localEncrypt(key, encryptEntries)
        val encHandShakeData = EncHandShakeData(
            conversationLocalId = handShakeData.conversationLocalId,
            encConversationSharedId = encEntries[0]!!,
            encOneSafePrivateKey = encEntries[1],
            encOneSafePublicKey = encEntries[2],
        )
        return encHandShakeData
    }

    suspend fun decrypt(encHandShakeData: EncHandShakeData): HandShakeData {
        val conversationLocalId = encHandShakeData.conversationLocalId
        val key = contactKeyRepository.getContactLocalKey(conversationLocalId)
        val decryptEntries = listOf(
            DecryptEntry(encHandShakeData.encConversationSharedId, UUID::class), // +0
            encHandShakeData.encOneSafePrivateKey?.let { DecryptEntry(it, ByteArray::class) }, // +1
            encHandShakeData.encOneSafePublicKey?.let { DecryptEntry(it, ByteArray::class) }, // +2
        )
        val plainEntries = bubblesCryptoRepository.localDecrypt(key, decryptEntries)
        return HandShakeData(
            conversationLocalId,
            plainEntries[0] as UUID,
            plainEntries[1] as ByteArray?,
            plainEntries[2] as ByteArray?,
        )
    }
}
