package studio.lunabee.onesafe.usecase

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.onesafe.domain.model.safe.BiometricCryptoMaterial
import studio.lunabee.onesafe.domain.model.verifypassword.VerifyPasswordInterval
import studio.lunabee.onesafe.domain.repository.SecuritySettingsRepository
import studio.lunabee.onesafe.domain.usecase.verifypassword.ShouldVerifyPasswordUseCase
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltUnitTest
import studio.lunabee.onesafe.test.OSTestConfig
import studio.lunabee.onesafe.test.assertSuccess
import studio.lunabee.onesafe.test.firstSafeId
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.inject.Inject
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@HiltAndroidTest
class ShouldVerifyPasswordUseCaseTest : OSHiltUnitTest() {

    @get:Rule override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.Home()

    @Inject lateinit var shouldVerifyPasswordUseCase: ShouldVerifyPasswordUseCase

    @Inject lateinit var securitySettingsRepository: SecuritySettingsRepository

    @Test
    fun should_never_verify_password_test(): TestResult = runTest {
        setBiometryOnSafe()
        securitySettingsRepository.setVerifyPasswordInterval(firstSafeId, VerifyPasswordInterval.NEVER)
        assertFalse(assertSuccess(shouldVerifyPasswordUseCase()).successData)
    }

    @Test
    fun last_password_verification_6_day_ago_test(): TestResult = runTest {
        setBiometryOnSafe()
        securitySettingsRepository.setLastPasswordVerification(firstSafeId, getTimeStampAgo(daysAgo = 6))
        securitySettingsRepository.setVerifyPasswordInterval(firstSafeId, VerifyPasswordInterval.EVERY_WEEK)
        assertFalse(assertSuccess(shouldVerifyPasswordUseCase()).successData)
        securitySettingsRepository.setVerifyPasswordInterval(firstSafeId, VerifyPasswordInterval.EVERY_TWO_WEEKS)
        assertFalse(assertSuccess(shouldVerifyPasswordUseCase()).successData)
        securitySettingsRepository.setVerifyPasswordInterval(firstSafeId, VerifyPasswordInterval.EVERY_MONTH)
        assertFalse(assertSuccess(shouldVerifyPasswordUseCase()).successData)
        securitySettingsRepository.setVerifyPasswordInterval(firstSafeId, VerifyPasswordInterval.EVERY_TWO_MONTHS)
        assertFalse(assertSuccess(shouldVerifyPasswordUseCase()).successData)
        securitySettingsRepository.setVerifyPasswordInterval(firstSafeId, VerifyPasswordInterval.EVERY_SIX_MONTHS)
        assertFalse(assertSuccess(shouldVerifyPasswordUseCase()).successData)
    }

    @Test
    fun last_password_verification_10_day_ago_test(): TestResult = runTest {
        setBiometryOnSafe()
        securitySettingsRepository.setLastPasswordVerification(firstSafeId, getTimeStampAgo(daysAgo = 10))
        securitySettingsRepository.setVerifyPasswordInterval(firstSafeId, VerifyPasswordInterval.EVERY_WEEK)
        assertTrue(assertSuccess(shouldVerifyPasswordUseCase()).successData)
        securitySettingsRepository.setVerifyPasswordInterval(firstSafeId, VerifyPasswordInterval.EVERY_TWO_WEEKS)
        assertFalse(assertSuccess(shouldVerifyPasswordUseCase()).successData)
        securitySettingsRepository.setVerifyPasswordInterval(firstSafeId, VerifyPasswordInterval.EVERY_MONTH)
        assertFalse(assertSuccess(shouldVerifyPasswordUseCase()).successData)
        securitySettingsRepository.setVerifyPasswordInterval(firstSafeId, VerifyPasswordInterval.EVERY_TWO_MONTHS)
        assertFalse(assertSuccess(shouldVerifyPasswordUseCase()).successData)
        securitySettingsRepository.setVerifyPasswordInterval(firstSafeId, VerifyPasswordInterval.EVERY_SIX_MONTHS)
        assertFalse(assertSuccess(shouldVerifyPasswordUseCase()).successData)
    }

    @Test
    fun last_password_verification_two_weeks_and_a_day_ago_test(): TestResult = runTest {
        setBiometryOnSafe()
        securitySettingsRepository.setLastPasswordVerification(firstSafeId, getTimeStampAgo(weeksAgo = 2, daysAgo = 1))
        securitySettingsRepository.setVerifyPasswordInterval(firstSafeId, VerifyPasswordInterval.EVERY_WEEK)
        assertTrue(assertSuccess(shouldVerifyPasswordUseCase()).successData)
        securitySettingsRepository.setVerifyPasswordInterval(firstSafeId, VerifyPasswordInterval.EVERY_TWO_WEEKS)
        assertTrue(assertSuccess(shouldVerifyPasswordUseCase()).successData)
        securitySettingsRepository.setVerifyPasswordInterval(firstSafeId, VerifyPasswordInterval.EVERY_MONTH)
        assertFalse(assertSuccess(shouldVerifyPasswordUseCase()).successData)
        securitySettingsRepository.setVerifyPasswordInterval(firstSafeId, VerifyPasswordInterval.EVERY_TWO_MONTHS)
        assertFalse(assertSuccess(shouldVerifyPasswordUseCase()).successData)
        securitySettingsRepository.setVerifyPasswordInterval(firstSafeId, VerifyPasswordInterval.EVERY_SIX_MONTHS)
        assertFalse(assertSuccess(shouldVerifyPasswordUseCase()).successData)
    }

    @Test
    fun last_password_verification_1_month_and_a_day_ago_test(): TestResult = runTest {
        setBiometryOnSafe()
        securitySettingsRepository.setLastPasswordVerification(firstSafeId, getTimeStampAgo(monthsAgo = 1, daysAgo = 1))
        securitySettingsRepository.setVerifyPasswordInterval(firstSafeId, VerifyPasswordInterval.EVERY_TWO_WEEKS)
        assertTrue(assertSuccess(shouldVerifyPasswordUseCase()).successData)
        securitySettingsRepository.setVerifyPasswordInterval(firstSafeId, VerifyPasswordInterval.EVERY_MONTH)
        assertTrue(assertSuccess(shouldVerifyPasswordUseCase()).successData)
        securitySettingsRepository.setVerifyPasswordInterval(firstSafeId, VerifyPasswordInterval.EVERY_TWO_MONTHS)
        assertFalse(assertSuccess(shouldVerifyPasswordUseCase()).successData)
        securitySettingsRepository.setVerifyPasswordInterval(firstSafeId, VerifyPasswordInterval.EVERY_SIX_MONTHS)
        assertFalse(assertSuccess(shouldVerifyPasswordUseCase()).successData)
    }

    @Test
    fun last_password_verification_2_month_and_a_day_ago_test(): TestResult = runTest {
        setBiometryOnSafe()
        securitySettingsRepository.setLastPasswordVerification(firstSafeId, getTimeStampAgo(monthsAgo = 2, daysAgo = 1))
        securitySettingsRepository.setVerifyPasswordInterval(firstSafeId, VerifyPasswordInterval.EVERY_WEEK)
        assertTrue(assertSuccess(shouldVerifyPasswordUseCase()).successData)
        securitySettingsRepository.setVerifyPasswordInterval(firstSafeId, VerifyPasswordInterval.EVERY_TWO_WEEKS)
        assertTrue(assertSuccess(shouldVerifyPasswordUseCase()).successData)
        securitySettingsRepository.setVerifyPasswordInterval(firstSafeId, VerifyPasswordInterval.EVERY_MONTH)
        assertTrue(assertSuccess(shouldVerifyPasswordUseCase()).successData)
        securitySettingsRepository.setVerifyPasswordInterval(firstSafeId, VerifyPasswordInterval.EVERY_TWO_MONTHS)
        assertTrue(assertSuccess(shouldVerifyPasswordUseCase()).successData)
        securitySettingsRepository.setVerifyPasswordInterval(firstSafeId, VerifyPasswordInterval.EVERY_SIX_MONTHS)
        assertFalse(assertSuccess(shouldVerifyPasswordUseCase()).successData)
    }

    @Test
    fun last_password_verification_6_month_and_a_day_ago_test(): TestResult = runTest {
        setBiometryOnSafe()
        securitySettingsRepository.setLastPasswordVerification(firstSafeId, getTimeStampAgo(monthsAgo = 6, daysAgo = 1))
        securitySettingsRepository.setVerifyPasswordInterval(firstSafeId, VerifyPasswordInterval.EVERY_WEEK)
        assertTrue(assertSuccess(shouldVerifyPasswordUseCase()).successData)
        securitySettingsRepository.setVerifyPasswordInterval(firstSafeId, VerifyPasswordInterval.EVERY_TWO_WEEKS)
        assertTrue(assertSuccess(shouldVerifyPasswordUseCase()).successData)
        securitySettingsRepository.setVerifyPasswordInterval(firstSafeId, VerifyPasswordInterval.EVERY_MONTH)
        assertTrue(assertSuccess(shouldVerifyPasswordUseCase()).successData)
        securitySettingsRepository.setVerifyPasswordInterval(firstSafeId, VerifyPasswordInterval.EVERY_TWO_MONTHS)
        assertTrue(assertSuccess(shouldVerifyPasswordUseCase()).successData)
        securitySettingsRepository.setVerifyPasswordInterval(firstSafeId, VerifyPasswordInterval.EVERY_SIX_MONTHS)
        assertTrue(assertSuccess(shouldVerifyPasswordUseCase()).successData)
    }

    @Test
    fun last_password_verification_on_safe_without_biometry_test(): TestResult = runTest {
        securitySettingsRepository.setLastPasswordVerification(firstSafeId, getTimeStampAgo(monthsAgo = 6, daysAgo = 1))
        securitySettingsRepository.setVerifyPasswordInterval(firstSafeId, VerifyPasswordInterval.EVERY_WEEK)
        assertFalse(assertSuccess(shouldVerifyPasswordUseCase()).successData)
        securitySettingsRepository.setVerifyPasswordInterval(firstSafeId, VerifyPasswordInterval.EVERY_TWO_WEEKS)
        assertFalse(assertSuccess(shouldVerifyPasswordUseCase()).successData)
        securitySettingsRepository.setVerifyPasswordInterval(firstSafeId, VerifyPasswordInterval.EVERY_MONTH)
        assertFalse(assertSuccess(shouldVerifyPasswordUseCase()).successData)
        securitySettingsRepository.setVerifyPasswordInterval(firstSafeId, VerifyPasswordInterval.EVERY_TWO_MONTHS)
        assertFalse(assertSuccess(shouldVerifyPasswordUseCase()).successData)
        securitySettingsRepository.setVerifyPasswordInterval(firstSafeId, VerifyPasswordInterval.EVERY_SIX_MONTHS)
        assertFalse(assertSuccess(shouldVerifyPasswordUseCase()).successData)
    }

    private suspend fun setBiometryOnSafe() {
        safeRepository.setBiometricMaterial(
            firstSafeId,
            BiometricCryptoMaterial(
                iv = OSTestConfig.random.nextBytes(16),
                key = OSTestConfig.random.nextBytes(48),
            ),
        )
    }

    private fun getTimeStampAgo(monthsAgo: Long = 0, weeksAgo: Long = 0, daysAgo: Long = 0): Instant =
        LocalDateTime.now(testClock)
            .minusMonths(monthsAgo)
            .minusWeeks(weeksAgo)
            .minusDays(daysAgo)
            .atZone(ZoneOffset.UTC)
            .toInstant()
}
