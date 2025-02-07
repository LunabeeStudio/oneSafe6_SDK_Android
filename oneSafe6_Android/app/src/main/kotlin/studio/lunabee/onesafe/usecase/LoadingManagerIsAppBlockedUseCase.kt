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
 * Created by Lunabee Studio / Date - 8/20/2024 - for the oneSafe6 SDK.
 * Last modified 8/20/24, 3:06 PM
 */

package studio.lunabee.onesafe.usecase

import com.lunabee.lbloading.LoadingManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import studio.lunabee.onesafe.domain.manager.IsAppBlockedUseCase

class LoadingManagerIsAppBlockedUseCase(private val loadingManager: LoadingManager) : IsAppBlockedUseCase {
    override fun flow(): Flow<Boolean> = loadingManager.loadingState.map { it.isBlocking }
    override suspend fun invoke(): Boolean = loadingManager.loadingState.value.isBlocking
}
