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

import com.lunabee.lbcore.model.LBFlowResult
import com.lunabee.lbcore.model.LBFlowResult.Companion.transformResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withTimeoutOrNull
import studio.lunabee.onesafe.domain.qualifier.RemoteDispatcher
import studio.lunabee.onesafe.error.OSRemoteError
import studio.lunabee.onesafe.remote.api.UrlMetadataApi
import studio.lunabee.onesafe.repository.datasource.UrlMetadataRemoteDataSource
import java.io.File
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class UrlMetadataRemoteDataSourceImpl @Inject constructor(
    private val urlMetadataApi: UrlMetadataApi,
    @param:RemoteDispatcher
    private val dispatcher: CoroutineDispatcher,
) : UrlMetadataRemoteDataSource {
    private val htmlPageFetchTimeout = 6.seconds

    override suspend fun getPageHtmlCode(url: String): String {
        return try {
            withTimeoutOrNull(htmlPageFetchTimeout) {
                urlMetadataApi.getHtmlPageCode(url = url)
            }
        } catch (e: Exception) {
            throw OSRemoteError(code = OSRemoteError.Code.UNKNOWN_HTTP_ERROR, cause = e)
        } ?: throw OSRemoteError(code = OSRemoteError.Code.UNEXPECTED_TIMEOUT)
    }

    override fun downloadFavIcon(baseUrl: String, targetFile: File): Flow<LBFlowResult<File>> =
        urlMetadataApi.downloadImage(url = DefaultFaviconUrlApi + baseUrl, targetFile).transformResult(
            transformError = {
                val fallbackFlow = urlMetadataApi.downloadImage(url = FallbackFaviconUrlApi + baseUrl, targetFile)
                emitAll(fallbackFlow)
            },
        )
            .flowOn(dispatcher)
            .catch { throwable ->
                emit(LBFlowResult.Failure(throwable, targetFile))
            }

    override fun downloadImage(url: String, targetFile: File): Flow<LBFlowResult<File>> =
        urlMetadataApi.downloadImage(url, targetFile)
            .flowOn(dispatcher)
            .catch { throwable ->
                emit(LBFlowResult.Failure(throwable, targetFile))
            }

    companion object {
        private const val DefaultFaviconUrlApi: String =
            "https://t2.gstatic.com/faviconV2?client=SOCIAL&type=FAVICON&fallback_opts=TYPE,SIZE,URL&size=256&url="
        private const val FallbackFaviconUrlApi: String = "https://logo.clearbit.com/"
    }
}
