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
 * Created by Lunabee Studio / Date - 6/22/2023 - for the oneSafe6 SDK.
 * Last modified 6/22/23, 10:52 AM
 */

package studio.lunabee.bubbles.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import studio.lunabee.bubbles.domain.model.contact.Contact
import studio.lunabee.bubbles.domain.model.contactkey.ContactLocalKey
import studio.lunabee.bubbles.domain.model.contactkey.ContactSharedKey
import studio.lunabee.bubbles.domain.repository.ContactRepository
import studio.lunabee.bubbles.repository.datasource.ContactLocalDataSource
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.onesafe.di.Inject

class ContactRepositoryImpl @Inject constructor(
    private val localDataSource: ContactLocalDataSource,
) : ContactRepository {

    override suspend fun save(contact: Contact, key: ContactLocalKey): Unit = localDataSource.saveContact(contact, key)

    override fun getAllContactsFlow(safeId: DoubleRatchetUUID): Flow<List<Contact>> = localDataSource.getAllContactsFlow(safeId)

    override fun getRecentContactsFlow(
        maxNumber: Int,
        safeId: DoubleRatchetUUID,
    ): Flow<List<Contact>> = localDataSource.getRecentContactsFlow(
        maxNumber,
        safeId,
    )

    override fun getContactFlow(id: DoubleRatchetUUID): Flow<Contact?> = localDataSource.getContactFlow(id)
    override suspend fun getContact(id: DoubleRatchetUUID): Contact? {
        return localDataSource.getContact(id)
    }

    override suspend fun getContactInSafe(id: DoubleRatchetUUID, safeId: DoubleRatchetUUID): Contact? {
        return localDataSource.getContactInSafe(id, safeId)
    }

    override suspend fun getSharedKey(id: DoubleRatchetUUID): ContactSharedKey? = localDataSource.getContactSharedKey(id)
    override suspend fun addContactSharedKey(id: DoubleRatchetUUID, sharedKey: ContactSharedKey) {
        localDataSource.addContactSharedKey(id, sharedKey)
    }

    override suspend fun deleteContact(id: DoubleRatchetUUID) {
        localDataSource.deleteContact(id)
    }

    override suspend fun updateMessageSharingMode(id: DoubleRatchetUUID, encSharingMode: ByteArray, updateAt: Instant) {
        localDataSource.updateMessageSharingMode(id, encSharingMode, updateAt)
    }

    override suspend fun updateUpdatedAt(id: DoubleRatchetUUID, updateAt: Instant) {
        localDataSource.updateUpdatedAt(id, updateAt)
    }

    override suspend fun updateContact(id: DoubleRatchetUUID, encSharingMode: ByteArray, encName: ByteArray, updateAt: Instant) {
        localDataSource.updateContact(id, encSharingMode, encName, updateAt)
    }

    override suspend fun updateContactConsultedAt(id: DoubleRatchetUUID, consultedAt: Instant) {
        localDataSource.updateContactConsultedAt(id, consultedAt)
    }

    override suspend fun getContactCount(safeId: DoubleRatchetUUID): Int {
        return localDataSource.getContactCount(safeId)
    }

    override suspend fun updateContactResetConversationDate(id: DoubleRatchetUUID, encResetConversationDate: ByteArray) {
        localDataSource.updateContactResetConversationDate(id, encResetConversationDate)
    }
}
