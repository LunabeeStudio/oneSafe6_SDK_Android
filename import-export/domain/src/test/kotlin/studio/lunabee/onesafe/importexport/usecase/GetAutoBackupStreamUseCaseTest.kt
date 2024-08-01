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
 * Created by Lunabee Studio / Date - 11/15/2023 - for the oneSafe6 SDK.
 * Last modified 11/15/23, 10:33 AM
 */

package studio.lunabee.onesafe.importexport.usecase

import com.lunabee.lbcore.model.LBFlowResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.AfterClass
import org.junit.BeforeClass
import studio.lunabee.onesafe.importexport.repository.CloudBackupRepository
import studio.lunabee.onesafe.importexport.repository.LocalBackupRepository
import studio.lunabee.onesafe.test.DummySafeRepository
import studio.lunabee.onesafe.test.firstSafeId
import java.io.File
import java.io.InputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class GetAutoBackupStreamUseCaseTest {

    private val localBackupRepository: LocalBackupRepository = mockk {
        coEvery { getFile("sync") } returns localSyncFile
        coEvery { getFile("local") } returns localFile
        coEvery { getFile("not_exist") } returns null
        coEvery { getFile("cloud") } returns null
    }
    private val cloudBackupRepository: CloudBackupRepository = mockk {
        coEvery { getInputStream("cloud", firstSafeId) } returns flowOf(LBFlowResult.Success(cloudFile.inputStream()))
        coEvery { getInputStream("not_exist", firstSafeId) } returns flowOf(LBFlowResult.Failure())
        coEvery { getInputStream("local", firstSafeId) } returns flowOf(LBFlowResult.Failure())
    }

    private val useCase = GetAutoBackupStreamUseCase(
        localBackupRepository,
        cloudBackupRepository,
        DummySafeRepository(),
    )

    companion object {

        val localFile: File = File("local")
        val localSyncFile: File = File("sync_local")
        val cloudFile: File = File("cloud")

        @JvmStatic
        @BeforeClass
        fun setUp() {
            localFile.writeText(localFile.name)
            localSyncFile.writeText(localSyncFile.name)
            cloudFile.writeText(cloudFile.name)
        }

        @JvmStatic
        @AfterClass
        fun tearsDown() {
            localFile.delete()
            localSyncFile.delete()
            cloudFile.delete()
        }
    }

    @Test
    fun no_backup_found_test(): TestResult = runTest {
        val actual = useCase("not_exist").last()
        assertIs<LBFlowResult.Failure<InputStream>>(actual)
        assertNull(actual.data)
    }

    @Test
    fun local_only_backup_found_test(): TestResult = runTest {
        val actual = useCase("local").last()
        assertIs<LBFlowResult.Success<InputStream>>(actual)
        val actualContent = actual.successData.reader().readText()
        assertEquals(localFile.name, actualContent)
    }

    @Test
    fun cloud_only_backup_found_test(): TestResult = runTest {
        val actual = useCase("cloud").last()
        assertIs<LBFlowResult.Success<InputStream>>(actual)
        val actualContent = actual.successData.reader().readText()
        assertEquals(cloudFile.name, actualContent)
    }

    @Test
    fun synchronized_backup_found_test(): TestResult = runTest {
        val actual = useCase("sync").last()
        assertIs<LBFlowResult.Success<InputStream>>(actual)
        val actualContent = actual.successData.reader().readText()
        assertEquals(localSyncFile.name, actualContent)
    }
}
