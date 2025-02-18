package studio.lunabee.onesafe.usecase.clipboard

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.test.platform.app.InstrumentationRegistry
import com.lunabee.lbextensions.lazyFast
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.onesafe.domain.usecase.clipboard.ClipboardCopyTextUseCase
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltUnitTest
import javax.inject.Inject
import kotlin.test.assertEquals

/**
 * Clipboard tests use [runComposeUiTest] to get a foreground activity which allow us to use the [ClipboardManager] on API >= 31
 */
@HiltAndroidTest
class ClipboardCopyTextUseCaseTest : OSHiltUnitTest() {
    @get:Rule
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)
    override val initialTestState: InitialTestState = InitialTestState.Home()

    private val context: Context by lazyFast {
        InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Inject
    lateinit var clipboardCopyTextUseCase: ClipboardCopyTextUseCase

    /**
     * Check [ClipboardCopyTextUseCase] actually add clip to the clipboard (without sensitive flag)
     */
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun clipboardCopyTextUseCase_test(): Unit = runComposeUiTest {
        runOnUiThread {
            doTest(false)
        }
    }

    /**
     * Check [ClipboardCopyTextUseCase] actually add clip to the clipboard (with sensitive flag)
     */
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun clipboardCopyTextUseCase_secured_test(): Unit = runComposeUiTest {
        runOnUiThread {
            doTest(true)
        }
    }

    private fun doTest(isSecured: Boolean) {
        val label = "label"
        val value = "value"
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        clipboardCopyTextUseCase(label, value, isSecured)

        assertEquals(1, clipboardManager.primaryClip?.itemCount)
        assertEquals(value, clipboardManager.primaryClip?.getItemAt(0)?.text)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            assertEquals(isSecured, clipboardManager.primaryClipDescription?.extras?.getBoolean(ClipDescription.EXTRA_IS_SENSITIVE))
        }
    }
}
