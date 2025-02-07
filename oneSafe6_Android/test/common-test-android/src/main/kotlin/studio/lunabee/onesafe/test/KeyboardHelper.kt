/*
 * Copyright (c) 2024 Lunabee Studio
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Created by Lunabee Studio / Date - 4/16/2024 - for the oneSafe6 SDK.
 * Last modified 4/16/24, 10:41 AM
 */

package studio.lunabee.onesafe.test

import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsAnimation
import androidx.annotation.RequiresApi
import androidx.compose.ui.test.ComposeTimeoutException
import studio.lunabee.compose.androidtest.rule.LbcPrintRule
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Helper methods for hiding and showing the keyboard in tests.
 *
 * https://github.com/androidx/androidx/blob/androidx-main/compose/foundation/foundation/src/androidInstrumentedTest/kotlin/androidx/compose/foundation/text/KeyboardHelper.kt
 */
class KeyboardHelper(
    private val view: View,
    private val timeout: Long = 20_000L,
) {
    /**
     * Blocks until the [timeout] or the keyboard's visibility matches [visible].
     * May be called from the test thread or the main thread.
     */
    fun waitForKeyboardVisibility(visible: Boolean, timeout: Long = this.timeout, printRule: LbcPrintRule? = null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                waitUntil(timeout) {
                    val keyboardVisible = view.rootWindowInsets?.isVisible(WindowInsets.Type.ime())
                    keyboardVisible == visible
                }
            } catch (e: ComposeTimeoutException) {
                printRule?.printWholeScreen("_visible_$visible")
                throw e
            }
        }
    }

    private fun waitUntil(timeout: Long, condition: () -> Boolean) {
        view.waitForWindowInsetsUntil(timeout, condition)
    }

    private fun View.waitForWindowInsetsUntil(timeoutMillis: Long, condition: () -> Boolean) {
        val latch = CountDownLatch(1)
        rootView.setOnApplyWindowInsetsListener { view, windowInsets ->
            if (condition()) {
                latch.countDown()
            }
            view.onApplyWindowInsets(windowInsets)
            windowInsets
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            rootView.setWindowInsetsAnimationCallback(
                InsetAnimationCallback {
                    if (condition()) {
                        latch.countDown()
                    }
                },
            )
        }

        // if condition already met return
        if (condition()) return

        // else wait for condition to be met
        val conditionMet = latch.await(timeoutMillis, TimeUnit.MILLISECONDS)
        assert(conditionMet) { "After waiting for $timeoutMillis ms, window insets condition is still false" }
    }
}

@RequiresApi(Build.VERSION_CODES.R)
private class InsetAnimationCallback(val block: () -> Unit) :
    WindowInsetsAnimation.Callback(DISPATCH_MODE_CONTINUE_ON_SUBTREE) {

    override fun onProgress(
        insets: WindowInsets,
        runningAnimations: MutableList<WindowInsetsAnimation>,
    ) = insets

    override fun onEnd(animation: WindowInsetsAnimation) {
        block()
        super.onEnd(animation)
    }
}
