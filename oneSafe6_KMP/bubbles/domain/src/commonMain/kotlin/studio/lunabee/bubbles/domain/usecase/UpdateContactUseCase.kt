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
 * Created by Lunabee Studio / Date - 8/23/2023 - for the oneSafe6 SDK.
 * Last modified 23/08/2023 09:41
 */

package studio.lunabee.bubbles.domain.usecase

import com.lunabee.lbcore.model.LBResult
import kotlinx.datetime.Clock
import studio.lunabee.onesafe.di.Inject
import studio.lunabee.bubbles.domain.model.MessageSharingMode
import studio.lunabee.bubbles.domain.model.contactkey.ContactLocalKey
import studio.lunabee.bubbles.domain.repository.BubblesCryptoRepository
import studio.lunabee.bubbles.domain.repository.ContactKeyRepository
import studio.lunabee.bubbles.domain.repository.ContactRepository
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.onesafe.domain.model.crypto.EncryptEntry
import studio.lunabee.onesafe.error.OSError

class UpdateContactUseCase @Inject constructor(
    private val contactRepository: ContactRepository,
    private val contactKeyRepository: ContactKeyRepository,
    private val bubblesCryptoRepository: BubblesCryptoRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(
        id: DoubleRatchetUUID,
        sharingMode: MessageSharingMode,
        name: String,
    ): LBResult<Unit> = OSError.runCatching {
        val localKey: ContactLocalKey = contactKeyRepository.getContactLocalKey(id)
        contactRepository.updateContact(
            id = id,
            encSharingMode = bubblesCryptoRepository.localEncrypt(localKey, EncryptEntry(sharingMode)),
            encName = bubblesCryptoRepository.localEncrypt(localKey, EncryptEntry(name)),
            updateAt = clock.now(),
        )
    }
}
