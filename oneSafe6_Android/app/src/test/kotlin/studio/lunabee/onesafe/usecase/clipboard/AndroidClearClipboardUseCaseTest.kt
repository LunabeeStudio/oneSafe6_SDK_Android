package studio.lunabee.onesafe.usecase.clipboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.test.platform.app.InstrumentationRegistry
import com.lunabee.lbextensions.lazyFast
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import studio.lunabee.onesafe.domain.usecase.clipboard.ClipboardClearUseCase
import studio.lunabee.onesafe.domain.usecase.clipboard.ClipboardCopyTextUseCase
import studio.lunabee.onesafe.feature.clipboard.AndroidClearClipboardUseCase
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltUnitTest
import studio.lunabee.onesafe.test.firstSafeId
import javax.inject.Inject
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

/**
 * Clipboard tests use [runComposeUiTest] to get a foreground activity which allow us to use the [ClipboardManager] on API >= 31
 */
@HiltAndroidTest
class AndroidClearClipboardUseCaseTest : OSHiltUnitTest() {
    @get:Rule
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)
    override val initialTestState: InitialTestState = InitialTestState.Home()

    private val context: Context by lazyFast {
        InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Inject
    lateinit var clipboardCopyTextUseCase: ClipboardCopyTextUseCase

    @Inject
    lateinit var clipboardClearUseCase: AndroidClearClipboardUseCase

    /**
     * Check [ClipboardClearUseCase] clear clip added from oneSafe
     */
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun clear_oneSafe_clip_test(): Unit = runComposeUiTest {
        runOnUiThread {
            val illegalValue = "value"
            clipboardCopyTextUseCase("label", illegalValue, false)
            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

            runBlocking { clipboardClearUseCase(firstSafeId) }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                assertNull(clipboardManager.primaryClip)
            } else {
                assertNotEquals(illegalValue, clipboardManager.primaryClip?.getItemAt(0)?.text)
            }
        }
    }

    /**
     * Check [ClipboardClearUseCase] does not clear clip added from other app / system
     */
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun dont_clear_non_oneSafe_clip_test(): Unit = runComposeUiTest {
        runOnUiThread {
            val label = "label"
            val value = "value"
            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText(label, value)
            clipboardManager.setPrimaryClip(clipData)

            runBlocking { clipboardClearUseCase(firstSafeId) }

            assertEquals(label, clipboardManager.primaryClip?.description?.label)
            assertEquals(value, clipboardManager.primaryClip?.getItemAt(0)?.text)
        }
    }
}
