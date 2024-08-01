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
 * Last modified 4/8/24, 9:41 AM
 */

package studio.lunabee.onesafe.test

import android.content.Context
import android.view.View
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.printToString
import androidx.fragment.app.FragmentActivity
import androidx.test.platform.app.InstrumentationRegistry
import co.touchlab.kermit.Logger
import com.lunabee.lbextensions.content.getQuantityString
import org.junit.Rule
import studio.lunabee.compose.androidtest.LbcAndroidTestConstants
import studio.lunabee.compose.androidtest.extension.waitUntilAtLeastOneExists
import studio.lunabee.compose.androidtest.rule.LbcPrintRule
import java.io.File

/**
 * Base class to wrap whole activity test
 */
@OptIn(ExperimentalTestApi::class)
abstract class OSActivityTest<A : FragmentActivity> : OSHiltTest() {

    protected val targetContext: Context = InstrumentationRegistry.getInstrumentation().targetContext

    /**
     * Activity to be used in the whole class. It will lead to crash if not initialized.
     */
    lateinit var activity: A

    /**
     * Handle keyboard visibility in your test. You should hide it to avoid flaky test.
     */
    lateinit var keyboardHelper: KeyboardHelper

    @get:Rule
    val printRule: LbcPrintRule = LbcPrintRule.internalStorage()

    context(ComposeUiTest)
    protected fun onFailure(e: Throwable) {
        val suffix = LbcAndroidTestConstants.FailureSuffix + "_${e.javaClass.simpleName}"
        printRule.printWholeScreen(suffix)
        val tree = isRoot()
            .waitUntilAtLeastOneExists(true)
            .onFirst()
            .printToString()
        Logger.withTag("ON_FAILURE").e(tree)
        val composeTreeFile = File("${printRule.basePath}_failing_compose_tree.txt")
        composeTreeFile.writeText(tree)
    }

    fun initKeyboardHelper() {
        keyboardHelper = KeyboardHelper(activity.findViewById<View>(android.R.id.content).rootView)
    }

    fun getString(@StringRes id: Int, vararg args: Any): String = targetContext.getString(id, *args)

    fun getQuantityString(@PluralsRes id: Int, quantity: Int, vararg args: Any): String = targetContext.getQuantityString(
        id,
        quantity,
        *args,
    )
}
