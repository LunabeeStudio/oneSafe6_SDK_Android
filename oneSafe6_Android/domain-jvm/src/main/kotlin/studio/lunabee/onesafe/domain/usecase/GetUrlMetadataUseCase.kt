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
import studio.lunabee.onesafe.error.OSDomainError
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.jvm.get
import java.io.File
import javax.inject.Inject

class GetUrlMetadataUseCase @Inject constructor(
    private val urlMetadataRepository: UrlMetadataRepository,
    @ValidUrlStartList
    private val validUrlStartList: List<String>,
) {
    suspend operator fun invoke(
        url: String,
        iconFile: File,
        force: Boolean,
        requestedData: RequestedData = RequestedData.All,
    ): LBResult<UrlMetadata> {
        val lowerUrl = url.lowercase()
        val (htmlCode: String, finalUrl: String) = validUrlStartList.firstNotNullOfOrNull { prefix ->
            val candidateUrl = prefix + lowerUrl
            val htmlPageCode = getHtmlPageCode(url = candidateUrl)
            (htmlPageCode as? LBResult.Success)?.let { it.successData to candidateUrl }
        } ?: return LBResult.Failure(OSDomainError.Code.NO_HTML_PAGE_FOUND.get())

        return when (requestedData) {
            RequestedData.Title -> {
                LBResult.Success(
                    successData = UrlMetadata(
                        url = url, // keep url enters by user
                        title = extractTitle(htmlCode = htmlCode),
                        iconFile = null,
                        force = force,
                    ),
                )
            }
            RequestedData.Image -> {
                val fileResult = urlMetadataRepository.downloadFavIcon(baseUrl = finalUrl, targetFile = iconFile)
                val urlMetadata = UrlMetadata(
                    url = url, // keep url enters by user
                    title = null,
                    iconFile = fileResult.data,
                    force = force,
                )
                when (fileResult) {
                    is LBResult.Failure -> LBResult.Failure(
                        throwable = fileResult.throwable,
                        failureData = urlMetadata,
                    )
                    is LBResult.Success -> LBResult.Success(
                        successData = urlMetadata,
                    )
                }
            }
            RequestedData.All -> {
                val fileResult = urlMetadataRepository.downloadFavIcon(baseUrl = finalUrl, targetFile = iconFile)
                val title = extractTitle(htmlCode = htmlCode)
                val metadata = UrlMetadata(
                    url = url, // keep url enters by user
                    title = title,
                    iconFile = fileResult.data,
                    force = force,
                )
                if (title != null || fileResult is LBResult.Success) {
                    LBResult.Success(successData = metadata)
                } else {
                    LBResult.Failure(failureData = metadata)
                }
            }
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
