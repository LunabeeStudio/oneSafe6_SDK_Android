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
 * Last modified 21/08/2024 14:25
 */

package studio.lunabee.importexport.datasource

import studio.lunabee.bubbles.domain.model.contact.Contact
import studio.lunabee.bubbles.domain.model.contactkey.ContactLocalKey
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.messaging.domain.model.EncConversation
import studio.lunabee.messaging.domain.model.SafeMessage
import studio.lunabee.onesafe.domain.model.safe.SafeId

interface ImportExportBubblesLocalDatasource {
    suspend fun save(
        contacts: List<Contact>,
        conversations: List<EncConversation>,
        contactsKey: Map<DoubleRatchetUUID, ContactLocalKey>,
        messages: Map<Float, SafeMessage>,
        safeId: SafeId,
    )

    suspend fun getAllContactIds(): List<DoubleRatchetUUID>
    suspend fun getAllMessageIds(): List<DoubleRatchetUUID>
    suspend fun getAllMessageByContactList(contactIds: List<DoubleRatchetUUID>): List<SafeMessage>
    suspend fun getEncConversations(ids: List<DoubleRatchetUUID>): List<EncConversation>
}
