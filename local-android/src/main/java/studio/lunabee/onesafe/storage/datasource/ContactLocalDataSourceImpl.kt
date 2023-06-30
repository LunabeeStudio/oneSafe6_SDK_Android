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
import studio.lunabee.bubbles.repository.datasource.ContactLocalDataSource
import studio.lunabee.onesafe.bubbles.domain.model.Contact
import studio.lunabee.onesafe.bubbles.domain.model.ContactLocalKey
import studio.lunabee.onesafe.bubbles.domain.model.ContactSharedKey
import studio.lunabee.onesafe.error.OSStorageError
import studio.lunabee.onesafe.storage.BubblesDatabase
import studio.lunabee.onesafe.storage.dao.ContactDao
import studio.lunabee.onesafe.storage.dao.ContactKeyDao
import studio.lunabee.onesafe.storage.model.RoomContact
import studio.lunabee.onesafe.storage.model.RoomContactKey
import studio.lunabee.onesafe.storage.utils.TransactionProvider
import studio.lunabee.onesafe.storage.utils.runSQL
import java.util.UUID
import javax.inject.Inject

class ContactLocalDataSourceImpl @Inject constructor(
    private val dao: ContactDao,
    private val keyDao: ContactKeyDao,
    private val transactionProvider: TransactionProvider<BubblesDatabase>,
) : ContactLocalDataSource {

    override suspend fun clearAll() {
        dao.clearTable()
    }

    override suspend fun saveContact(contact: Contact, key: ContactLocalKey) {
        runSQL {
            transactionProvider.runAsTransaction {
                dao.insert(RoomContact.fromBubblesContact(contact))
                keyDao.insert(RoomContactKey(contact.id, key))
            }
        }
    }

    override fun getAllContactsFlow(): Flow<List<Contact>> = dao.getAllInFlow().mapValues { it.toContact() }

    override suspend fun getContact(id: UUID): Contact? = dao.getById(id)?.toContact()

    override suspend fun getContactSharedKey(id: UUID): ContactSharedKey = dao.getContactSharedKey(id)
        ?: throw OSStorageError(OSStorageError.Code.CONTACT_NOT_FOUND)

    override suspend fun getAllContacts(): List<Contact> = dao.getAllContacts().map { it.toContact() }
}
