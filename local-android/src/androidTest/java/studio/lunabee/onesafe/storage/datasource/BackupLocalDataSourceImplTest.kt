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
 * Created by Lunabee Studio / Date - 10/4/2023 - for the oneSafe6 SDK.
 * Last modified 10/4/23, 4:30 PM
 */

package studio.lunabee.onesafe.storage.datasource

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.assertDoesNotThrow
import studio.lunabee.onesafe.domain.qualifier.InternalDir
import java.io.File
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@OptIn(FlowPreview::class)
@HiltAndroidTest
class BackupLocalDataSourceImplTest {
    @get:Rule val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @Inject internal lateinit var backupLocalDataSource: BackupLocalDataSourceImpl

    @Inject
    @InternalDir(InternalDir.Type.Backups)
    lateinit var backupDir: File

    @Before
    fun setUp() {
        hiltRule.inject()
        backupDir.deleteRecursively()
    }

    @After
    fun tearsDown() {
        backupDir.deleteRecursively()
    }

    /**
     * Create file and check flow update
     */
    @Test
    fun getBackupsFlow_create_test(): TestResult = runTest {
        val abcFile = File(backupLocalDataSource.backupsDir, "abc")
        val actual = backupLocalDataSource.getBackupsFlow().stateIn(backgroundScope, SharingStarted.Eagerly, null)
        assertDoesNotThrow({
            "actual value = ${actual.value?.joinToString()}"
        }) {
            actual.filter { it?.isEmpty() == true }
                .timeout(2.seconds)
                .flowOn(Dispatchers.Default)
                .first()
        }
        abcFile.createNewFile()
        assertDoesNotThrow({
            "actual value = ${actual.value?.joinToString()}"
        }) {
            actual.filter { it?.size == 1 && it.first() == abcFile }
                .timeout(2.seconds)
                .flowOn(Dispatchers.Default)
                .first()
        }
    }

    /**
     * Delete file and check flow update
     */
    @Test
    fun getBackupsFlow_delete_test(): TestResult = runTest {
        val abcFile = File(backupLocalDataSource.backupsDir, "abc").also { it.createNewFile() }
        val actual = backupLocalDataSource.getBackupsFlow().stateIn(backgroundScope, SharingStarted.Eagerly, null)
        assertDoesNotThrow({
            "actual value = ${actual.value?.joinToString()}"
        }) {
            actual.filter { it?.size == 1 && it.first() == abcFile }
                .timeout(2.seconds)
                .flowOn(Dispatchers.Default)
                .first()
        }
        abcFile.delete()
        assertDoesNotThrow({
            "actual value = ${actual.value?.joinToString()}"
        }) {
            actual.filter { it?.isEmpty() == true }
                .timeout(2.seconds)
                .flowOn(Dispatchers.Default)
                .first()
        }
    }
}
