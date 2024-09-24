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
 * Created by Lunabee Studio / Date - 5/24/2023 - for the oneSafe6 SDK.
 * Last modified 5/24/23, 10:44 AM
 */

package studio.lunabee.bubbles.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import studio.lunabee.onesafe.di.Inject
import studio.lunabee.bubbles.domain.model.contact.Contact
import studio.lunabee.bubbles.domain.repository.ContactRepository
import studio.lunabee.bubbles.domain.repository.BubblesSafeRepository

class GetRecentContactsUseCase @Inject constructor(
    private val bubblesSafeRepository: BubblesSafeRepository,
    private val bubblesContactRepository: ContactRepository,
) {
    /**
     * Retrieves a flow of the most recently updated contacts if the crypto data is ready.
     *
     * @param maxNumber The maximum number of contacts to retrieve.
     * @return A flow emitting a list of [Contact] objects sorted by [Contact.updatedAt] in descending order
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(maxNumber: Int): Flow<List<Contact>> {
        return bubblesSafeRepository.currentSafeIdFlow().flatMapLatest { safeId ->
            safeId?.let {
                bubblesContactRepository.getRecentContactsFlow(maxNumber, safeId)
            } ?: flowOf(emptyList())
        }
    }
}