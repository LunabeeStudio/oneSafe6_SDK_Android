package studio.lunabee.onesafe.usecase.importexport

import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import studio.lunabee.di.CryptoConstantsTestModule
import studio.lunabee.onesafe.cryptography.android.CryptoConstants
import studio.lunabee.onesafe.cryptography.android.PBKDF2JceHashEngine
import studio.lunabee.onesafe.cryptography.android.PasswordHashEngine
import studio.lunabee.onesafe.domain.qualifier.InternalDir
import studio.lunabee.onesafe.importexport.usecase.LocalAutoBackupUseCase
import studio.lunabee.onesafe.test.assertSuccess
import studio.lunabee.onesafe.test.firstSafeId
import java.io.File
import javax.inject.Inject
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@HiltAndroidTest
@UninstallModules(CryptoConstantsTestModule::class)
class LocalAutoBackupUseCaseTest : AbstractExportUseCaseTest() {
    @get:Rule
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @BindValue
    val hashEngine: PasswordHashEngine = PBKDF2JceHashEngine(Dispatchers.Default, CryptoConstants.PBKDF2Iterations)

    @Inject lateinit var autoBackupUseCase: LocalAutoBackupUseCase

    @Inject
    @InternalDir(InternalDir.Type.Backups)
    lateinit var backupDir: File

    @Before
    fun setUp() {
        backupDir.deleteRecursively()
    }

    @After
    fun tearsDown() {
        backupDir.deleteRecursively()
    }

    @Test
    fun auto_backup_data_test() {
        runTest {
            signup()
            login()
            setupItemsForTest()
            setupBubblesForTest()
            val exportedArchiveResult = autoBackupUseCase(firstSafeId).last()
            assertSuccess(exportedArchiveResult)
            assertTrue(archiveCacheDir.listFiles()!!.isEmpty()) // assert cache cleaned

            val backups = localBackupRepository.getBackups(firstSafeId)
            assertEquals(1, backups.count()) // assert backup created

            // Try to re-import exported archive.
            assertImport(backups.first().data!!.file, importItems = true, importBubbles = true)
        }
    }
}
