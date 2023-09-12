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
 * Created by Lunabee Studio / Date - 6/20/2023 - for the oneSafe6 SDK.
 * Last modified 6/20/23, 1:41 PM
 */

package studio.lunabee.onesafe.bubbles.domain.repository

import kotlinx.coroutines.flow.Flow
import studio.lunabee.onesafe.bubbles.domain.model.Contact
import studio.lunabee.onesafe.bubbles.domain.model.ContactLocalKey
import studio.lunabee.onesafe.bubbles.domain.model.ContactSharedKey
import java.time.Instant
import java.util.UUID

interface ContactRepository {
    suspend fun save(contact: Contact, key: ContactLocalKey)
    fun getAllContactsFlow(): Flow<List<Contact>>
    suspend fun getContact(id: UUID): Flow<Contact?>
    suspend fun getSharedKey(id: UUID): ContactSharedKey?
    suspend fun addContactSharedKey(id: UUID, sharedKey: ContactSharedKey)
    suspend fun clearAll()
    suspend fun deleteContact(id: UUID)
    suspend fun updateIsUsingDeeplink(id: UUID, encIsUsingDeeplink: ByteArray, updateAt: Instant)
    suspend fun updateUpdatedAt(id: UUID, updateAt: Instant)
    suspend fun updateContact(id: UUID, encIsUsingDeeplink: ByteArray, encName: ByteArray, updateAt: Instant)
    suspend fun updateContactConsultedAt(id: UUID, consultedAt: Instant)
}
