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
 *
 * Created by Lunabee Studio / Date - 4/17/2024 - for the oneSafe6 SDK.
 * Last modified 4/17/24, 2:16 PM
 */

package studio.lunabee.onesafe.domain.usecase.authentication

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import studio.lunabee.onesafe.domain.repository.DatabaseKeyRepository
import javax.inject.Inject

class HasDatabaseKeyUseCase @Inject constructor(
    private val databaseKeyRepository: DatabaseKeyRepository,
) {
    operator fun invoke(): Flow<Boolean> = databaseKeyRepository.getKeyFlow().map { it != null }
}
