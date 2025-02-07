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
 * Created by Lunabee Studio / Date - 2/7/2024 - for the oneSafe6 SDK.
 * Last modified 2/7/24, 3:59 PM
 */

package studio.lunabee.onesafe.domain.usecase.item

import com.lunabee.lbcore.model.LBFlowResult
import com.lunabee.lbcore.model.LBFlowResult.Companion.transformResult
import kotlinx.coroutines.flow.Flow
import studio.lunabee.onesafe.domain.model.common.UrlMetadata
import studio.lunabee.onesafe.domain.repository.UrlMetadataRepository
import studio.lunabee.onesafe.domain.usecase.GetUrlMetadataUseCase
import java.io.File
import javax.inject.Inject

/**
 * Download the image from the provided url to the provided file. Fallback to [GetUrlMetadataUseCase] flow in case of failure.
 */
class DownloadItemIconFromUrlUseCase @Inject constructor(
    private val urlMetadataRepository: UrlMetadataRepository,
    private val getUrlMetadataUseCase: GetUrlMetadataUseCase,
) {
    operator fun invoke(url: String, iconFile: File): Flow<LBFlowResult<UrlMetadata>> {
        return urlMetadataRepository.downloadImage(url, iconFile).transformResult(
            transform = {
                val urlMetadata = UrlMetadata(url = url, iconFile = it.successData, title = null, force = true)
                emit(LBFlowResult.Success(urlMetadata))
            },
            transformError = {
                val metadataResult = getUrlMetadataUseCase(
                    url = url,
                    iconFile = iconFile,
                    force = true,
                    requestedData = GetUrlMetadataUseCase.RequestedData.Image,
                )
                emit(metadataResult.asFlowResult())
            },
        )
    }
}
