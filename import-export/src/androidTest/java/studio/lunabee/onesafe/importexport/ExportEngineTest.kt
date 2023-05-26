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
 * Created by Lunabee Studio / Date - 4/7/2023 - for the oneSafe6 SDK.
 * Last modified 4/7/23, 12:24 AM
 */

package studio.lunabee.onesafe.importexport

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.di.CryptoConstantsTestModule
import studio.lunabee.onesafe.cryptography.CryptoConstants
import studio.lunabee.onesafe.cryptography.qualifier.PBKDF2Iterations
import studio.lunabee.onesafe.domain.engine.ExportEngine
import studio.lunabee.onesafe.domain.model.importexport.OSArchiveKind
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltTest
import studio.lunabee.onesafe.test.assertSuccess
import java.io.File
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(CryptoConstantsTestModule::class)
class ExportEngineTest : OSHiltTest() {

    @BindValue
    @PBKDF2Iterations
    val iterationNumber: Int = CryptoConstants.PBKDF2Iterations

    @get:Rule
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @Inject
    @ApplicationContext
    lateinit var context: Context

    @Inject lateinit var exportEngine: ExportEngine

    override val initialTestState: InitialTestState = InitialTestState.LoggedIn

    @Test
    fun export_engine_empty_test(): TestResult = runTest {
        val prepareResult = exportEngine.prepareBackup(
            password = testPassword.toCharArray(),
            platformInfo = "AndroidTest",
            masterSalt = cryptoRepository.getCurrentSalt(),
        ).last()
        assertSuccess(prepareResult)

        val exportedArchiveResult = exportEngine.createExportArchiveContent(
            dataHolderFolder = File(context.cacheDir, "exportEngineTest"),
            safeItemsWithKeys = emptyMap(),
            safeItemFields = emptyList(),
            icons = emptyList(),
            archiveKind = OSArchiveKind.Backup,
        ).last()
        assertSuccess(exportedArchiveResult)
    }
}
