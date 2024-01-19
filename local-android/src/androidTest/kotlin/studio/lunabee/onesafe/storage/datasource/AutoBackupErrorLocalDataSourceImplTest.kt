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
 * Created by Lunabee Studio / Date - 11/21/2023 - for the oneSafe6 SDK.
 * Last modified 11/21/23, 3:20 PM
 */

package studio.lunabee.onesafe.storage.datasource

import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import studio.lunabee.onesafe.importexport.model.AutoBackupError
import java.io.File
import java.time.Clock
import java.time.ZonedDateTime
import javax.inject.Inject
import kotlin.test.assertContentEquals

@HiltAndroidTest
class AutoBackupErrorLocalDataSourceImplTest {
    @get:Rule val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @Inject internal lateinit var autoBackupErrorDataStore: AutoBackupErrorLocalDataSourceImpl

    @Inject lateinit var clock: Clock

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun setUp() {
        hiltRule.inject()
        File(context.filesDir, "datastore").deleteRecursively()
    }

    @Test
    fun read_write_test(): TestResult = runTest {
        val actual = mutableListOf<AutoBackupError?>()
        val job = launch(testScheduler) {
            autoBackupErrorDataStore.getError().toList(actual)
        }

        val expected = listOf(
            null,
            AutoBackupError(ZonedDateTime.now(clock), "error_code_0", null),
            AutoBackupError(ZonedDateTime.now(clock).plusDays(1), "error_code_1", "error_message_1"),
            null,
        )

        expected.forEach { autoBackupErrorDataStore.setError(it) }

        autoBackupErrorDataStore.getError().first() // sync wait collection
        assertContentEquals(expected, actual)
        job.cancel()
    }
}
