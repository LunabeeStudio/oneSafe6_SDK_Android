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
 * Created by Lunabee Studio / Date - 11/20/2023 - for the oneSafe6 SDK.
 * Last modified 11/20/23, 9:25 AM
 */

package studio.lunabee.onesafe.importexport.worker

import android.Manifest
import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.TestDriver
import androidx.work.testing.WorkManagerTestInitHelper
import com.lunabee.lbcore.model.LBFlowResult
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.BeforeClass
import org.junit.Rule
import studio.lunabee.onesafe.commonui.notification.OSNotificationManager
import studio.lunabee.onesafe.error.OSDriveError
import studio.lunabee.onesafe.importexport.ImportExportAndroidConstants
import studio.lunabee.onesafe.importexport.model.CloudBackup
import studio.lunabee.onesafe.importexport.repository.AutoBackupErrorRepository
import studio.lunabee.onesafe.importexport.usecase.CloudAutoBackupUseCase
import studio.lunabee.onesafe.importexport.usecase.DeleteOldCloudBackupsUseCase
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltTest
import studio.lunabee.onesafe.test.firstSafeId
import java.time.Instant
import java.time.ZonedDateTime
import java.util.UUID
import javax.inject.Inject
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@HiltAndroidTest
class CloudBackupWorkerTest : OSHiltTest() {
    @get:Rule
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)
    override val initialTestState: InitialTestState = InitialTestState.Home()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    companion object {
        @JvmStatic
        @BeforeClass
        fun notificationPermission() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val context = InstrumentationRegistry.getInstrumentation().targetContext
                InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(
                    context.packageName,
                    Manifest.permission.POST_NOTIFICATIONS,
                )
            }
        }
    }

    private val successCloudBackup: CloudBackup = CloudBackup(
        remoteId = "remoteId",
        name = "name",
        date = Instant.EPOCH,
        safeId = firstSafeId,
    )

    @BindValue
    val cloudAutoBackupUseCase: CloudAutoBackupUseCase = mockk {
        every { this@mockk.invoke(firstSafeId) } returns flowOf(LBFlowResult.Success(successCloudBackup))
    }

    @BindValue
    val deleteOldCloudBackupsUseCase: DeleteOldCloudBackupsUseCase = mockk {
        every { this@mockk.invoke(firstSafeId) } returns flowOf(LBFlowResult.Success(Unit))
        every { this@mockk.invoke(any()) } returns flowOf(LBFlowResult.Success(Unit))
    }

    @Inject lateinit var autoBackupErrorRepository: AutoBackupErrorRepository

    @Inject lateinit var osNotificationManager: OSNotificationManager

    @Inject lateinit var autoBackupWorkersHelper: AutoBackupWorkersHelper

    @After
    fun tearsDown(): TestResult = runTest {
        osNotificationManager.manager.cancel(OSNotificationManager.AUTO_BACKUP_ERROR_WORKER_NOTIFICATION_ID)
        autoBackupWorkersHelper.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun success_test(): TestResult = runTest {
        val workManager = WorkManager.getInstance(context)
        val testDriver = WorkManagerTestInitHelper.getTestDriver(context)!!

        CloudBackupWorker.start(context, false, firstSafeId)
        val workId = getWorkId(workManager)
        val actual = mutableListOf<WorkInfo.State>()
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            workManager.getWorkInfoByIdFlow(workId).map { it.state }.toList(actual)
        }

        testDriver.setAllConstraintsMet(workId)

        workManager.getWorkInfoByIdFlow(workId).first { it.state.isFinished }

        val expected = listOf(
            WorkInfo.State.ENQUEUED,
            WorkInfo.State.RUNNING,
            WorkInfo.State.SUCCEEDED,
        )
        assertContentEquals(expected, actual)

        collectJob.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun retry_test(): TestResult = runTest {
        every { cloudAutoBackupUseCase.invoke(firstSafeId) } returns
            flowOf(LBFlowResult.Failure(OSDriveError(OSDriveError.Code.DRIVE_REQUEST_EXECUTION_FAILED)))

        val workManager = WorkManager.getInstance(context)
        val testDriver = WorkManagerTestInitHelper.getTestDriver(context)!!

        val expected = listOf(
            WorkInfo.State.ENQUEUED,
            WorkInfo.State.RUNNING,
            WorkInfo.State.ENQUEUED, // retry
        )

        CloudBackupWorker.start(context, false, firstSafeId)
        val workId = getWorkId(workManager)
        launch(UnconfinedTestDispatcher(testScheduler)) {
            val actual = workManager.getWorkInfoByIdFlow(workId).take(3).map { it.state }.toList()
            assertContentEquals(expected, actual)
        }

        testDriver.setAllConstraintsMet(workId)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun unrecoverable_failure_test(): TestResult = runTest {
        every { cloudAutoBackupUseCase.invoke(firstSafeId) } returns
            flowOf(LBFlowResult.Failure(OSDriveError(OSDriveError.Code.DRIVE_AUTHENTICATION_REQUIRED)))

        val workManager = WorkManager.getInstance(context)
        val testDriver = WorkManagerTestInitHelper.getTestDriver(context)!!

        CloudBackupWorker.start(context, false, firstSafeId)
        val workId = getWorkId(workManager)
        val actual = mutableListOf<WorkInfo.State>()
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            workManager.getWorkInfoByIdFlow(workId).map { it.state }.toList(actual)
        }

        testDriver.setAllConstraintsMet(workId)

        workManager.getWorkInfoByIdFlow(workId).first { it.state.isFinished }

        val expected = listOf(
            WorkInfo.State.ENQUEUED,
            WorkInfo.State.RUNNING,
            WorkInfo.State.FAILED,
        )

        assertContentEquals(expected, actual)
        collectJob.cancel()

        val actualError = autoBackupErrorRepository.getError(firstSafeId).first()
        assertNotNull(actualError)
        assertEquals(OSDriveError.Code.DRIVE_AUTHENTICATION_REQUIRED.name, actualError.code)
        assertEquals(ZonedDateTime.now(testClock), actualError.date)
    }

    @Test
    fun error_notification_test(): TestResult = runTest {
        every { cloudAutoBackupUseCase.invoke(firstSafeId) } returns
            flowOf(LBFlowResult.Failure(OSDriveError(OSDriveError.Code.DRIVE_AUTHENTICATION_REQUIRED)))

        val workManager = WorkManager.getInstance(context)
        val testDriver = WorkManagerTestInitHelper.getTestDriver(context)!!

        runWorker(workManager, testDriver)

        val notificationOnFailure = osNotificationManager.manager.activeNotifications.firstOrNull {
            it.id == OSNotificationManager.AUTO_BACKUP_ERROR_WORKER_NOTIFICATION_ID
        }
        assertNotNull(notificationOnFailure)

        every { cloudAutoBackupUseCase.invoke(firstSafeId) } returns flowOf(LBFlowResult.Success(successCloudBackup))
        runWorker(workManager, testDriver)

        val notificationAfterSuccess = osNotificationManager.manager.activeNotifications.firstOrNull {
            it.id == OSNotificationManager.AUTO_BACKUP_ERROR_WORKER_NOTIFICATION_ID
        }
        assertNull(notificationAfterSuccess)
    }

    private suspend fun CloudBackupWorkerTest.runWorker(
        workManager: WorkManager,
        testDriver: TestDriver,
    ) {
        CloudBackupWorker.start(context, false, firstSafeId)
        val workId = getWorkId(workManager)
        testDriver.setAllConstraintsMet(workId)
        workManager.getWorkInfoByIdFlow(workId).first { it.state.isFinished }
    }

    private suspend fun getWorkId(workManager: WorkManager): UUID {
        val workInfo: WorkInfo = workManager.getWorkInfosForUniqueWorkFlow(ImportExportAndroidConstants.autoBackupWorkerName(firstSafeId))
            .first()
            .first()
        return workInfo.id
    }
}
