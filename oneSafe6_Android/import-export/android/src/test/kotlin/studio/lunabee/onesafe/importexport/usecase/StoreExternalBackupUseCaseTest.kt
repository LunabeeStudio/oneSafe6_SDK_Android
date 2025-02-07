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
 * Created by Lunabee Studio / Date - 4/16/2024 - for the oneSafe6 SDK.
 * Last modified 4/16/24, 9:16 AM
 */

package studio.lunabee.onesafe.importexport.usecase

import androidx.core.net.toUri
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.every
import io.mockk.mockkObject
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import kotlin.test.Test
import studio.lunabee.compose.androidtest.helper.LbcResourcesHelper
import studio.lunabee.onesafe.commonui.utils.FileDetails
import studio.lunabee.onesafe.error.OSImportExportError
import studio.lunabee.onesafe.error.osCode
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltUnitTest
import studio.lunabee.onesafe.test.TestConstants
import studio.lunabee.onesafe.test.assertFailure
import studio.lunabee.onesafe.test.assertSuccess
import java.io.File
import javax.inject.Inject
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@HiltAndroidTest
class StoreExternalBackupUseCaseTest : OSHiltUnitTest() {

    @get:Rule
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.Home()

    @Inject
    lateinit var useCase: StoreExternalBackupUseCase

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val oneSafeArchive = File(context.cacheDir, TestConstants.ResourcesFile.ArchiveValid)

    @Before
    fun setUp() {
        LbcResourcesHelper.copyResourceToDeviceFile(TestConstants.ResourcesFile.ArchiveValid, oneSafeArchive)
    }

    @After
    fun tearsDown() {
        oneSafeArchive.delete()
    }

    @Test
    fun store_test(): TestResult = runTest {
        val result = useCase.invoke(oneSafeArchive.toUri()).last()
        assertSuccess(result)
        val backup = result.successData
        assertTrue(backup.file.exists())
    }

    @Test
    fun not_a_backup_test(): TestResult = runTest {
        val result = useCase.invoke(File(context.cacheDir, "bad.zip").toUri()).last()
        val error = assertFailure(result)
        assertEquals(OSImportExportError.Code.FILE_NOT_A_BACKUP, error.throwable.osCode())
    }

    @Test
    fun bad_uri_test(): TestResult = runTest {
        mockkObject(FileDetails.Companion)
        every { FileDetails.fromUri(any(), any()) } returns FileDetails("mock", 123, "os6lsb")
        val backupUri = File(context.cacheDir, "bad.os6lsb").toUri()
        val result = useCase.invoke(backupUri).last()
        val error = assertFailure(result)
        assertEquals(OSImportExportError.Code.CANNOT_OPEN_URI, error.throwable.osCode())
    }
}
