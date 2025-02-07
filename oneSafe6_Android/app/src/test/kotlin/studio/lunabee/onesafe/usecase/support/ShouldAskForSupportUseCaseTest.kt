package studio.lunabee.onesafe.usecase.support

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import studio.lunabee.onesafe.domain.repository.SupportOSRepository
import studio.lunabee.onesafe.domain.usecase.support.ShouldAskForSupportUseCase
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltUnitTest
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import javax.inject.Inject
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@HiltAndroidTest
class ShouldAskForSupportUseCaseTest : OSHiltUnitTest() {

    @get:Rule override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.Home()

    @Inject lateinit var shouldAskForSupportUseCase: ShouldAskForSupportUseCase

    @Inject lateinit var supportOSRepository: SupportOSRepository

    @Before
    fun setup() {
        runTest {
            supportOSRepository.resetAppVisit()
        }
    }

    /**
     * Test that if the user has never open the app, we will never ask to support oneSafe.
     */
    @Test
    fun has_never_open_the_app_test() {
        runTest {
            assertFalse(shouldAskForSupportUseCase())
        }
    }

    /**
     * Test that if the user open the app a sufficient number of time, and that he never interacted with the UI we ask to support oneSafe.
     */
    @Test
    fun min_number_has_been_reached_test() {
        runTest {
            supportOSRepository.setAppVisit(ShouldAskForSupportUseCase.CountToAskForSupport)
        }
        runTest {
            assertTrue(shouldAskForSupportUseCase())
        }
    }

    /**
     * Test that if the user dismiss the UI just now and even if he open the app a sufficient number of time we do not ask for
     * support.
     */
    @Test
    fun test_has_dismiss_now_test() {
        runTest {
            supportOSRepository.setDismissInstant(Instant.now())
            assertFalse(shouldAskForSupportUseCase())
            supportOSRepository.setAppVisit(ShouldAskForSupportUseCase.CountToAskForSupport)
            assertFalse(shouldAskForSupportUseCase())
        }
    }

    /**
     * Test that if the user dismiss the UI some times ago and even if he opens the app a sufficient number of time we do
     * not ask for support.
     */
    @Test
    fun test_has_dismiss_not_so_long_ago_test() {
        runTest {
            supportOSRepository.setDismissInstant(getInstantAgo(ShouldAskForSupportUseCase.BetweenDismissMonthDelay - 1))
            assertFalse(shouldAskForSupportUseCase())
            supportOSRepository.setAppVisit(ShouldAskForSupportUseCase.CountToAskForSupport)
            assertFalse(shouldAskForSupportUseCase())
        }
    }

    /**
     * Test that if the user dismiss the UI a long time ago and  if he open the app a sufficient number of time we do
     * ask for support.
     */
    @Test
    fun test_has_dismiss_long_ago_test() {
        runTest {
            supportOSRepository.setDismissInstant(getInstantAgo(ShouldAskForSupportUseCase.BetweenDismissMonthDelay, 1))
            assertFalse(shouldAskForSupportUseCase())
            supportOSRepository.setAppVisit(ShouldAskForSupportUseCase.CountToAskForSupport)
            assertTrue(shouldAskForSupportUseCase())
        }
    }

    /**
     * Test that if the user dismiss the UI a long time ago and has not visited the app again, we do not ask for support.
     */
    @Test
    fun test_has_dismiss_long_ago_but_not_visited_enough_test() {
        runTest {
            supportOSRepository.setDismissInstant(getInstantAgo(ShouldAskForSupportUseCase.BetweenDismissMonthDelay, 1))
            assertFalse(shouldAskForSupportUseCase())
        }
    }

    /**
     * Test that if the user rates the app just now and even if he open the app a sufficient number of time we do not ask for
     * support.
     */
    @Test
    fun test_has_rated_now_test() {
        runTest {
            supportOSRepository.setRatingInstant(Instant.now())
            assertFalse(shouldAskForSupportUseCase())
            supportOSRepository.setAppVisit(ShouldAskForSupportUseCase.CountToAskForSupport)
            assertFalse(shouldAskForSupportUseCase())
        }
    }

    /**
     * Test that if the user rated the app some times ago and even if he opens the app a sufficient number of time we do
     * not ask for support.
     */
    @Test
    fun test_has_rated_not_so_long_ago_test() {
        runTest {
            supportOSRepository.setDismissInstant(getInstantAgo(ShouldAskForSupportUseCase.BetweenRatingMonthDelay - 1, -1))
            assertFalse(shouldAskForSupportUseCase())
            supportOSRepository.setAppVisit(ShouldAskForSupportUseCase.CountToAskForSupport)
            assertFalse(shouldAskForSupportUseCase())
        }
    }

    /**
     * Test that if the user rated the app a long time ago and  if he open the app a sufficient number of time we do
     * ask for support.
     */
    @Test
    fun test_rated_long_ago_test() {
        runTest {
            supportOSRepository.setDismissInstant(getInstantAgo(ShouldAskForSupportUseCase.BetweenRatingMonthDelay, 1))
            assertFalse(shouldAskForSupportUseCase())
            supportOSRepository.setAppVisit(ShouldAskForSupportUseCase.CountToAskForSupport)
            assertTrue(shouldAskForSupportUseCase())
        }
    }

    /**
     * Test that if the rated the app a long time ago and has not visited the app again, we do not ask for support.
     */
    @Test
    fun test_rated_long_ago_but_not_visited_enough_test() {
        runTest {
            supportOSRepository.setRatingInstant(getInstantAgo(ShouldAskForSupportUseCase.BetweenDismissMonthDelay, 1))
            assertFalse(shouldAskForSupportUseCase())
        }
    }

    private fun getInstantAgo(monthsAgo: Long, daysAgo: Long = 0): Instant =
        LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault())
            .minusMonths(monthsAgo)
            .minusDays(daysAgo)
            .atZone(ZoneOffset.UTC)
            .toInstant()
}
