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
 * Created by Lunabee Studio / Date - 2/14/2024 - for the oneSafe6 SDK.
 * Last modified 2/14/24, 3:17 PM
 */

package studio.lunabee.onesafe.domain.usecase

import com.lunabee.lbcore.model.LBFlowResult
import com.lunabee.lbcore.model.LBResult
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import studio.lunabee.onesafe.domain.model.common.UrlMetadata
import studio.lunabee.onesafe.domain.repository.UrlMetadataRepository
import studio.lunabee.onesafe.domain.usecase.item.DownloadItemIconFromUrlUseCase
import studio.lunabee.onesafe.test.assertSuccess
import java.io.File
import kotlin.test.assertEquals

class DownloadItemIconFromUrlUseCaseTest {
    private val urlOk = "url_ok"
    private val urlKo = "url_ko"
    private val tmpFile = File("tmp")
    private val fallbackUrlMetadata = UrlMetadata(urlKo, null, tmpFile, true)

    private val urlMetadataRepository = mockk<UrlMetadataRepository> {
        every { this@mockk.downloadImage(urlOk, tmpFile) } returns flowOf(LBFlowResult.Success(tmpFile))
        every { this@mockk.downloadImage(urlKo, tmpFile) } returns flowOf(LBFlowResult.Failure(null, tmpFile))
    }

    private val getUrlMetadataUseCase = mockk<GetUrlMetadataUseCase> {
        coEvery {
            this@mockk.invoke(urlKo, tmpFile, true, studio.lunabee.onesafe.domain.usecase.GetUrlMetadataUseCase.RequestedData.Image)
        } returns LBResult.Success(fallbackUrlMetadata)
    }

    private val useCase = DownloadItemIconFromUrlUseCase(
        urlMetadataRepository,
        getUrlMetadataUseCase,
    )

    @AfterEach
    fun tearsDown() {
        tmpFile.delete()
    }

    @Test
    fun download_image_success_test(): TestResult = runTest {
        val actual = useCase(urlOk, tmpFile).last()
        assertSuccess(actual)
        assertEquals(tmpFile, actual.successData.iconFile)
    }

    @Test
    fun download_image_fallback_test(): TestResult = runTest {
        val actual = useCase(urlKo, tmpFile).last()
        assertSuccess(actual)
        assertEquals(fallbackUrlMetadata, actual.successData)
    }
}
