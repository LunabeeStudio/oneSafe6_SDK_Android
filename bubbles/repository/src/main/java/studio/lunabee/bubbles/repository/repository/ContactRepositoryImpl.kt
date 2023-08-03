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

package studio.lunabee.bubbles.repository.repository

import kotlinx.coroutines.flow.Flow
import studio.lunabee.bubbles.repository.datasource.ContactLocalDataSource
import studio.lunabee.onesafe.bubbles.domain.model.Contact
import studio.lunabee.onesafe.bubbles.domain.model.ContactLocalKey
import studio.lunabee.onesafe.bubbles.domain.model.ContactSharedKey
import studio.lunabee.onesafe.bubbles.domain.repository.ContactRepository
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class ContactRepositoryImpl @Inject constructor(
    private val localDataSource: ContactLocalDataSource,
) : ContactRepository {

    override suspend fun save(contact: Contact, key: ContactLocalKey): Unit = localDataSource.saveContact(contact, key)

    override fun getAllContactsFlow(): Flow<List<Contact>> = localDataSource.getAllContactsFlow()

    override suspend fun getContact(id: UUID): Flow<Contact?> = localDataSource.getContact(id)

    override suspend fun getSharedKey(id: UUID): ContactSharedKey? = localDataSource.getContactSharedKey(id)
    override suspend fun addContactSharedKey(id: UUID, sharedKey: ContactSharedKey) {
        localDataSource.addContactSharedKey(id, sharedKey)
    }

    override suspend fun clearAll(): Unit = localDataSource.clearAll()

    override suspend fun deleteContact(id: UUID) {
        localDataSource.deleteContact(id)
    }

    override suspend fun updateIsUsingDeeplink(id: UUID, encIsUsingDeeplink: ByteArray, updateAt: Instant) {
        localDataSource.updateIsUsingDeeplink(id, encIsUsingDeeplink, updateAt)
    }
}
