/*
 * Copyright (c) 2023-2023 Lunabee Studio
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
 * Last modified 6/14/23, 12:19 PM
 */

package studio.lunabee.onesafe.storage.datasource

import com.lunabee.lbextensions.mapValues
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import studio.lunabee.bubbles.domain.model.contact.Contact
import studio.lunabee.bubbles.domain.model.contactkey.ContactLocalKey
import studio.lunabee.bubbles.domain.model.contactkey.ContactSharedKey
import studio.lunabee.bubbles.repository.datasource.ContactLocalDataSource
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.onesafe.storage.MainDatabase
import studio.lunabee.onesafe.storage.dao.ContactDao
import studio.lunabee.onesafe.storage.dao.ContactKeyDao
import studio.lunabee.onesafe.storage.model.RoomContact
import studio.lunabee.onesafe.storage.model.RoomContactKey
import studio.lunabee.onesafe.storage.utils.TransactionProvider
import studio.lunabee.onesafe.storage.utils.runSQL
import javax.inject.Inject

class ContactLocalDataSourceImpl @Inject constructor(
    private val dao: ContactDao,
    private val keyDao: ContactKeyDao,
    private val transactionProvider: TransactionProvider<MainDatabase>,
) : ContactLocalDataSource {

    override suspend fun saveContact(contact: Contact, key: ContactLocalKey) {
        runSQL {
            transactionProvider.runAsTransaction {
                dao.insert(RoomContact.fromBubblesContact(contact))
                keyDao.insert(RoomContactKey(contact.id.uuid, key.encKey))
            }
        }
    }

    override fun getAllContactsFlow(safeId: DoubleRatchetUUID): Flow<List<Contact>> = dao.getAllInFlow(safeId.uuid)
        .mapValues { it.toContact() }

    override fun getRecentContactsFlow(maxNumber: Int, safeId: DoubleRatchetUUID): Flow<List<Contact>> =
        dao.getRecentContactsFlow(maxNumber, safeId.uuid).mapValues { it.toContact() }

    override fun getContactFlow(id: DoubleRatchetUUID): Flow<Contact?> = dao.getByIdFlow(id.uuid).map { it?.toContact() }
    override suspend fun getContact(id: DoubleRatchetUUID): Contact? {
        return dao.getById(id.uuid).let { it?.toContact() }
    }

    override suspend fun getContactInSafe(id: DoubleRatchetUUID, safeId: DoubleRatchetUUID): Contact? {
        return dao.getByIdInSafe(id.uuid, safeId.uuid).let { it?.toContact() }
    }

    override suspend fun getContactSharedKey(id: DoubleRatchetUUID): ContactSharedKey? = dao.getContactSharedKey(id.uuid)
        ?.let(::ContactSharedKey)

    override suspend fun addContactSharedKey(id: DoubleRatchetUUID, sharedKey: ContactSharedKey) {
        dao.addContactSharedKey(id.uuid, sharedKey.encKey)
    }

    override suspend fun deleteContact(id: DoubleRatchetUUID) {
        dao.remote(id.uuid)
    }

    override suspend fun updateMessageSharingMode(id: DoubleRatchetUUID, encSharingMode: ByteArray, updateAt: Instant) {
        dao.updateMessageSharingMode(id.uuid, encSharingMode, updateAt.toJavaInstant())
    }

    override suspend fun updateUpdatedAt(id: DoubleRatchetUUID, updateAt: Instant) {
        dao.updateUpdatedAt(id.uuid, updateAt.toJavaInstant())
    }

    override suspend fun updateContact(id: DoubleRatchetUUID, encSharingMode: ByteArray, encName: ByteArray, updateAt: Instant) {
        dao.updateContact(id.uuid, encSharingMode, encName, updateAt.toJavaInstant())
    }

    override suspend fun updateContactConsultedAt(id: DoubleRatchetUUID, consultedAt: Instant) {
        dao.updateContactConsultedAt(id.uuid, consultedAt.toJavaInstant())
    }

    override suspend fun getContactCount(safeId: DoubleRatchetUUID): Int {
        return dao.getContactCount(safeId.uuid)
    }

    override suspend fun updateContactResetConversationDate(id: DoubleRatchetUUID, encResetConversationDate: ByteArray) {
        dao.updateContactResetConversationDate(id.uuid, encResetConversationDate)
    }
}
