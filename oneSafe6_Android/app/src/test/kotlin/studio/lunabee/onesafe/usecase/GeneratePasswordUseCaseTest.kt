package studio.lunabee.onesafe.usecase

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.onesafe.domain.model.password.PasswordConfig
import studio.lunabee.onesafe.domain.usecase.GeneratePasswordUseCase
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltUnitTest
import javax.inject.Inject
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@HiltAndroidTest
class GeneratePasswordUseCaseTest : OSHiltUnitTest() {

    @get:Rule
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)
    override val initialTestState: InitialTestState = InitialTestState.SignedOut

    @Inject
    lateinit var generatePasswordUseCase: GeneratePasswordUseCase

    private val testListSize = 1000

    @Test
    fun test_password_generation_length(): TestResult = runTest {
        val passwordConfig = PasswordConfig.default()
        val passwords = List(testListSize) { generatePasswordUseCase(passwordConfig) }
        assertTrue { passwords.none { it.value.length != passwordConfig.length } }
    }

    @Test
    fun test_password_generation_no_upper_case(): TestResult = runTest {
        var passwordConfig = PasswordConfig.default()
        passwordConfig = passwordConfig.copy(includeUpperCase = false)
        val passwords = List(testListSize) { generatePasswordUseCase(passwordConfig) }
        assertTrue { passwords.none { it.value.any { char -> char.isLetter() && char.isUpperCase() } } }
    }

    @Test
    fun test_password_generation_no_lower_case(): TestResult = runTest {
        var passwordConfig = PasswordConfig.default()
        passwordConfig = passwordConfig.copy(includeLowerCase = false)
        val passwords = List(testListSize) { generatePasswordUseCase(passwordConfig) }
        assertTrue { passwords.none { it.value.any { char -> char.isLetter() && char.isLowerCase() } } }
    }

    @Test
    fun test_password_generation_no_number_case(): TestResult = runTest {
        var passwordConfig = PasswordConfig.default()
        passwordConfig = passwordConfig.copy(includeNumber = false)
        val passwords = List(testListSize) { generatePasswordUseCase(passwordConfig) }
        assertTrue { passwords.none { it.value.any { char -> char.isDigit() } } }
    }

    @Test
    fun test_password_generation_no_symbol_case(): TestResult = runTest {
        var passwordConfig = PasswordConfig.default()
        passwordConfig = passwordConfig.copy(includeSymbol = false)
        val passwords = List(testListSize) { generatePasswordUseCase(passwordConfig) }
        assertTrue { passwords.none { it.value.matches(Regex(PasswordConfig.SpecialCharPattern)) } }
    }

    @Test
    fun test_function_password_matches_config(): TestResult = runTest {
        var passwordConfig = PasswordConfig.default()
        assertFalse(passwordConfig.matchesConfig("AZERT123!"))
        assertTrue(passwordConfig.matchesConfig("Azerty123!qwerty12"))

        assertFalse(passwordConfig.matchesConfig("Azerty123!q")) // Length not match
        assertFalse(passwordConfig.matchesConfig("azerty123!qw")) // uppercase not match
        assertFalse(passwordConfig.matchesConfig("AZERTY123!QW")) // lower not match
        assertFalse(passwordConfig.matchesConfig("AZEertyabc!QW")) // number not match
        assertFalse(passwordConfig.matchesConfig("Azerty123aQW")) // symbols not match
        assertFalse(passwordConfig.matchesConfig("Azerty123aQ ")) // has space

        passwordConfig = passwordConfig.copy(includeUpperCase = false)
        assertTrue(passwordConfig.matchesConfig("azerty123!qwerty12"))

        passwordConfig = passwordConfig.copy(includeUpperCase = true, includeLowerCase = false)
        assertTrue(passwordConfig.matchesConfig("AZERTY123!QWERTY12"))

        passwordConfig = passwordConfig.copy(includeLowerCase = true, includeNumber = false)
        assertTrue(passwordConfig.matchesConfig("AZErtyabc!QWertyxy"))

        passwordConfig = passwordConfig.copy(includeNumber = true, includeSymbol = false)
        assertTrue(passwordConfig.matchesConfig("Azerty123aQWerty12"))
    }
}
