package studio.lunabee.onesafe.feature.autolock

import androidx.lifecycle.Lifecycle
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Rule
import org.junit.Test
import studio.lunabee.onesafe.domain.manager.IsAppBlockedUseCase
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.repository.SecuritySettingsRepository
import studio.lunabee.onesafe.domain.usecase.authentication.IsSafeReadyUseCase
import studio.lunabee.onesafe.domain.usecase.autolock.AutoLockInactivityUseCase
import studio.lunabee.onesafe.domain.usecase.autolock.RefreshLastUserInteractionUseCase
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltUnitTest
import studio.lunabee.onesafe.test.assertThrows
import studio.lunabee.onesafe.test.firstSafeId
import studio.lunabee.onesafe.test.testUUIDs
import javax.inject.Inject
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.toJavaDuration

@HiltAndroidTest
class AndroidAutoLockInactivityManagerTest : OSHiltUnitTest() {
    @get:Rule
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.Home()

    @Inject
    lateinit var autoLockInactivityUseCase: AutoLockInactivityUseCase

    @Inject
    lateinit var refreshLastUserInteractionUseCase: RefreshLastUserInteractionUseCase

    @Inject
    lateinit var securitySettingsRepository: SecuritySettingsRepository

    @Inject
    lateinit var isSafeReadyUseCase: IsSafeReadyUseCase

    private val isBlocking: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val isAppBlockedUseCase: IsAppBlockedUseCase = object : IsAppBlockedUseCase {
        override fun flow(): Flow<Boolean> = isBlocking.asStateFlow()
        override suspend operator fun invoke(): Boolean = isBlocking.value
    }

    /**
     * Test the main use case of [AndroidAutoLockInactivityManager] (unlocked -> wait delay -> lock)
     */
    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    @Test
    fun lock_after_delay_test(): TestResult = runTest {
        val manager: AndroidAutoLockInactivityManager = androidAutoLockInactivityManager()

        // Set the inactive delay param
        val lockDelay = 2.milliseconds
        securitySettingsRepository.setAutoLockInactivityDelay(firstSafeId, lockDelay)

        // Trigger ON_RESUME state to get the inactive delay runs
        manager.onStateChanged(mockk(), Lifecycle.Event.ON_RESUME)
        advanceUntilIdle()

        // Assert not locked yet
        assertTrue(cryptoRepository.isCryptoDataInMemoryFlow().first())

        // Advance time by the inactive delay to simulate the deadline being exceeded
        testClock.add(lockDelay.toJavaDuration())

        withContext(Dispatchers.Default.limitedParallelism(1)) { // allow timeout in runTest
            cryptoRepository.isCryptoDataInMemoryFlow().filter { !it }.timeout(200.milliseconds).first()
        }
    }

    /**
     * unlocked -> wait delay partially -> interact to reset the delay -> wait more -> still unlocked
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun do_not_lock_on_interaction_test(): TestResult = runTest {
        val manager: AndroidAutoLockInactivityManager = androidAutoLockInactivityManager()

        // Set the inactive delay param
        val lockDelay = 5.milliseconds
        securitySettingsRepository.setAutoLockInactivityDelay(firstSafeId, lockDelay)

        // Trigger ON_RESUME state to get the inactive delay runs
        manager.onStateChanged(mockk(), Lifecycle.Event.ON_RESUME)

        // Advance time by the inactive delay
        testClock.add(lockDelay.toJavaDuration())

        // Interact to reset the delay
        refreshLastUserInteractionUseCase()

        advanceTimeBy(lockDelay)
        testClock.add((lockDelay / 2).toJavaDuration())
        advanceTimeBy(lockDelay)
        assertTrue(cryptoRepository.isCryptoDataInMemoryFlow().first())

        // Advance time to let the test finish (avoid infinite loop due to manual time handling)
        testClock.add(lockDelay.toJavaDuration())
    }

    /**
     * unlocked -> wait delay partially -> interact to reset the delay -> wait delay -> lock)
     */
    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    @Test
    fun lock_after_delay_with_interaction_test(): TestResult = runTest {
        val manager: AndroidAutoLockInactivityManager = androidAutoLockInactivityManager()

        // Set the inactive delay param
        val lockDelay = 2.milliseconds
        securitySettingsRepository.setAutoLockInactivityDelay(firstSafeId, lockDelay)

        // Trigger ON_RESUME state to get the inactive delay runs
        manager.onStateChanged(mockk(), Lifecycle.Event.ON_RESUME)
        // Advance time by the inactive delay to simulate the deadline being exceeded
        testClock.add(lockDelay.toJavaDuration())

        // Relaunch delay & simulate time
        refreshLastUserInteractionUseCase()

        // Re-advance time
        testClock.add(lockDelay.toJavaDuration())

        // Assert still unlocked
        advanceTimeBy(1.milliseconds)
        assertTrue(cryptoRepository.isCryptoDataInMemoryFlow().first())

        // Let the coroutine run the inactive delay
        advanceUntilIdle()
        withContext(Dispatchers.Default.limitedParallelism(1)) { // allow timeout in runTest
            cryptoRepository.isCryptoDataInMemoryFlow().filter { !it }.timeout(200.milliseconds).first()
        }
    }

    /**
     * Postpone lock while app is loading
     */
    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    @Test
    fun lock_after_delay_with_loading_test(): TestResult = runTest {
        val manager: AndroidAutoLockInactivityManager = androidAutoLockInactivityManager()

        // Set the inactive delay param
        val lockDelay = 2.milliseconds
        securitySettingsRepository.setAutoLockInactivityDelay(firstSafeId, lockDelay)

        // Emulate blocking loading
        isBlocking.value = true

        // Trigger ON_RESUME state to get the inactive delay runs
        manager.onStateChanged(mockk(), Lifecycle.Event.ON_RESUME)

        // Advance time by the inactive delay to simulate the deadline being exceeded
        testClock.add(lockDelay.toJavaDuration())

        assertThrows<TimeoutCancellationException> {
            withContext(Dispatchers.Default.limitedParallelism(1)) { // allow timeout in runTest
                cryptoRepository.isCryptoDataInMemoryFlow().filter { !it }.timeout(200.milliseconds).first()
            }
        }

        // Release blocking loading
        isBlocking.value = false

        // Assert not locked yet
        assertTrue(cryptoRepository.isCryptoDataInMemoryFlow().first())

        // Advance time by the inactive delay to simulate the deadline being exceeded
        testClock.add(lockDelay.toJavaDuration())

        withContext(Dispatchers.Default.limitedParallelism(1)) { // allow timeout in runTest
            cryptoRepository.isCryptoDataInMemoryFlow().filter { !it }.timeout(200.milliseconds).first()
        }
    }

    /**
     * unlocked -> wait delay, lock -> open another safe -> wait a different delay -> lock)
     */
    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    @Test
    fun lock_then_open_another_safe_test(): TestResult = runTest {
        val manager: AndroidAutoLockInactivityManager = androidAutoLockInactivityManager()

        val secondSafeId = SafeId(testUUIDs[1])
        signup("b".toCharArray(), secondSafeId)
        // Set the inactive delay param for second safe
        val lockDelay2 = 100.milliseconds
        securitySettingsRepository.setAutoLockInactivityDelay(secondSafeId, lockDelay2)

        logout()
        login()

        // Set the inactive delay param for first safe
        val lockDelay = 2.milliseconds
        securitySettingsRepository.setAutoLockInactivityDelay(firstSafeId, lockDelay)

        // Trigger ON_RESUME state to get the inactive delay runs
        manager.onStateChanged(mockk(), Lifecycle.Event.ON_RESUME)

        // Advance time by the inactive delay to simulate the deadline being exceeded
        testClock.add(lockDelay.toJavaDuration())

        withContext(Dispatchers.Default.limitedParallelism(1)) { // allow timeout in runTest
            cryptoRepository.isCryptoDataInMemoryFlow().filter { !it }.timeout(50.milliseconds).first()
        }

        loginUseCase("b".toCharArray())
        testClock.add(lockDelay.toJavaDuration())

        assertThrows<TimeoutCancellationException> {
            withContext(Dispatchers.Default.limitedParallelism(1)) { // allow timeout in runTest
                cryptoRepository.isCryptoDataInMemoryFlow().filter { !it }.timeout(50.milliseconds).first()
            }
        }

        // Consume time to reach second delay
        testClock.add((lockDelay2 - lockDelay).toJavaDuration())
        withContext(Dispatchers.Default.limitedParallelism(1)) { // allow timeout in runTest
            cryptoRepository.isCryptoDataInMemoryFlow().filter { !it }.timeout(200.milliseconds).first()
        }
    }

    /**
     * Get an [AndroidAutoLockInactivityManager] running with the test scheduler
     */
    private fun TestScope.androidAutoLockInactivityManager(): AndroidAutoLockInactivityManager {
        return AndroidAutoLockInactivityManager(
            autoLockInactivityUseCase,
            refreshLastUserInteractionUseCase,
            backgroundScope,
            isSafeReadyUseCase,
            isAppBlockedUseCase,
        )
    }
}
