package studio.lunabee.onesafe.usecase.authentication

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.onesafe.test.OSHiltUnitTest
import studio.lunabee.onesafe.domain.usecase.authentication.IsPasswordCorrectUseCase
import studio.lunabee.onesafe.error.OSCryptoError
import studio.lunabee.onesafe.error.OSRepositoryError
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.assertFailure
import studio.lunabee.onesafe.test.assertSuccess
import javax.inject.Inject
import kotlin.test.assertEquals
import kotlin.test.assertIs

@HiltAndroidTest
class IsPasswordCorrectUseCaseTest : OSHiltUnitTest() {
    @get:Rule
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @Inject
    lateinit var isPasswordCorrectUseCase: IsPasswordCorrectUseCase

    override val initialTestState: InitialTestState = InitialTestState.Home()

    @Test
    fun check_password_valid_test() {
        runTest {
            val result = isPasswordCorrectUseCase(
                password = testPassword.toCharArray(),
            )
            assertSuccess(result)
        }
    }

    @Test
    fun check_password_wrong_test() {
        runTest {
            val result = isPasswordCorrectUseCase(
                password = "testPassword".toCharArray(),
            )
            val error = assertFailure(result).throwable
            assertIs<OSCryptoError>(error)
            assertEquals(OSCryptoError.Code.MASTER_KEY_WRONG_PASSWORD, error.code)
        }
    }

    @Test
    fun check_password_no_crypto_test() {
        runTest {
            signOut()
            val result = isPasswordCorrectUseCase(
                password = "testPassword".toCharArray(),
            )
            val error = assertFailure(result).throwable
            assertIs<OSRepositoryError>(error)
            assertEquals(OSRepositoryError.Code.SAFE_ID_NOT_LOADED, error.code)
        }
    }
}
