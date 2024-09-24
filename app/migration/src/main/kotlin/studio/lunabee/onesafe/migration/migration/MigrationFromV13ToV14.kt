/*
 * Copyright (c) 2024-2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 7/9/2024 - for the oneSafe6 SDK.
 * Last modified 7/8/24, 9:05 PM
 */

package studio.lunabee.onesafe.migration.migration

import com.lunabee.lbcore.model.LBResult
import com.lunabee.lblogger.LBLogger
import kotlinx.coroutines.flow.first
import studio.lunabee.bubbles.domain.model.MessageSharingMode
import studio.lunabee.bubbles.domain.repository.ContactKeyRepository
import studio.lunabee.bubbles.domain.repository.ContactRepository
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.migration.utils.MigrationCryptoV1UseCase
import studio.lunabee.onesafe.jvm.use
import studio.lunabee.onesafe.use
import javax.inject.Inject

private val logger = LBLogger.get<MigrationFromV13ToV14>()

class MigrationFromV13ToV14 @Inject constructor(
    private val contactRepository: ContactRepository,
    private val migrationCryptoV1UseCase: MigrationCryptoV1UseCase,
    private val contactKeyRepository: ContactKeyRepository,
) {
    /**
     * Migrate `isDeepLink` (Boolean) to [MessageSharingMode] for all contacts
     */
    suspend operator fun invoke(bubblesMasterKey: ByteArray?, safeId: SafeId): LBResult<Unit> = OSError.runCatching(logger) {
        contactRepository.getAllContactsFlow(DoubleRatchetUUID(safeId.id)).first().forEach { contact ->
            val encKey = contactKeyRepository.getContactLocalKey(contact.id).encKey

            val encSharingMode = migrationCryptoV1UseCase.decrypt(encKey, bubblesMasterKey!!).use { key ->
                val isDeeplinkEnabled = migrationCryptoV1UseCase.decrypt(contact.encSharingMode, key)[0] == 1.toByte()
                val sharingMode = if (isDeeplinkEnabled) MessageSharingMode.Deeplink else MessageSharingMode.CypherText
                migrationCryptoV1UseCase.encrypt(sharingMode.id.encodeToByteArray(), key)
            }
            contactRepository.updateMessageSharingMode(contact.id, encSharingMode, contact.updatedAt)
        }
    }
}
