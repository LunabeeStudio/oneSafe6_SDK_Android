package studio.lunabee.onesafe.usecase.importexport

import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.di.CryptoConstantsTestModule
import studio.lunabee.onesafe.cryptography.android.CryptoConstants
import studio.lunabee.onesafe.cryptography.android.PBKDF2JceHashEngine
import studio.lunabee.onesafe.cryptography.android.PasswordHashEngine
import studio.lunabee.onesafe.importexport.usecase.ExportBackupUseCase
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSTestConfig
import studio.lunabee.onesafe.test.StaticIdProvider
import studio.lunabee.onesafe.test.assertSuccess
import studio.lunabee.onesafe.test.firstSafeId
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(CryptoConstantsTestModule::class)
class ExportBackupUseCaseTest : AbstractExportUseCaseTest() {
    @get:Rule
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @BindValue
    val hashEngine: PasswordHashEngine = PBKDF2JceHashEngine(Dispatchers.Default, CryptoConstants.PBKDF2Iterations)

    @Inject lateinit var exportBackupUseCase: ExportBackupUseCase

    override val initialTestState: InitialTestState = InitialTestState.SignedOut

    @Test
    fun export_backup_data_test() {
        runTest {
            exportAndImport(importBubbles = true, importItems = true)
        }
    }

    @Test
    fun export_backup_data_without_items_test() {
        runTest {
            exportAndImport(importBubbles = true, importItems = false)
        }
    }

    @Test
    fun export_backup_data_without_bubbles_test() {
        runTest {
            exportAndImport(importBubbles = false, importItems = true)
        }
    }

    private suspend fun exportAndImport(importBubbles: Boolean, importItems: Boolean) {
        OSTestConfig.extraSafeIds.forEach { safeId ->
            StaticIdProvider.id = safeId.id
            signup(safeId.toString().toCharArray(), safeId)
            assertSuccess(login(safeId.toString().toCharArray()))

            setupItemsForTest(safeId)
            setupBubblesForTest()
            logout()
        }

        // Set items to first safe
        StaticIdProvider.id = firstSafeId.id
        signup()
        login()
        setupItemsForTest()
        setupBubblesForTest()

        val exportedArchiveResult = exportBackupUseCase(
            exportEngine = exportEngine,
            archiveExtractedDirectory = archiveCacheDir,
            safeId = firstSafeId,
        ).last()
        assertSuccess(exportedArchiveResult)

        signOut()
        signup()
        login()

        // Try to re-import exported archive.
        assertImport(
            archiveResult = exportedArchiveResult.successData.file,
            importBubbles = importBubbles,
            importItems = importItems,
        )
    }
}
