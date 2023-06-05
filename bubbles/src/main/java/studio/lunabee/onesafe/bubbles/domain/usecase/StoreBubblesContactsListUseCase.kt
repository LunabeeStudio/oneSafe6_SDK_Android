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
 * Created by Lunabee Studio / Date - 5/22/2023 - for the oneSafe6 SDK.
 * Last modified 5/22/23, 3:12 PM
 */

package studio.lunabee.onesafe.bubbles.domain.usecase

import studio.lunabee.onesafe.bubbles.domain.model.PlainBubblesContact
import studio.lunabee.onesafe.bubbles.domain.repository.BubblesContactRepository
import javax.inject.Inject

// TODO : Temporary class used to test the Bubbles feature with oneSafeK
class StoreBubblesContactsListUseCase @Inject constructor(
    private val bubblesContactRepository: BubblesContactRepository,
) {
    suspend operator fun invoke(plainContacts: List<PlainBubblesContact>) {
        val encryptedData = plainContacts.map { contact -> bubblesContactRepository.encryptPlainContact(contact) }
        bubblesContactRepository.storeContactsList(encryptedData)
    }
}
