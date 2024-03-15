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
 * Created by Lunabee Studio / Date - 2/9/2024 - for the oneSafe6 SDK.
 * Last modified 2/9/24, 1:53 PM
 */

package studio.lunabee.onesafe.migration

import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.TestCase.assertFalse
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.onesafe.domain.usecase.item.CreateItemUseCase
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltTest
import studio.lunabee.onesafe.test.test
import studio.lunabee.onesafe.test.testUUIDs
import java.io.File
import javax.inject.Inject
import kotlin.test.assertTrue

@HiltAndroidTest
class MigrationFromV10ToV11Test : OSHiltTest() {

    @get:Rule override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)
    override val initialTestState: InitialTestState = InitialTestState.LoggedIn

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val iconDir: File = File(context.filesDir, "icons")

    @Inject lateinit var createItemUseCase: CreateItemUseCase

    @Inject lateinit var migrationFromV10ToV11: MigrationFromV10ToV11

    @Test
    fun run_migrationFromV10ToV11_test(): TestResult = runTest {
        iconDir.mkdirs()
        val wrongIconFile = File(iconDir, testUUIDs[10].toString()).apply { writeText("0") }

        val item = createItemUseCase.test(icon = byteArrayOf(1))
        val itemIconFile = File(iconDir, item.iconId!!.toString())

        migrationFromV10ToV11()

        assertFalse(wrongIconFile.exists())
        assertTrue(itemIconFile.exists())
    }
}
