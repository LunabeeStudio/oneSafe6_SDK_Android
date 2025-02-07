package studio.lunabee.onesafe.usecase.autolock

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.onesafe.domain.repository.AutoLockRepository
import studio.lunabee.onesafe.domain.usecase.autolock.RefreshLastUserInteractionUseCase
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltUnitTest
import java.time.Instant
import javax.inject.Inject
import kotlin.test.assertEquals

@HiltAndroidTest
class RefreshLastUserInteractionUseCaseTest : OSHiltUnitTest() {
    @get:Rule
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.Home()

    @Inject lateinit var autoLockRepository: AutoLockRepository

    @Inject lateinit var refreshLastUserInteractionUseCase: RefreshLastUserInteractionUseCase

    @Test
    fun refresh_last_user_interaction_instant() {
        autoLockRepository.lastUserInteractionInstant = Instant.now(testClock).minusSeconds(10)
        refreshLastUserInteractionUseCase()
        assertEquals(Instant.now(testClock), autoLockRepository.lastUserInteractionInstant)
    }
}
