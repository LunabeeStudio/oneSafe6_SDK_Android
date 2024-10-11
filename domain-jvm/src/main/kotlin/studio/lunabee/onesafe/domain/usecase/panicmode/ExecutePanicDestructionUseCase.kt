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
 * Created by Lunabee Studio / Date - 9/16/2024 - for the oneSafe6 SDK.
 * Last modified 16/09/2024 11:32
 */

package studio.lunabee.onesafe.domain.usecase.panicmode

import com.lunabee.lbcore.model.LBResult
import studio.lunabee.onesafe.di.Inject
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.domain.usecase.authentication.DeleteSafeUseCase

class ExecutePanicDestructionUseCase @Inject constructor(
    private val safeRepository: SafeRepository,
    private val deleteSafeUseCase: DeleteSafeUseCase,
) {

    suspend operator fun invoke(): LBResult<Unit> {
        return safeRepository.getSafeToDestroy().map {
            deleteSafeUseCase(it)
        }.lastOrNull() ?: LBResult.Success(Unit)
    }
}
