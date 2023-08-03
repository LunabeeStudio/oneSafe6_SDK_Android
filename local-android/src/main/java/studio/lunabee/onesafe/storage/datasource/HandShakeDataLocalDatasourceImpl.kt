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
 * Created by Lunabee Studio / Date - 7/6/2023 - for the oneSafe6 SDK.
 * Last modified 06/07/2023 10:40
 */

package studio.lunabee.onesafe.storage.datasource

import studio.lunabee.messaging.repository.datasource.HandShakeDataLocalDatasource
import studio.lunabee.onesafe.bubbles.domain.repository.BubblesCryptoRepository
import studio.lunabee.onesafe.bubbles.domain.repository.ContactKeyRepository
import studio.lunabee.onesafe.domain.model.crypto.DecryptEntry
import studio.lunabee.onesafe.domain.model.crypto.EncryptEntry
import studio.lunabee.onesafe.messaging.domain.model.EncHandShakeData
import studio.lunabee.onesafe.messaging.domain.model.HandShakeData
import studio.lunabee.onesafe.storage.dao.HandShakeDataDao
import studio.lunabee.onesafe.storage.model.RoomHandShakeData
import java.util.UUID
import javax.inject.Inject

// TODO rework KMP lib to have use case like GetHandShakeDataUseCase
class HandShakeDataLocalDatasourceImpl @Inject constructor(
    private val handShakeDataDao: HandShakeDataDao,
    private val bubblesCryptoRepository: BubblesCryptoRepository,
    private val contactKeyRepository: ContactKeyRepository,
) : HandShakeDataLocalDatasource {
    override suspend fun insert(handShakeData: HandShakeData) {
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

        handShakeDataDao.insert(RoomHandShakeData.fromHandShakeData(encHandShakeData))
    }

    override suspend fun delete(conversationLocalId: UUID) {
        handShakeDataDao.deleteById(conversationLocalId)
    }

    override suspend fun getById(conversationLocalId: UUID): HandShakeData? {
        val key = contactKeyRepository.getContactLocalKey(conversationLocalId)
        return handShakeDataDao.getById(conversationLocalId)?.toHandShakeData()?.let { encHandShakeData ->
            val decryptEntries = listOf(
                DecryptEntry(encHandShakeData.encConversationSharedId, UUID::class), // +0
                encHandShakeData.encOneSafePrivateKey?.let { DecryptEntry(it, ByteArray::class) }, // +1
                encHandShakeData.encOneSafePublicKey?.let { DecryptEntry(it, ByteArray::class) }, // +2
            )
            val plainEntries = bubblesCryptoRepository.localDecrypt(key, decryptEntries)
            HandShakeData(
                conversationLocalId,
                plainEntries[0] as UUID,
                plainEntries[1] as ByteArray?,
                plainEntries[2] as ByteArray?,
            )
        }
    }
}
