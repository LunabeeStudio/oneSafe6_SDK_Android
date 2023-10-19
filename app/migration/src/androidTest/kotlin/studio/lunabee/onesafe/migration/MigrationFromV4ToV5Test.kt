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
 * Created by Lunabee Studio / Date - 10/13/2023 - for the oneSafe6 SDK.
 * Last modified 10/13/23, 12:10 PM
 */

package studio.lunabee.onesafe.migration

import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import studio.lunabee.onesafe.importexport.model.LocalBackup
import studio.lunabee.onesafe.importexport.usecase.AutoBackupUseCase
import studio.lunabee.onesafe.importexport.usecase.GetLocalBackupsUseCase
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltTest
import studio.lunabee.onesafe.test.testClock
import java.io.File
import java.time.Instant
import javax.inject.Inject
import kotlin.test.assertContentEquals

@HiltAndroidTest
class MigrationFromV4ToV5Test : OSHiltTest() {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val backupsDir: File = File(context.filesDir, "backups").also { it.mkdirs() }

    @get:Rule override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)
    override val initialTestState: InitialTestState = InitialTestState.LoggedIn

    @Inject lateinit var migrationFromV4ToV5: MigrationFromV4ToV5

    @Inject lateinit var getLocalBackupsUseCase: GetLocalBackupsUseCase

    @Inject lateinit var autoBackupUseCase: AutoBackupUseCase

    @Before
    fun setUp() {
        backupsDir.listFiles()?.forEach { it.delete() }
    }

    @Test
    fun migrate_backups_test(): TestResult = runTest {
        val expected = listOf(
            LocalBackup(Instant.EPOCH.plusSeconds(2), File(backupsDir, "oneSafe-19700101-000002.os6lsb")),
            LocalBackup(Instant.EPOCH.plusSeconds(1), File(backupsDir, "oneSafe-19700101-000001.os6lsb")),
            LocalBackup(Instant.EPOCH, File(backupsDir, "oneSafe-19700101-000000.os6lsb")),
        ).onEach { it.file.createNewFile() }

        assertContentEquals(emptyList(), getLocalBackupsUseCase())
        migrationFromV4ToV5()
        val actual = getLocalBackupsUseCase()
        assertContentEquals(expected, actual)
    }

    @Test
    fun migrate_backups_above_limit_test(): TestResult = runTest {
        testClock.setInstant(Instant.EPOCH.plusSeconds(3))
        autoBackupUseCase().last()
        testClock.setInstant(Instant.EPOCH.plusSeconds(6))
        autoBackupUseCase().last()

        val oldBackups = listOf(
            LocalBackup(Instant.EPOCH.plusSeconds(5), File(backupsDir, "oneSafe-19700101-000005.os6lsb")),
            LocalBackup(Instant.EPOCH.plusSeconds(4), File(backupsDir, "oneSafe-19700101-000004.os6lsb")),
            LocalBackup(Instant.EPOCH.plusSeconds(2), File(backupsDir, "oneSafe-19700101-000002.os6lsb")),
            LocalBackup(Instant.EPOCH.plusSeconds(1), File(backupsDir, "oneSafe-19700101-000001.os6lsb")),
            LocalBackup(Instant.EPOCH, File(backupsDir, "oneSafe-19700101-000000.os6lsb")),
        ).onEach { it.file.createNewFile() }

        val expected = (getLocalBackupsUseCase() + oldBackups).sortedDescending().take(5)
        migrationFromV4ToV5()
        val actual = getLocalBackupsUseCase()
        assertContentEquals(expected, actual)
    }
}
