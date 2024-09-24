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
 * Created by Lunabee Studio / Date - 5/29/2023 - for the oneSafe6 SDK.
 * Last modified 5/29/23, 8:13 AM
 */

package studio.lunabee.bubbles.domain.usecase

import kotlinx.coroutines.flow.Flow
import studio.lunabee.bubbles.domain.model.contact.Contact
import studio.lunabee.bubbles.domain.repository.ContactRepository
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.onesafe.di.Inject

class GetContactUseCase @Inject constructor(
    private val contactRepository: ContactRepository,
) {
    suspend operator fun invoke(id: DoubleRatchetUUID): Contact? {
        return contactRepository.getContact(id)
    }

    suspend operator fun invoke(id: DoubleRatchetUUID, safeId: DoubleRatchetUUID): Contact? {
        return contactRepository.getContactInSafe(id, safeId)
    }

    fun flow(id: DoubleRatchetUUID): Flow<Contact?> = contactRepository.getContactFlow(id)
}
