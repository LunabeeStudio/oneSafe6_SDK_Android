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
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import studio.lunabee.onesafe.importexport.model.AutoBackupError
import studio.lunabee.onesafe.importexport.model.AutoBackupMode
import studio.lunabee.onesafe.storage.dao.SafeDao
import studio.lunabee.onesafe.test.CommonTestUtils
import studio.lunabee.onesafe.test.firstSafeId
import studio.lunabee.onesafe.test.testUUIDs
import java.io.File
import java.time.Clock
import java.time.ZonedDateTime
import javax.inject.Inject
import kotlin.test.Test
import kotlin.test.assertContentEquals

@RunWith(RobolectricTestRunner::class)
@Config(
    application = HiltTestApplication::class,
)
@HiltAndroidTest
class AutoBackupErrorLocalDataSourceImplTest {
    @get:Rule val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @Inject internal lateinit var autoBackupErrorDataStore: AutoBackupErrorLocalDataSourceImpl

    @Inject lateinit var clock: Clock

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Inject internal lateinit var safeDao: SafeDao

    @Before
    fun setUp() {
        hiltRule.inject()
        File(context.filesDir, "datastore").deleteRecursively()
        runTest {
            safeDao.insert(CommonTestUtils.roomSafe())
        }
    }

    @Test
    fun read_write_test(): TestResult = runTest {
        val actual = mutableListOf<AutoBackupError?>()
        val job = launch(testScheduler) {
            autoBackupErrorDataStore.getLastError(firstSafeId).toList(actual)
        }

        val expected = listOf(
            AutoBackupError(
                id = testUUIDs[0],
                date = ZonedDateTime.now(clock),
                code = "error_code_0",
                message = null,
                source = AutoBackupMode.Synchronized,
                safeId = firstSafeId,
            ),
            AutoBackupError(
                id = testUUIDs[1],
                date = ZonedDateTime.now(clock).plusDays(1),
                code = "error_code_1",
                message = "error_message_1",
                source = AutoBackupMode.Synchronized,
                safeId = firstSafeId,
            ),
        )

        expected.forEach {
            val size = actual.size
            autoBackupErrorDataStore.addError(it)
            // Force sync between set and get
            while (actual.size == size) {
                yield()
            }
        }

        assertContentEquals(expected, actual)
        job.cancel()
    }
}
