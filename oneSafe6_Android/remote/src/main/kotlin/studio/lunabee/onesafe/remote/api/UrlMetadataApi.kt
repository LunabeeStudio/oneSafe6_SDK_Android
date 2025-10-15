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

package studio.lunabee.onesafe.remote.api

import co.touchlab.kermit.Logger
import com.lunabee.lbcore.model.LBFlowResult
import com.lunabee.lblogger.LBLogger
import io.ktor.client.HttpClient
import io.ktor.client.plugins.onDownload
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import studio.lunabee.onesafe.domain.Constant
import java.io.File
import javax.inject.Inject

private val logger: Logger = LBLogger.get<UrlMetadataApi>()

class UrlMetadataApi @Inject constructor(
    private val httpClient: HttpClient,
) {
    private val getHtmlPageTimeout = 5_000L

    suspend fun getHtmlPageCode(url: String): String = httpClient
        .get(urlString = url) {
            timeout {
                requestTimeoutMillis = getHtmlPageTimeout
                connectTimeoutMillis = getHtmlPageTimeout
                socketTimeoutMillis = getHtmlPageTimeout
            }
        }.bodyAsText()

    fun downloadImage(url: String, targetFile: File): Flow<LBFlowResult<File>> = callbackFlow {
        val byteReadChannel = httpClient
            .get(urlString = url) {
                onDownload { bytesSentTotal, contentLength ->
                    val progress = if (contentLength == null || contentLength == 0L) {
                        Constant.IndeterminateProgress
                    } else {
                        bytesSentTotal.toFloat() / contentLength
                    }
                    logger.v { "downloading ${(progress * 100).toInt()}%" }
                    send(LBFlowResult.Loading(targetFile, progress))
                }
            }.bodyAsChannel()
        byteReadChannel.copyAndClose(targetFile.writeChannel())
        send(LBFlowResult.Success(targetFile))
        close()
    }
}
