/*
 * Copyright (c) 2023-2023 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 4/7/2023 - for the oneSafe6 SDK.
 * Last modified 4/7/23, 12:30 AM
 */

package studio.lunabee.onesafe.macrobenchmark

import android.content.Context
import android.graphics.Point
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import com.lunabee.lblogger.LBLogger
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.ui.UiConstants
import java.io.File
import java.time.LocalTime

private val logger = LBLogger.get<HomeBenchmark>()

@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
class HomeBenchmark {
    @get:Rule
    val benchmarkRule: MacrobenchmarkRule = MacrobenchmarkRule()

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun home_scroll_benchmark(): Unit = benchmarkRule.measureRepeated(
        packageName = "studio.lunabee.onesafe.dev",
        metrics = listOf(FrameTimingMetric()),
        startupMode = StartupMode.WARM,
        iterations = 5,
        setupBlock = setupBenchmark(),
    ) {
        logger.d("Run $iteration")
        val itemGrid = device.findObject(By.res(UiConstants.TestTag.Item.HomeItemGrid))!!
        itemGrid.setGestureMargin(device.displayWidth / 5)

        // Use swipe instead of fling to avoid weird timeout which slow down the benchmark
        while (device.findObject(By.text("Item 99")) == null) {
            itemGrid.swipe(Direction.UP, 0.8f)
            logger.d("Scroll down at ${LocalTime.now()}")
        }
        while (device.findObject(By.text("Item 1")) == null) {
            itemGrid.swipe(Direction.DOWN, 0.8f)
            logger.d("Scroll up at ${LocalTime.now()}")
        }
    }

    private fun setupBenchmark(): MacrobenchmarkScope.() -> Unit = {
        logger.d("Setup $iteration")

        // Start activity
        startActivityAndWait()

        if (!device.hasObject(By.text("Item 0"))) {
            val skipButton = device.findObject(By.text(context.getString(OSString.common_skip)))
            val loginScreen = device.findObject(By.res(UiConstants.TestTag.Screen.Login))

            if (skipButton != null) {
                // Open debug menu (swipe outside of pager area)
                skipButton.drag(Point(device.displayWidth, skipButton.visibleCenter.y))
                runBlocking { delay(300) } // wait the drawer anim

                // Generate items
                device.findObject(By.res("BenchmarkButton"))!!.longClick()
                device.wait(Until.gone(By.textContains("Debug menu")), 60_000) // wait drawer close (i.e items generated)
            } else if (loginScreen != null) { // in case of Cold startupMode
                var loginBtn = device.wait(Until.findObject(By.res(UiConstants.TestTag.Item.LoginButtonIcon)), 2_000)
                if (loginBtn == null) {
                    // Scroll because of ime
                    loginScreen.setGestureMargins(
                        device.displayWidth / 5,
                        device.displayHeight / 8,
                        device.displayWidth / 5,
                        device.displayHeight / 2,
                    )
                    loginScreen.fling(Direction.DOWN)
                    loginBtn = device.findObject(By.res(UiConstants.TestTag.Item.LoginButtonIcon))!!
                }
                loginBtn.click()
            }

            // Wait the home is loaded
            device.wait(Until.hasObject(By.text("Item 0")), 10_000)!!
        }
    }

    /**
     * Dump windows hierarchy to log
     */
    @Suppress("unused")
    private fun MacrobenchmarkScope.printWindowHierarchy() {
        val dumpFile = File(context.cacheDir, "dump.xml")
        device.dumpWindowHierarchy(dumpFile)
        logger.d("Setup ${dumpFile.readText()}")
        dumpFile.delete()
    }
}
