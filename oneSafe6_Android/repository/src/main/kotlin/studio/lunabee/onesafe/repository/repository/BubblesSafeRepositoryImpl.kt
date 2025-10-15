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
 * Created by Lunabee Studio / Date - 7/11/2024 - for the oneSafe6 SDK.
 * Last modified 11/07/2024 08:48
 */

package studio.lunabee.onesafe.repository.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import studio.lunabee.bubbles.domain.repository.BubblesSafeRepository
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.onesafe.di.Inject
import studio.lunabee.onesafe.domain.usecase.authentication.IsSafeReadyUseCase
import studio.lunabee.onesafe.error.OSRepositoryError
import studio.lunabee.onesafe.jvm.get
import studio.lunabee.onesafe.repository.datasource.SafeIdCacheDataSource

class BubblesSafeRepositoryImpl @Inject constructor(
    private val cacheDataSource: SafeIdCacheDataSource,
    private val isSafeReadyUseCase: IsSafeReadyUseCase,
) : BubblesSafeRepository {
    override suspend fun currentSafeId(): DoubleRatchetUUID = cacheDataSource.getSafeId()?.let { DoubleRatchetUUID(it.id) }
        ?: throw OSRepositoryError.Code.SAFE_ID_NOT_LOADED.get()

    override fun currentSafeIdFlow(): Flow<DoubleRatchetUUID?> = cacheDataSource.getSafeIdFlow().map {
        it?.let {
            DoubleRatchetUUID(
                it.id,
            )
        }
    }

    override fun isSafeReady(): Flow<Boolean> = isSafeReadyUseCase.flow()
}
