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
 * Created by Lunabee Studio / Date - 6/24/2024 - for the oneSafe6 SDK.
 * Last modified 6/24/24, 9:39 AM
 */

package studio.lunabee.onesafe.importexport

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.importexport.data.GoogleDrivePreferencesRepository
import javax.inject.Inject

class GetGoogleDriveSettingUseCase @Inject constructor(
    private val settingRepository: GoogleDrivePreferencesRepository,
    private val safeRepository: SafeRepository,
) {
    suspend fun selectedAccount(currentSafeId: SafeId? = null): String? {
        val safeId = currentSafeId ?: safeRepository.currentSafeId()
        return settingRepository.selectedAccount(safeId)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun selectedAccountFlow(currentSafeId: SafeId? = null): Flow<String?> = currentSafeId?.let {
        settingRepository.selectedAccountFlow(currentSafeId)
    } ?: safeRepository.currentSafeIdFlow().flatMapLatest { retrievedSafeId ->
        retrievedSafeId?.let {
            settingRepository.selectedAccountFlow(retrievedSafeId)
        } ?: flowOf()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun folderId(currentSafeId: SafeId? = null): Flow<String?> = currentSafeId?.let {
        settingRepository.folderIdFlow(currentSafeId)
    } ?: safeRepository.currentSafeIdFlow().flatMapLatest { retrievedSafeId ->
        retrievedSafeId?.let {
            settingRepository.folderIdFlow(retrievedSafeId)
        } ?: flowOf()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun folderUrl(currentSafeId: SafeId? = null): Flow<String?> = currentSafeId?.let {
        settingRepository.folderUrlFlow(currentSafeId)
    } ?: safeRepository.currentSafeIdFlow().flatMapLatest { retrievedSafeId ->
        retrievedSafeId?.let {
            settingRepository.folderUrlFlow(retrievedSafeId)
        } ?: flowOf()
    }
}
