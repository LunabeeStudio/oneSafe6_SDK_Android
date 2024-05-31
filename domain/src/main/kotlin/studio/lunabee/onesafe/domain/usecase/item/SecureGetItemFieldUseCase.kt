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
 * Created by Lunabee Studio / Date - 5/17/2023 - for the oneSafe6 SDK.
 * Last modified 5/17/23, 4:25 PM
 */

package studio.lunabee.onesafe.domain.usecase.item

import kotlinx.coroutines.flow.Flow
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemField
import studio.lunabee.onesafe.domain.repository.SafeItemFieldRepository
import studio.lunabee.onesafe.domain.usecase.authentication.IsCryptoDataReadyInMemoryUseCase
import java.util.UUID
import javax.inject.Inject

/**
 * Use case to retrieve item fields only if the master key is loaded
 */
class SecureGetItemFieldUseCase @Inject constructor(
    private val safeItemFieldRepository: SafeItemFieldRepository,
    private val isCryptoDataReadyInMemoryUseCase: IsCryptoDataReadyInMemoryUseCase,
) {
    /**
     * @param id the item id
     *
     * @return a flow of [SafeItemField] list or an empty flow master key is not loaded
     */
    operator fun invoke(id: UUID): Flow<List<SafeItemField>> = isCryptoDataReadyInMemoryUseCase.withCrypto(
        safeItemFieldRepository.getSafeItemFieldsFlow(id),
    )
}
