package studio.lunabee.onesafe.usecase.autolock

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.onesafe.commonui.settings.AutoLockInactivityDelay
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.usecase.autolock.AutoLockInactivityGetRemainingTimeUseCase
import studio.lunabee.onesafe.domain.usecase.autolock.AutoLockInactivityUseCase
import studio.lunabee.onesafe.domain.usecase.autolock.LockAppUseCase
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.MockAutoLockInactivityGetRemainingTimeUseCase
import studio.lunabee.onesafe.test.OSHiltUnitTest
import studio.lunabee.onesafe.test.firstSafeId
import javax.inject.Inject
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@HiltAndroidTest
class AutoLockInactivityUseCaseTest : OSHiltUnitTest() {
    @get:Rule
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.Home()

    @Inject lateinit var autoLockInactivityGetRemainingTimeUseCase: AutoLockInactivityGetRemainingTimeUseCase

    private val inactivityDelay = AutoLockInactivityDelay.THIRTY_SECONDS

    @Test
    fun not_connected_test(): TestResult = runTest {
        val mockLockAppUseCase = mockk<LockAppUseCase> {
            coEvery { this@mockk.invoke(any()) } returns Unit
        }
        val spyAutoLockInactivityGetRemainingTimeUseCase = spyk(autoLockInactivityGetRemainingTimeUseCase)
        val autoLockInactivityUseCase = spyk(
            AutoLockInactivityUseCase(
                lockAppUseCase = mockLockAppUseCase,
                securitySettingsRepository = mockk {
                    every { this@mockk.autoLockInactivityDelayFlow(firstSafeId) } returns flowOf(Duration.ZERO)
                },
                autoLockInactivityGetRemainingTimeUseCase = spyAutoLockInactivityGetRemainingTimeUseCase,
                isSafeReadyUseCase = mockk {
                    every { this@mockk.flow() } returns flowOf(false)
                },
                safeRepository = safeRepository,
            ),
        )

        autoLockInactivityUseCase.app()

        coVerify(exactly = 0) { spyAutoLockInactivityGetRemainingTimeUseCase.app(firstSafeId) }
        coVerify(exactly = 0) { mockLockAppUseCase.invoke(any()) }
    }

    @Test
    fun connected_test(): TestResult = runTest {
        val mockLockAppUseCase = mockk<LockAppUseCase> {
            coEvery { this@mockk.invoke(any()) } returns Unit
        }
        val autoLockInactivityUseCase = spyk(
            AutoLockInactivityUseCase(
                lockAppUseCase = mockLockAppUseCase,
                securitySettingsRepository = mockk {
                    every { this@mockk.autoLockInactivityDelayFlow(firstSafeId) } returns flowOf(Duration.ZERO)
                },
                autoLockInactivityGetRemainingTimeUseCase = autoLockInactivityGetRemainingTimeUseCase,
                isSafeReadyUseCase = mockk {
                    every { this@mockk.flow() } returns flowOf(true)
                },
                safeRepository = safeRepository,
            ),
        )

        autoLockInactivityUseCase.app()

        coVerify(exactly = 1) { mockLockAppUseCase.invoke(any()) }
    }

    @Test
    fun inactivity_delay_over(): TestResult = runTest {
        val lockAppMock = mockk<LockAppUseCase> {
            coEvery { this@mockk.invoke(any()) } returns Unit
        }
        var getRemainingTimeMockCalls = 0
        val getRemainingTimeMock = object : MockAutoLockInactivityGetRemainingTimeUseCase() {
            override suspend fun app(currentSafeId: SafeId?): Duration {
                getRemainingTimeMockCalls++
                return Duration.ZERO
            }
        }
        val autoLockInactivityUseCase = spyk(
            AutoLockInactivityUseCase(
                lockAppUseCase = lockAppMock,
                securitySettingsRepository = mockk {
                    every { this@mockk.autoLockInactivityDelayFlow(firstSafeId) } returns flowOf(inactivityDelay.value)
                },
                autoLockInactivityGetRemainingTimeUseCase = getRemainingTimeMock,
                isSafeReadyUseCase = mockk {
                    every { this@mockk.flow() } returns flowOf(true)
                },
                safeRepository = safeRepository,
            ),
        )

        autoLockInactivityUseCase.app()

        assertEquals(1, getRemainingTimeMockCalls)
        coVerify(exactly = 1) { lockAppMock.invoke(any()) }
    }

    @Test
    fun inactivity_flow(): TestResult = runTest {
        val lockAppMock = mockk<LockAppUseCase> {
            coEvery { this@mockk.invoke(any()) } returns Unit
        }
        val remainingTimeSequence = listOf(10.seconds, 2.seconds, Duration.ZERO, 30.seconds)
        val noTimeIndex = remainingTimeSequence.indexOf(Duration.ZERO)
        var getRemainingTimeMockIdx = 0
        val getRemainingTimeMock = object : MockAutoLockInactivityGetRemainingTimeUseCase() {
            override suspend fun app(currentSafeId: SafeId?): Duration {
                return remainingTimeSequence[getRemainingTimeMockIdx++]
            }
        }

        val autoLockInactivityUseCase = spyk(
            AutoLockInactivityUseCase(
                lockAppUseCase = lockAppMock,
                securitySettingsRepository = mockk {
                    every { this@mockk.autoLockInactivityDelayFlow(firstSafeId) } returns flowOf(inactivityDelay.value)
                },
                autoLockInactivityGetRemainingTimeUseCase = getRemainingTimeMock,
                isSafeReadyUseCase = mockk {
                    every { this@mockk.flow() } returns flowOf(true)
                },
                safeRepository = safeRepository,
            ),
        )

        autoLockInactivityUseCase.app()

        assertEquals(noTimeIndex + 1, getRemainingTimeMockIdx)
        coVerify(exactly = 1) { lockAppMock.invoke(any()) }
    }
}
