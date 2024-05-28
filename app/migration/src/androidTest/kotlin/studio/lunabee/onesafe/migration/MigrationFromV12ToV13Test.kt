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
 * Created by Lunabee Studio / Date - 3/12/2024 - for the oneSafe6 SDK.
 * Last modified 12/03/2024 09:05
 */

package studio.lunabee.onesafe.migration

import androidx.datastore.dataStoreFile
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.onesafe.DatastoreSettingsDataSource
import studio.lunabee.onesafe.model.LocalCtaState
import studio.lunabee.onesafe.storage.datastore.ProtoSerializer
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltTest
import javax.inject.Inject
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@HiltAndroidTest
class MigrationFromV12ToV13Test : OSHiltTest() {

    @get:Rule override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)
    override val initialTestState: InitialTestState = InitialTestState.LoggedIn

    @Inject lateinit var migrationFromV12ToV13: MigrationFromV12ToV13

    @Inject lateinit var datastoreSettingsDataSource: DatastoreSettingsDataSource

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val legacyDatastore = "b0e3c5a2-7959-4121-b664-372e544252cd"

    @Test
    fun run_migrationFromV12ToV13_test(): TestResult = runTest {
        // Setup V12 state
        val initLegacyFile = "$legacyDatastore.init"
        val datastore = ProtoSerializer.dataStore<LocalCtaState>(context, LocalCtaState.Hidden, initLegacyFile)
        val visibleSince = LocalCtaState.VisibleSince(1234L)
        datastore.updateData { visibleSince }
        // Avoid multiple opening of datastore
        context.dataStoreFile(initLegacyFile).renameTo(context.dataStoreFile(legacyDatastore))

        migrationFromV12ToV13()

        val ctaState = datastoreSettingsDataSource.enableAutoBackupCtaState.firstOrNull()
        assertEquals(visibleSince.toCtaState(), ctaState)
        assertFalse(context.dataStoreFile(legacyDatastore).exists())
    }
}
