package studio.lunabee.onesafe.usecase.importexport

import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.di.CryptoConstantsTestModule
import studio.lunabee.di.MigrationCryptoModule
import studio.lunabee.onesafe.cryptography.android.CryptoConstants
import studio.lunabee.onesafe.cryptography.android.PBKDF2JceHashEngine
import studio.lunabee.onesafe.cryptography.android.PasswordHashEngine
import studio.lunabee.onesafe.domain.model.importexport.ImportMode
import studio.lunabee.onesafe.domain.repository.MigrationCryptoRepository
import studio.lunabee.onesafe.error.OSImportExportError
import studio.lunabee.onesafe.importexport.usecase.MigrateOldArchiveUseCase
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltUnitTest
import studio.lunabee.onesafe.test.assertFailure
import studio.lunabee.onesafe.test.assertSuccess
import javax.inject.Inject
import kotlin.test.assertEquals
import kotlin.test.assertIs

@HiltAndroidTest
@UninstallModules(CryptoConstantsTestModule::class, MigrationCryptoModule::class)
class MigrateOldArchiveUseCaseTest : OSHiltUnitTest() {

    // Replace binding to get the expected password
    @BindValue
    val migrationCryptoRepository: MigrationCryptoRepository = mockk<MigrationCryptoRepository>().apply {
        every { decryptMigrationArchivePassword(byteArrayOf()) } returns "password".toCharArray()
        every { decryptMigrationArchivePassword(byteArrayOf(1)) } returns "bad".toCharArray()
    }

    @BindValue
    val hashEngine: PasswordHashEngine = PBKDF2JceHashEngine(Dispatchers.Default, CryptoConstants.PBKDF2Iterations)

    @Inject
    lateinit var migrateOldArchiveUseCase: MigrateOldArchiveUseCase

    @get:Rule
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.Home()

    @Test
    fun migrateOldArchiveUseCase_test(): TestResult = runTest {
        val inputStream = javaClass.classLoader!!.getResourceAsStream("os5_migration_archive")
        val result = migrateOldArchiveUseCase(ImportMode.AppendInFolder, inputStream, byteArrayOf()).last()
        assertSuccess(result)
    }

    @Test
    fun migrateOldArchiveUseCase_wrong_password_test(): TestResult = runTest {
        val inputStream = javaClass.classLoader!!.getResourceAsStream("os5_migration_archive")
        val result = migrateOldArchiveUseCase(ImportMode.AppendInFolder, inputStream, byteArrayOf(1)).last()
        assertFailure(result)
        val error = assertIs<OSImportExportError>(result.throwable)
        assertEquals(OSImportExportError.Code.WRONG_CREDENTIALS, error.code)
    }
}
