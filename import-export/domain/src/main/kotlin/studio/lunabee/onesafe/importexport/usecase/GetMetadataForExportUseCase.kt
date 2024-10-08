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
 * Last modified 9/29/23, 6:05 PM
 */

package studio.lunabee.onesafe.importexport.usecase

import com.lunabee.lbcore.model.LBResult
import studio.lunabee.bubbles.domain.repository.ContactRepository
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.onesafe.domain.model.importexport.ExportMetadata
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.error.OSError
import javax.inject.Inject

class GetMetadataForExportUseCase @Inject constructor(
    private val safeItemRepository: SafeItemRepository,
    private val safeRepository: SafeRepository,
    private val contactRepository: ContactRepository,
) {
    suspend operator fun invoke(): LBResult<ExportMetadata> = OSError.runCatching {
        val safeId = safeRepository.currentSafeId()
        val itemCount = safeItemRepository.getSafeItemsCount(safeId)
        val contactCount = contactRepository.getContactCount(DoubleRatchetUUID(safeId.id))
        ExportMetadata(
            itemCount = itemCount,
            contactCount = contactCount,
        )
    }
}
