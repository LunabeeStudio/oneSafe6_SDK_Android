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
 * Created by Lunabee Studio / Date - 6/24/2024 - for the oneSafe6 SDK.
 * Last modified 6/24/24, 3:40 PM
 */

package studio.lunabee.onesafe.importexport.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.importexport.model.CloudInfo
import studio.lunabee.onesafe.importexport.repository.CloudBackupRepository
import java.net.URI
import javax.inject.Inject

class GetCloudInfoUseCase @Inject constructor(
    private val cloudBackupRepository: CloudBackupRepository,
    private val safeRepository: SafeRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun current(): Flow<CloudInfo> = safeRepository.currentSafeIdFlow().flatMapLatest { safeId ->
        safeId?.let { cloudBackupRepository.getCloudInfoFlow(safeId) } ?: flowOf(CloudInfo(null, null))
    }

    suspend fun getFirstFolderAvailable(): URI? = cloudBackupRepository.getFirstCloudFolderAvailable()
}
