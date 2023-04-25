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
 * Created by Lunabee Studio / Date - 4/7/2023 - for the oneSafe6 SDK.
 * Last modified 4/7/23, 12:24 AM
 */

package studio.lunabee.onesafe.domain.usecase

import com.lunabee.lbcore.model.LBResult
import studio.lunabee.onesafe.domain.model.common.UrlMetadata
import studio.lunabee.onesafe.domain.qualifier.ValidUrlStartList
import studio.lunabee.onesafe.domain.repository.UrlMetadataRepository
import studio.lunabee.onesafe.error.OSError
import javax.inject.Inject

class GetUrlMetadataUseCase @Inject constructor(
    private val urlMetadataRepository: UrlMetadataRepository,
    @ValidUrlStartList
    private val validUrlStartList: List<String>,
) {
    suspend operator fun invoke(url: String, iconPath: String, requestedData: RequestedData = RequestedData.All): LBResult<UrlMetadata> {
        var result = getHtmlPageCode(url = url.lowercase())

        var finalUrl = url
        var schemeIndexTested = 0
        while (result is LBResult.Failure && validUrlStartList.getOrNull(schemeIndexTested) != null) {
            finalUrl = validUrlStartList[schemeIndexTested] + url
            result = getHtmlPageCode(url = finalUrl)
            schemeIndexTested++
        }

        return when {
            requestedData == RequestedData.Title && result is LBResult.Success -> {
                LBResult.Success(
                    successData = UrlMetadata(
                        url = url, // keep url enters by user
                        title = extractTitle(htmlCode = result.successData),
                        filePath = null,
                    ),
                )
            }
            requestedData == RequestedData.Image && result is LBResult.Success -> {
                val isSuccessful = urlMetadataRepository.downloadIcon(baseUrl = finalUrl, filePath = iconPath)
                LBResult.Success(
                    successData = UrlMetadata(
                        url = url, // keep url enters by user
                        title = null,
                        filePath = iconPath.takeIf { isSuccessful },
                    ),
                )
            }
            requestedData == RequestedData.All && result is LBResult.Success -> {
                val isSuccessful = urlMetadataRepository.downloadIcon(baseUrl = finalUrl, filePath = iconPath)
                LBResult.Success(
                    successData = UrlMetadata(
                        url = url, // keep url enters by user
                        title = extractTitle(htmlCode = result.successData),
                        filePath = iconPath.takeIf { isSuccessful },
                    ),
                )
            }
            else -> LBResult.Failure(throwable = (result as? LBResult.Failure)?.throwable)
        }
    }

    private suspend fun getHtmlPageCode(url: String): LBResult<String> {
        return OSError.runCatching {
            urlMetadataRepository.getHtmlPageCode(url = url)
        }
    }

    private fun extractTitle(htmlCode: String): String? {
        // Try to extract <meta property="og:site_name"> tag.
        val title: String? = MetaTitlePropertyPattern
            .toRegex()
            .find(input = htmlCode)
            ?.value
            ?.substringAfter(delimiter = ContentAttribute)
            ?.substringBefore(delimiter = Quote)

        // Fallback on page title.
        return title ?: TitleTagPattern
            .toRegex()
            .find(input = htmlCode)
            ?.value
            ?.substringAfter(delimiter = EndTag)
            ?.substringBefore(delimiter = StartTag)
    }

    enum class RequestedData {
        All,
        Title,
        Image,
        ;
    }

    companion object {
        private const val AnyStringPattern: String = "(.*?)"

        private const val Quote: String = "\""
        private const val ContentAttribute: String = "content=$Quote"
        private const val StartTag: String = "<"
        private const val EndTag: String = ">"
        private const val OgSiteName: String = "og:site_name"

        const val MetaTitlePropertyPattern: String =
            "<meta property=${Quote}$OgSiteName$Quote $ContentAttribute$AnyStringPattern$Quote\\s?/?>"
        const val TitleTagPattern: String = "<title>$AnyStringPattern</?title>"
    }
}
