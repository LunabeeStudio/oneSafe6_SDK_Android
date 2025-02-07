package studio.lunabee.onesafe.usecase.authentication

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.coEvery
import io.mockk.spyk
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.onesafe.domain.model.crypto.DatabaseKey
import studio.lunabee.onesafe.domain.repository.DatabaseEncryptionManager
import studio.lunabee.onesafe.domain.usecase.authentication.StartSetupDatabaseEncryptionUseCase
import studio.lunabee.onesafe.error.OSCryptoError
import studio.lunabee.onesafe.jvm.get
import studio.lunabee.onesafe.test.DummyDatabaseCryptoRepository
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltTest
import studio.lunabee.onesafe.test.OSTestConfig
import studio.lunabee.onesafe.test.assertFailure
import studio.lunabee.onesafe.test.assertThrows
import javax.inject.Inject
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@HiltAndroidTest
class StartSetupDatabaseEncryptionUseCaseTest : OSHiltTest() {
    @get:Rule
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)
    override val initialTestState: InitialTestState = InitialTestState.Home()

    @Inject
    lateinit var sqlCipherManager: DatabaseEncryptionManager

    private val spySqlCipherManager: DatabaseEncryptionManager by lazy { spyk(sqlCipherManager) }

    private val databaseKey = DatabaseKey(OSTestConfig.random.nextBytes(DatabaseKey.DatabaseKeyByteSize))

    private val databaseKeyRepository: DummyDatabaseCryptoRepository = DummyDatabaseCryptoRepository(databaseKey)

    private val startSetupDatabaseEncryptionUseCase by lazy {
        StartSetupDatabaseEncryptionUseCase(
            databaseKeyRepository,
            spySqlCipherManager,
        )
    }

    /**
     * Check that if migrateToEncrypted fails, the key is removed
     */
    @Test
    fun enable_error_test(): TestResult = runTest {
        coEvery { spySqlCipherManager.migrateToEncrypted(databaseKey) } returns Unit
        startSetupDatabaseEncryptionUseCase(databaseKey)
        assertNotNull(databaseKeyRepository.key.value)

        coEvery { spySqlCipherManager.migrateToEncrypted(databaseKey) } throws Exception("error")
        assertThrows<Exception> {
            startSetupDatabaseEncryptionUseCase(databaseKey)
        }
        assertNull(databaseKeyRepository.key.value)
    }

    /**
     * Check that if migrateToEncrypted fails because the key already exists, the key is NOT removed
     */
    @Test
    fun enable_already_exists_error_test(): TestResult = runTest {
        coEvery { spySqlCipherManager.migrateToEncrypted(databaseKey) } returns Unit
        startSetupDatabaseEncryptionUseCase(databaseKey)
        val expected = databaseKeyRepository.key.value
        assertNotNull(expected)

        coEvery { spySqlCipherManager.migrateToEncrypted(databaseKey) } throws
            OSCryptoError.Code.DATASTORE_ENTRY_KEY_ALREADY_EXIST.get()
        val result = startSetupDatabaseEncryptionUseCase(databaseKey)
        assertFailure(result)
        assertEquals(expected, databaseKeyRepository.key.value)
    }

    @Test
    fun disable_error_test(): TestResult = runTest {
        databaseKeyRepository.setKey(databaseKey, true)
        coEvery { spySqlCipherManager.migrateToPlain(databaseKey) } returns Unit
        startSetupDatabaseEncryptionUseCase(null)
        assertNull(databaseKeyRepository.key.value)
        assertNotNull(databaseKeyRepository.backupKey.value)

        databaseKeyRepository.setKey(databaseKey, true)
        coEvery { spySqlCipherManager.migrateToPlain(databaseKey) } throws Exception("error")
        assertThrows<Exception> {
            startSetupDatabaseEncryptionUseCase(null)
        }
        assertNotNull(databaseKeyRepository.key.value)
        assertNull(databaseKeyRepository.backupKey.value)
    }
}
