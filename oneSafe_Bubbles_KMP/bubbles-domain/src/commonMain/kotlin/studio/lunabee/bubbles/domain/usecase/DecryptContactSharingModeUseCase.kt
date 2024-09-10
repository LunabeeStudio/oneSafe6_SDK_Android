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
 */

package studio.lunabee.bubbles.domain.usecase

import studio.lunabee.bubbles.domain.di.Inject
import studio.lunabee.bubbles.domain.model.MessageSharingMode
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID

class DecryptContactSharingModeUseCase @Inject constructor(
    private val contactLocalDecryptUseCase: ContactLocalDecryptUseCase,
) {
    suspend operator fun invoke(encSharingMode: ByteArray, contactId: DoubleRatchetUUID): MessageSharingMode? {
        return contactLocalDecryptUseCase.invoke(encSharingMode, contactId, MessageSharingMode::class).data
    }
}