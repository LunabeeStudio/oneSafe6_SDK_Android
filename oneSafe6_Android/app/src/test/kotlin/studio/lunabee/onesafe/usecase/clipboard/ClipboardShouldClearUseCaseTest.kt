package studio.lunabee.onesafe.usecase.clipboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.test.platform.app.InstrumentationRegistry
import com.lunabee.lbextensions.lazyFast
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.usecase.clipboard.ClipboardCopyTextUseCase
import studio.lunabee.onesafe.domain.usecase.clipboard.ClipboardShouldClearUseCase
import studio.lunabee.onesafe.domain.usecase.settings.SetSecuritySettingUseCase
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltUnitTest
import studio.lunabee.onesafe.test.firstSafeId
import studio.lunabee.onesafe.test.testUUIDs
import javax.inject.Inject
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Clipboard tests use [runComposeUiTest] to get a foreground activity which allow us to use the [ClipboardManager] on API >= 31
 */
@HiltAndroidTest
class ClipboardShouldClearUseCaseTest : OSHiltUnitTest() {
    @get:Rule
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)
    override val initialTestState: InitialTestState = InitialTestState.Home()

    @Inject
    lateinit var clipboardShouldClearUseCase: ClipboardShouldClearUseCase

    @Inject
    lateinit var clipboardCopyTextUseCase: ClipboardCopyTextUseCase

    @Inject
    lateinit var setSecuritySettingsUseCase: SetSecuritySettingUseCase

    private val context: Context by lazyFast {
        InstrumentationRegistry.getInstrumentation().targetContext
    }

    /**
     * Check [ClipboardShouldClearUseCase] returns null without clip set
     */
    @Test
    fun without_clip_test() {
        val actual = runBlocking { clipboardShouldClearUseCase(firstSafeId) }
        assertNull(actual)
    }

    /**
     * Check [ClipboardShouldClearUseCase] returns the expected duration with a clip from oneSafe set
     */
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun with_oneSafe_clip_test(): Unit = runComposeUiTest {
        runOnUiThread {
            val expected = 123.seconds
            runBlocking {
                setSecuritySettingsUseCase.setClipboardClearDelay(expected)
            }

            clipboardCopyTextUseCase("label", "value", false)

            val actual = runBlocking { clipboardShouldClearUseCase(firstSafeId) }
            assertEquals(expected, actual)
        }
    }

    /**
     * Check [ClipboardShouldClearUseCase] returns the expected duration with a clip from oneSafe set
     */
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun with_oneSafe_clip_from_other_safe_test(): Unit = runComposeUiTest {
        runOnUiThread {
            logout()
            val pwdB = charArrayOf('b')
            val safeIdB = SafeId(testUUIDs[1])
            runBlocking {
                signup(pwdB, safeIdB)
                login(pwdB)
            }

            val expectedB = 123.seconds
            runBlocking {
                setSecuritySettingsUseCase.setClipboardClearDelay(Duration.INFINITE, firstSafeId)
                setSecuritySettingsUseCase.setClipboardClearDelay(expectedB, safeIdB)
            }

            clipboardCopyTextUseCase("label", "value", false)

            runBlocking {
                val actual = clipboardShouldClearUseCase(safeIdB)
                assertEquals(expectedB, actual)
            }

            runBlocking {
                val actual = clipboardShouldClearUseCase(firstSafeId)
                assertNull(actual)
            }
        }
    }

    /**
     * Check [ClipboardShouldClearUseCase] returns null with a clip set by another app / system
     */
    @Test
    fun with_non_oneSafe_clip_test() {
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("label", "value")
        clipboardManager.setPrimaryClip(clipData)

        val actual = runBlocking { clipboardShouldClearUseCase(firstSafeId) }
        assertNull(actual)
    }
}
