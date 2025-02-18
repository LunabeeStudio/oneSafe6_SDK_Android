package studio.lunabee.onesafe.usecase.autolock

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.onesafe.commonui.settings.AutoLockInactivityDelay
import studio.lunabee.onesafe.domain.repository.AutoLockRepository
import studio.lunabee.onesafe.domain.repository.SecuritySettingsRepository
import studio.lunabee.onesafe.domain.usecase.autolock.AutoLockInactivityGetRemainingTimeUseCase
import studio.lunabee.onesafe.domain.usecase.autolock.RefreshLastUserInteractionUseCase
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltUnitTest
import studio.lunabee.onesafe.test.firstSafeId
import java.time.Instant
import javax.inject.Inject
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@HiltAndroidTest
class AutoLockInactivityGetRemainingTimeUseCaseTest : OSHiltUnitTest() {
    @get:Rule
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.Home()

    @Inject lateinit var autoLockInactivityGetRemainingTimeUseCase: AutoLockInactivityGetRemainingTimeUseCase

    @Inject lateinit var securitySettingsRepository: SecuritySettingsRepository

    @Inject lateinit var autoLockRepository: AutoLockRepository

    @Inject lateinit var refreshLastUserInteractionUseCase: RefreshLastUserInteractionUseCase

    private val inactivityDelay = AutoLockInactivityDelay.THIRTY_SECONDS.value

    @Test
    fun get_inactivity_remaining_time_interaction_past_test(): TestResult = runTest {
        securitySettingsRepository.setAutoLockInactivityDelay(firstSafeId, inactivityDelay)
        autoLockRepository.lastUserInteractionInstant =
            Instant.now(testClock).minusSeconds(inactivityDelay.inWholeSeconds - 10)

        assertEquals(
            expected = 10.seconds,
            actual = autoLockInactivityGetRemainingTimeUseCase.app(),
        )
    }

    @Test
    fun get_inactivity_remaining_time_interaction_now_test(): TestResult = runTest {
        securitySettingsRepository.setAutoLockInactivityDelay(firstSafeId, inactivityDelay)
        autoLockRepository.lastUserInteractionInstant =
            Instant.now(testClock).minusSeconds(10)
        refreshLastUserInteractionUseCase()

        assertEquals(
            expected = inactivityDelay,
            actual = autoLockInactivityGetRemainingTimeUseCase.app(),
        )
    }

    @Test
    fun get_inactivity_remaining_time_interaction_over_delay_test(): TestResult = runTest {
        securitySettingsRepository.setAutoLockInactivityDelay(firstSafeId, inactivityDelay)
        autoLockRepository.lastUserInteractionInstant =
            Instant.now(testClock).minusSeconds(inactivityDelay.inWholeSeconds + 10)

        assertEquals(
            expected = Duration.ZERO,
            actual = autoLockInactivityGetRemainingTimeUseCase.app(),
        )
    }
}
