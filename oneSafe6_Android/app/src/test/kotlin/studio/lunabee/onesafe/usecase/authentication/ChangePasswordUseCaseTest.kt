package studio.lunabee.onesafe.usecase.authentication

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.coEvery
import io.mockk.spyk
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemKey
import studio.lunabee.onesafe.domain.repository.EditCryptoRepository
import studio.lunabee.onesafe.domain.repository.SafeItemKeyRepository
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.domain.repository.SecuritySettingsRepository
import studio.lunabee.onesafe.domain.repository.StorageManager
import studio.lunabee.onesafe.domain.usecase.authentication.ChangePasswordUseCase
import studio.lunabee.onesafe.domain.usecase.item.CreateItemUseCase
import studio.lunabee.onesafe.domain.usecase.item.ItemDecryptUseCase
import studio.lunabee.onesafe.domain.usecase.settings.GetSecuritySettingUseCase
import studio.lunabee.onesafe.domain.usecase.settings.SetSecuritySettingUseCase
import studio.lunabee.onesafe.error.OSRepositoryError
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltUnitTest
import studio.lunabee.onesafe.test.assertFailure
import studio.lunabee.onesafe.test.assertSuccess
import studio.lunabee.onesafe.test.assertThrows
import studio.lunabee.onesafe.test.firstSafeId
import studio.lunabee.onesafe.test.test
import studio.lunabee.onesafe.test.testUUIDs
import javax.inject.Inject
import kotlin.test.assertEquals
import kotlin.test.assertIs

@HiltAndroidTest
class ChangePasswordUseCaseTest : OSHiltUnitTest() {

    @get:Rule
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @Inject lateinit var createItemUseCase: CreateItemUseCase

    @Inject lateinit var safeItemRepository: SafeItemRepository

    @Inject lateinit var safeItemKeyRepository: SafeItemKeyRepository

    @Inject lateinit var decryptUseCase: ItemDecryptUseCase

    @Inject lateinit var editCryptoRepository: EditCryptoRepository

    @Inject lateinit var setSecuritySettingUseCase: SetSecuritySettingUseCase

    @Inject lateinit var getSecuritySettingUseCase: GetSecuritySettingUseCase

    @Inject lateinit var securitySettingsRepository: SecuritySettingsRepository

    @Inject lateinit var transactionManager: StorageManager

    override val initialTestState: InitialTestState = InitialTestState.Home()

    private lateinit var itemKey: SafeItemKey
    private val itemName = "name"
    private val newPassword
        get() = charArrayOf('z')

    @Before
    fun setup(): TestResult = runTest {
        createItemUseCase.test(itemName)
        itemKey = safeItemKeyRepository.getSafeItemKey(testUUIDs[0])
    }

    @Inject lateinit var changePasswordUseCase: ChangePasswordUseCase

    @Test
    fun changePasswordUseCase_test(): TestResult = runTest {
        assertSuccess(changePasswordUseCase(newPassword))
        logout()
        assertSuccess(loginUseCase(newPassword))
        val item = safeItemRepository.getSafeItem(testUUIDs[0])
        val actual = decryptUseCase(item.encName!!, item.id, String::class).data
        assertEquals(itemName, actual)
        assertEquals(
            expected = testClock.millis().toDouble(),
            actual = getSecuritySettingUseCase.lastPasswordVerificationInstant().data?.toEpochMilli()?.toDouble() ?: 0.0,
            absoluteTolerance = 1000.0,
        )
    }

    @Test
    fun changePasswordUseCase_notSignedIn_test(): TestResult = runTest {
        logout()
        val error = assertFailure(changePasswordUseCase(newPassword)).throwable
        assertIs<OSRepositoryError>(error)
        assertEquals(OSRepositoryError.Code.SAFE_ID_NOT_LOADED, error.code)

        // Check old password still works
        assertSuccess(loginUseCase(testPassword.toCharArray()))
        val item = safeItemRepository.getSafeItem(testUUIDs[0])
        val actual = decryptUseCase(item.encName!!, item.id, String::class).data
        assertEquals(itemName, actual)
    }

    // Make sure that any error during ChangePasswordUseCase will rollback the key re-encryption
    @Test
    fun changePasswordUseCase_transactionFailure_test(): TestResult = runTest {
        val editCryptoRepository = spyk(editCryptoRepository) {
            coEvery { overrideMainCryptographicData(firstSafeId) } throws Exception("dummy error")
        }

        val changePasswordUseCase = ChangePasswordUseCase(
            safeItemKeyRepository = safeItemKeyRepository,
            editCryptoRepository = editCryptoRepository,
            transactionManager = transactionManager,
            setSecuritySettingUseCase = setSecuritySettingUseCase,
            safeRepository = safeRepository,
        )

        assertThrows<Exception> {
            changePasswordUseCase(newPassword)
        }

        // Check old password still works
        logout()
        assertSuccess(loginUseCase(testPassword.toCharArray()))
        val item = safeItemRepository.getSafeItem(testUUIDs[0])
        val actual = decryptUseCase(item.encName!!, item.id, String::class).data
        assertEquals(itemName, actual)
    }
}
