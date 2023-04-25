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

package studio.lunabee.onesafe.remote.datasource

import com.lunabee.lblogger.LBLogger
import com.lunabee.lblogger.e
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpStatusCode
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import studio.lunabee.onesafe.error.OSRemoteError
import studio.lunabee.onesafe.remote.api.UrlMetadataApi
import studio.lunabee.onesafe.repository.datasource.UrlMetadataRemoteDataSource
import java.io.File
import javax.inject.Inject

private val log = LBLogger.get<UrlMetadataRemoteDataSourceImpl>()

class UrlMetadataRemoteDataSourceImpl @Inject constructor(
    private val urlMetadataApi: UrlMetadataApi,
) : UrlMetadataRemoteDataSource {

    override suspend fun getPageHtmlCode(url: String): String {
        return try {
            urlMetadataApi.getHtmlPageCode(url = url)
        } catch (e: Exception) {
            throw OSRemoteError(code = OSRemoteError.Code.UNKNOWN_HTTP_ERROR, cause = e)
        }
    }

    override suspend fun downloadIcon(baseUrl: String, filePath: String): Boolean {
        return try {
            val httpResponse = urlMetadataApi.downloadIcon(url = DefaultFaviconUrlApi + baseUrl)
            handleResponse(httpResponse = httpResponse, filePath = filePath) || kotlin.run {
                val fallbackHttpResponse = urlMetadataApi.downloadIcon(url = FallbackFaviconUrlApi + baseUrl)
                handleResponse(httpResponse = fallbackHttpResponse, filePath = filePath)
            }
        } catch (e: Exception) {
            // Silent exception.
            log.e(t = e)
            false
        }
    }

    private suspend fun handleResponse(httpResponse: HttpResponse, filePath: String): Boolean {
        return if (httpResponse.status == HttpStatusCode.OK) {
            val file = File(filePath)
            httpResponse.bodyAsChannel().copyAndClose(file.writeChannel())
            true
        } else {
            false
        }
    }

    companion object {
        private const val DefaultFaviconUrlApi: String =
            "https://t2.gstatic.com/faviconV2?client=SOCIAL&type=FAVICON&fallback_opts=TYPE,SIZE,URL&size=256&url="
        private const val FallbackFaviconUrlApi: String = "https://logo.clearbit.com/"
    }
}
