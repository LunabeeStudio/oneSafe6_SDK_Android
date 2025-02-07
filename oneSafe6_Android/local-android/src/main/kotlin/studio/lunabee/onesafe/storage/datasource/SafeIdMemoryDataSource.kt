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
 * Created by Lunabee Studio / Date - 6/6/2024 - for the oneSafe6 SDK.
 * Last modified 6/6/24, 3:47 PM
 */

package studio.lunabee.onesafe.storage.datasource

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.error.OSStorageError
import studio.lunabee.onesafe.repository.datasource.SafeIdCacheDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SafeIdMemoryDataSource @Inject constructor() : SafeIdCacheDataSource {

    private val currentSafeId: MutableStateFlow<SafeId?> = MutableStateFlow(null)
    private var lastSafeId: SafeId? = null

    override fun loadSafeId(safeId: SafeId) {
        if (!currentSafeId.compareAndSet(null, safeId)) {
            throw OSStorageError(OSStorageError.Code.SAFE_ID_ALREADY_LOADED)
        }
        lastSafeId = safeId
    }

    override fun clearSafeId() {
        currentSafeId.value = null
    }

    override fun getSafeIdFlow(): StateFlow<SafeId?> = currentSafeId.asStateFlow()
    override suspend fun getSafeId(): SafeId? = currentSafeId.value
    override fun getLastSafeId(): SafeId? = lastSafeId
}
