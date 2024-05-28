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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.Test
import studio.lunabee.di.CryptoConstantsTestModule
import studio.lunabee.onesafe.cryptography.CryptoConstants
import studio.lunabee.onesafe.cryptography.PBKDF2JceHashEngine
import studio.lunabee.onesafe.cryptography.PasswordHashEngine
import studio.lunabee.onesafe.domain.model.importexport.OSArchiveKind
import studio.lunabee.onesafe.domain.qualifier.BackupType
import studio.lunabee.onesafe.error.OSCryptoError
import studio.lunabee.onesafe.importexport.engine.BackupExportEngine
import studio.lunabee.onesafe.importexport.model.ExportData
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltTest
import studio.lunabee.onesafe.test.assertFailure
import studio.lunabee.onesafe.test.assertSuccess
import java.io.File
import javax.inject.Inject
import kotlin.test.assertEquals
import kotlin.test.assertIs

@HiltAndroidTest
@UninstallModules(CryptoConstantsTestModule::class)
class BackupExportEngineTest : OSHiltTest() {

    @BindValue
    val hashEngine: PasswordHashEngine = PBKDF2JceHashEngine(Dispatchers.Default, CryptoConstants.PBKDF2Iterations)

    @get:Rule
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @Inject
    @ApplicationContext
    lateinit var context: Context

    @Inject
    @BackupType(BackupType.Type.Foreground)
    lateinit var exportEngine: BackupExportEngine

    override val initialTestState: InitialTestState = InitialTestState.LoggedIn

    @Test
    fun backup_export_engine_empty_test(): TestResult = runTest {
        val data = ExportData(
            safeItemsWithKeys = emptyMap(),
            safeItemFields = emptyList(),
            icons = emptyList(),
            files = emptyList(),
        )
        val exportedArchiveResult = exportEngine.createExportArchiveContent(
            dataHolderFolder = File(context.cacheDir, "exportEngineTest"),
            data = data,
            archiveKind = OSArchiveKind.Backup,
        ).last()
        assertSuccess(exportedArchiveResult)
    }

    @Test
    fun export_engine_not_signed_up_test(): TestResult = runTest {
        signOut()

        val data = ExportData(
            safeItemsWithKeys = emptyMap(),
            safeItemFields = emptyList(),
            icons = emptyList(),
            files = emptyList(),
        )
        val exportedArchiveResult = exportEngine.createExportArchiveContent(
            dataHolderFolder = File(context.cacheDir, "exportEngineTest"),
            data = data,
            archiveKind = OSArchiveKind.Backup,
        ).last()

        val failure = assertFailure(exportedArchiveResult).throwable
        assertIs<OSCryptoError>(failure)
        assertEquals(OSCryptoError.Code.MASTER_SALT_NOT_GENERATED, failure.code)
    }
}
