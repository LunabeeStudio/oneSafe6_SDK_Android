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
 * Created by Lunabee Studio / Date - 8/21/2024 - for the oneSafe6 SDK.
 * Last modified 21/08/2024 14:27
 */

package studio.lunabee.onesafe.storage.datasource

import studio.lunabee.bubbles.domain.model.contact.Contact
import studio.lunabee.bubbles.domain.model.contactkey.ContactLocalKey
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.importexport.datasource.ImportExportBubblesLocalDatasource
import studio.lunabee.messaging.domain.model.EncConversation
import studio.lunabee.messaging.domain.model.SafeMessage
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.storage.MainDatabase
import studio.lunabee.onesafe.storage.dao.ContactDao
import studio.lunabee.onesafe.storage.dao.ContactKeyDao
import studio.lunabee.onesafe.storage.dao.DoubleRatchetConversationDao
import studio.lunabee.onesafe.storage.dao.MessageDao
import studio.lunabee.onesafe.storage.model.RoomContact
import studio.lunabee.onesafe.storage.model.RoomContactKey
import studio.lunabee.onesafe.storage.model.RoomDoubleRatchetConversation
import studio.lunabee.onesafe.storage.model.RoomMessage
import studio.lunabee.onesafe.storage.utils.TransactionProvider
import studio.lunabee.onesafe.storage.utils.runSQL
import javax.inject.Inject

class ImportExportBubblesLocalDatasourceImpl @Inject constructor(
    private val safeMessageDao: MessageDao,
    private val conversationDao: DoubleRatchetConversationDao,
    private val contactDao: ContactDao,
    private val keyDao: ContactKeyDao,
    private val transactionProvider: TransactionProvider<MainDatabase>,
) : ImportExportBubblesLocalDatasource {

    override suspend fun save(
        contacts: List<Contact>,
        conversations: List<EncConversation>,
        contactsKey: Map<DoubleRatchetUUID, ContactLocalKey>,
        messages: Map<Float, SafeMessage>,
        safeId: SafeId,
    ) {
        runSQL {
            transactionProvider.runAsTransaction {
                contactDao.deleteAll(safeId.id)
                contactDao.insertAll(contacts.map(RoomContact::fromBubblesContact))
                keyDao.insertAll(contactsKey.map { (id, key) -> RoomContactKey(id.uuid, key.encKey) })
                safeMessageDao.insertAll(messages.map { RoomMessage.fromMessage(it.value, it.key) })
                conversationDao.insertAll(conversations.map(RoomDoubleRatchetConversation::fromEncConversation))
            }
        }
    }

    override suspend fun getAllContactIds(): List<DoubleRatchetUUID> {
        return contactDao.getAllIds().map(::DoubleRatchetUUID)
    }

    override suspend fun getAllMessageIds(): List<DoubleRatchetUUID> {
        return safeMessageDao.getAllIds().map(::DoubleRatchetUUID)
    }

    override suspend fun getAllMessageByContactList(contactIds: List<DoubleRatchetUUID>): List<SafeMessage> {
        return safeMessageDao.getAllByContactList(contactIds.map { it.uuid }).map(RoomMessage::toMessage)
    }

    override suspend fun getEncConversations(ids: List<DoubleRatchetUUID>): List<EncConversation> {
        return conversationDao.getByIds(ids.map { it.uuid }).map { it.toEncConversation() }
    }
}
