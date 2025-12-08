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
 * Created by Lunabee Studio / Date - 9/29/2023 - for the oneSafe6 SDK.
 * Last modified 9/29/23, 6:08 PM
 */

package studio.lunabee.onesafe.importexport.model

import studio.lunabee.bubbles.domain.model.contact.Contact
import studio.lunabee.bubbles.domain.model.contactkey.ContactLocalKey
import studio.lunabee.messaging.domain.model.EncConversation
import studio.lunabee.messaging.domain.model.SafeMessage
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemField
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemKey
import java.io.File

class ExportData(
    val safeItemsWithKeys: Map<ExportItem, SafeItemKey>,
    val safeItemFields: List<SafeItemField>,
    val icons: Set<File>,
    val files: Set<File>,
    val bubblesContactsWithKey: Map<Contact, ContactLocalKey>,
    val bubblesConversation: List<EncConversation>,
    val bubblesMessages: List<SafeMessage>,
)
