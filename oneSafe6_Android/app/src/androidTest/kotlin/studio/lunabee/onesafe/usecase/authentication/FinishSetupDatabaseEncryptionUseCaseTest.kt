package studio.lunabee.onesafe.usecase.authentication

import android.content.Context
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.lunabee.lbcore.model.LBFlowResult
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import io.mockk.coEvery
import io.mockk.spyk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import net.zetetic.database.sqlcipher.SQLiteDatabase
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import studio.lunabee.di.InMemoryMainDatabaseModule
import studio.lunabee.di.InMemoryMainDatabaseNamesModule
import studio.lunabee.onesafe.domain.model.crypto.DatabaseKey
import studio.lunabee.onesafe.domain.qualifier.DatabaseName
import studio.lunabee.onesafe.domain.repository.DatabaseEncryptionManager
import studio.lunabee.onesafe.domain.repository.DatabaseKeyRepository
import studio.lunabee.onesafe.domain.usecase.authentication.CreateDatabaseKeyUseCase
import studio.lunabee.onesafe.domain.usecase.authentication.FinishSetupDatabaseEncryptionUseCase
import studio.lunabee.onesafe.domain.usecase.authentication.SetDatabaseKeyUseCase
import studio.lunabee.onesafe.domain.usecase.authentication.StartSetupDatabaseEncryptionUseCase
import studio.lunabee.onesafe.domain.usecase.item.CreateItemUseCase
import studio.lunabee.onesafe.storage.MainDatabase
import studio.lunabee.onesafe.storage.SqlCipherDBManager
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltTest
import studio.lunabee.onesafe.test.OSTestConfig
import studio.lunabee.onesafe.test.assertDoesNotThrow
import studio.lunabee.onesafe.test.assertSuccess
import studio.lunabee.onesafe.test.test
import javax.inject.Inject
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull

@HiltAndroidTest
@UninstallModules(InMemoryMainDatabaseModule::class, InMemoryMainDatabaseNamesModule::class)
class FinishSetupDatabaseEncryptionUseCaseTest : OSHiltTest() {
    @get:Rule
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.Home()

    @Inject
    lateinit var databaseKeyRepository: DatabaseKeyRepository

    @Inject
    lateinit var createDatabaseKeyUseCase: CreateDatabaseKeyUseCase

    @Inject
    lateinit var startSetup: StartSetupDatabaseEncryptionUseCase

    @Inject
    lateinit var finishSetup: FinishSetupDatabaseEncryptionUseCase

    @Inject
    lateinit var createItemUseCase: CreateItemUseCase

    @Inject
    lateinit var setDatabaseKeyUseCase: SetDatabaseKeyUseCase

    val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @BindValue
    @DatabaseName(DatabaseName.Type.Main)
    val mainDb: String = "test-main-db"

    @BindValue
    @DatabaseName(DatabaseName.Type.CipherTemp)
    val tempCipherDb: String = "test-temp-cipher-db"

    @BindValue
    val sqlCipherManager: DatabaseEncryptionManager = spyk(SqlCipherDBManager(Dispatchers.IO, context, mainDb, tempCipherDb))

    @BindValue
    val plainDb: MainDatabase = getDatabase(null)

    @Before
    fun setUp() {
        SQLiteDatabase.deleteDatabase(context.getDatabasePath(tempCipherDb))
    }

    /**
     * No-op flow
     */
    @Test
    fun noop_test(): TestResult = runTest {
        val state = finishSetup().last()
        val data = assertSuccess(state).successData
        assertEquals(FinishSetupDatabaseEncryptionUseCase.SuccessState.Noop, data)
        assertDoesNotThrow { plainDb.openHelper.writableDatabase.version } // database openable
    }

    /**
     * Full success flow
     */
    @Test
    fun done_flow_test(): TestResult = runTest {
        createItemUseCase.test() // insert data to generate shm & wal db files
        val newKey = createDatabaseKeyUseCase()
        startSetup(newKey)
        val state = finishSetup().last()
        val data = assertSuccess(state).successData
        assertEquals(FinishSetupDatabaseEncryptionUseCase.SuccessState.Success, data)
        val key = databaseKeyRepository.getKeyFlow().first()!!
        val actual = setDatabaseKeyUseCase(key.asCharArray().joinToString(""))
        assertSuccess(actual)

        val cipherDb = getDatabase(key)
        assertDoesNotThrow { cipherDb.openHelper.writableDatabase.version } // database openable
        assertNull(databaseKeyRepository.getBackupKeyFlow().firstOrNull()) // backup key removed
    }

    /**
     * Canceled flow
     */
    @Test
    fun canceled_flow_test(): TestResult = runTest {
        // Encrypt DB
        val newKey = createDatabaseKeyUseCase()
        startSetup(newKey)
        finishSetup().last()

        val oldKey = databaseKeyRepository.getKeyFlow().first()!!
        startSetup(null)

        // Check key is backup & removed
        val backupKey = databaseKeyRepository.getBackupKeyFlow().first()!!
        assertContentEquals(oldKey.raw, backupKey.raw)
        assertNull(databaseKeyRepository.getKeyFlow().first())

        // Run canceled flow
        coEvery { sqlCipherManager.finishMigrationIfNeeded(null, oldKey) } returns
            flowOf(LBFlowResult.Success(DatabaseEncryptionManager.MigrationState.Canceled))
        val state = finishSetup().last()
        val data = assertSuccess(state).successData
        assertEquals(FinishSetupDatabaseEncryptionUseCase.SuccessState.Canceled, data)

        val restoredKey = databaseKeyRepository.getKeyFlow().first()!!
        assertContentEquals(oldKey.raw, restoredKey.raw) // key restored
        assertNull(databaseKeyRepository.getBackupKeyFlow().firstOrNull()) // backup key removed

        val cipherDb = getDatabase(restoredKey)
        assertDoesNotThrow { cipherDb.openHelper.writableDatabase.version } // cipher database openable
    }

    /**
     * Canceled due to key exist but no migration running (no-op state)
     */
    @Test
    fun wrong_key_canceled_flow_test(): TestResult = runTest {
        // Set key manually
        databaseKeyRepository.setKey(DatabaseKey(OSTestConfig.random.nextBytes(32)), true)

        // Finish setup without starting it
        val state = finishSetup().last()
        val data = assertSuccess(state).successData

        // Assert canceled state & database openable without key
        assertEquals(FinishSetupDatabaseEncryptionUseCase.SuccessState.Canceled, data)
        assertDoesNotThrow { plainDb.openHelper.writableDatabase.version } // database openable
    }

    private fun getDatabase(key: DatabaseKey?) = Room.databaseBuilder(
        context,
        MainDatabase::class.java,
        mainDb,
    )
        .openHelperFactory(SupportOpenHelperFactory(key?.raw))
        .build()
}
