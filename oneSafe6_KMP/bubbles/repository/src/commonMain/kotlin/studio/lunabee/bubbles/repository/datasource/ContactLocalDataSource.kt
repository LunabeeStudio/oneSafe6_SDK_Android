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

package studio.lunabee.bubbles.repository.datasource

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import studio.lunabee.bubbles.domain.model.contact.Contact
import studio.lunabee.bubbles.domain.model.contactkey.ContactLocalKey
import studio.lunabee.bubbles.domain.model.contactkey.ContactSharedKey
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID

interface ContactLocalDataSource {
    suspend fun saveContact(contact: Contact, key: ContactLocalKey)
    fun getAllContactsFlow(safeId: DoubleRatchetUUID): Flow<List<Contact>>
    fun getRecentContactsFlow(maxNumber: Int, safeId: DoubleRatchetUUID): Flow<List<Contact>>
    fun getContactFlow(id: DoubleRatchetUUID): Flow<Contact?>
    suspend fun getContact(id: DoubleRatchetUUID): Contact?
    suspend fun getContactInSafe(id: DoubleRatchetUUID, safeId: DoubleRatchetUUID): Contact?
    suspend fun getContactSharedKey(id: DoubleRatchetUUID): ContactSharedKey?
    suspend fun addContactSharedKey(id: DoubleRatchetUUID, sharedKey: ContactSharedKey)
    suspend fun deleteContact(id: DoubleRatchetUUID)
    suspend fun updateMessageSharingMode(id: DoubleRatchetUUID, encSharingMode: ByteArray, updateAt: Instant)
    suspend fun updateUpdatedAt(id: DoubleRatchetUUID, updateAt: Instant)
    suspend fun updateContact(id: DoubleRatchetUUID, encSharingMode: ByteArray, encName: ByteArray, updateAt: Instant)
    suspend fun updateContactConsultedAt(id: DoubleRatchetUUID, consultedAt: Instant)
    suspend fun getContactCount(safeId: DoubleRatchetUUID): Int
    suspend fun updateContactResetConversationDate(id: DoubleRatchetUUID, encResetConversationDate: ByteArray)
}
