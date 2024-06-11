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

package studio.lunabee.onesafe.bubbles.domain.usecase

import com.lunabee.lbcore.model.LBResult
import studio.lunabee.onesafe.bubbles.domain.model.ContactLocalKey
import studio.lunabee.onesafe.bubbles.domain.repository.BubblesCryptoRepository
import studio.lunabee.onesafe.bubbles.domain.repository.ContactKeyRepository
import studio.lunabee.onesafe.bubbles.domain.repository.ContactRepository
import studio.lunabee.onesafe.domain.model.crypto.EncryptEntry
import studio.lunabee.onesafe.error.OSError
import java.time.Clock
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class UpdateContactUseCase @Inject constructor(
    private val contactRepository: ContactRepository,
    private val contactKeyRepository: ContactKeyRepository,
    private val bubblesCryptoRepository: BubblesCryptoRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(
        id: UUID,
        isUsingDeeplink: Boolean,
        name: String,
    ): LBResult<Unit> = OSError.runCatching {
        val localKey: ContactLocalKey = contactKeyRepository.getContactLocalKey(id)
        contactRepository.updateContact(
            id = id,
            encIsUsingDeeplink = bubblesCryptoRepository.localEncrypt(localKey, EncryptEntry(isUsingDeeplink)),
            encName = bubblesCryptoRepository.localEncrypt(localKey, EncryptEntry(name)),
            updateAt = Instant.now(clock),
        )
    }
}
