package studio.lunabee.onesafe.common.utils

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Test
import studio.lunabee.onesafe.commonui.utils.CloseableMainCoroutineScope
import studio.lunabee.onesafe.test.OSUiThreadTest
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CloseableMainCoroutineScopeTest : OSUiThreadTest() {

    private val closeableCoroutineScope by lazy {
        CloseableMainCoroutineScope()
    }

    @Test
    fun no_close_test(): TestResult = runTest {
        var hasRun = false
        closeableCoroutineScope.coroutineScope.launch {
            delay(5_000)
            hasRun = true
        }.join()

        assertTrue(hasRun)
    }

    @Test
    fun canceled_on_close_test(): TestResult = runTest {
        var hasRun = false
        val job = closeableCoroutineScope.coroutineScope.launch {
            delay(5_000)
            hasRun = true
        }
        closeableCoroutineScope.close()
        job.join()

        assertFalse(hasRun)
    }
}
