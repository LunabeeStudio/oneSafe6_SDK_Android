package studio.lunabee.onesafe.feature.onboarding

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.onesafe.domain.usecase.onboarding.ResetEditCryptoUseCase
import studio.lunabee.onesafe.error.OSDomainError
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltTest
import studio.lunabee.onesafe.test.assertFailure
import studio.lunabee.onesafe.test.assertSuccess
import javax.inject.Inject
import kotlin.test.assertEquals
import kotlin.test.assertIs

@HiltAndroidTest
class OnBoardingTest : OSHiltTest() {

    @get:Rule
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.SignedOut

    @Inject lateinit var resetEditCryptoUseCase: ResetEditCryptoUseCase

    @Test
    fun create_credential_and_save_test() {
        runTest {
            generateCryptoForNewSafeUseCase(PASSWORD.toCharArray())
            finishSafeCreationUseCase()
            logout()
            val signInResult = loginUseCase(PASSWORD.toCharArray())
            assertSuccess(signInResult)
        }
    }

    @Test
    fun create_credential_and_reset_test() {
        runTest {
            generateCryptoForNewSafeUseCase(PASSWORD.toCharArray())
            resetEditCryptoUseCase()
            val signInResult = loginUseCase(PASSWORD.toCharArray())
            val error = assertFailure(signInResult).throwable
            assertIs<OSDomainError>(error)
            assertEquals(OSDomainError.Code.SIGNIN_NOT_SIGNED_UP, error.code)
        }
    }

    companion object {
        private const val PASSWORD: String = "password"
    }
}
