package studio.lunabee.onesafe.usecase.autolock

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.coVerify
import io.mockk.spyk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.onesafe.domain.LoadFileCancelAllUseCase
import studio.lunabee.onesafe.domain.repository.ClipboardRepository
import studio.lunabee.onesafe.domain.repository.MainCryptoRepository
import studio.lunabee.onesafe.domain.usecase.authentication.IsSafeReadyUseCase
import studio.lunabee.onesafe.domain.usecase.autolock.LockAppUseCase
import studio.lunabee.onesafe.domain.usecase.clipboard.ClipboardScheduleClearUseCase
import studio.lunabee.onesafe.domain.usecase.clipboard.ClipboardShouldClearUseCase
import studio.lunabee.onesafe.domain.usecase.settings.SetSecuritySettingUseCase
import studio.lunabee.onesafe.feature.clipboard.model.ClipboardClearDelay
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltUnitTest
import studio.lunabee.onesafe.test.firstSafeId
import javax.inject.Inject
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@HiltAndroidTest
class LockAppUseCaseTest : OSHiltUnitTest() {
    @get:Rule
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.Home()

    @Inject lateinit var isSafeReadyUseCase: IsSafeReadyUseCase

    @Inject lateinit var setSecuritySettingUseCase: SetSecuritySettingUseCase

    @Inject lateinit var clipboardRepository: ClipboardRepository

    @Inject lateinit var mainCryptoRepository: MainCryptoRepository

    @Inject lateinit var loadFileCancelAllUseCase: LoadFileCancelAllUseCase

    @Inject lateinit var clipboardShouldClearUseCase: ClipboardShouldClearUseCase

    @Inject lateinit var clipboardScheduleClearUseCase: ClipboardScheduleClearUseCase

    private val spyClipboardScheduleClearUseCase: ClipboardScheduleClearUseCase by lazy { spyk(clipboardScheduleClearUseCase) }

    val useCase: LockAppUseCase by lazy {
        LockAppUseCase(
            mainCryptoRepository,
            loadFileCancelAllUseCase,
            safeRepository,
            clipboardShouldClearUseCase,
            spyClipboardScheduleClearUseCase,
        )
    }

    @Test
    fun lock_app_test() {
        runTest {
            assertTrue(isSafeReadyUseCase.flow().first())
            useCase(false)
            assertFalse(isSafeReadyUseCase.flow().first())
            coVerify(exactly = 0) { spyClipboardScheduleClearUseCase.setup(any(), any()) }
        }
    }

    @Test
    fun lock_app_and_clear_clipboard_test(): TestResult = runTest {
        val clearDelay = ClipboardClearDelay.TEN_SECONDS.value
        setSecuritySettingUseCase.setClipboardClearDelay(clearDelay, firstSafeId)
        clipboardRepository.hasCopiedValue = true
        assertTrue(isSafeReadyUseCase.flow().first())
        useCase(true)
        assertFalse(isSafeReadyUseCase.flow().first())
        coVerify(exactly = 1) { spyClipboardScheduleClearUseCase.setup(clearDelay, firstSafeId) }
    }
}
