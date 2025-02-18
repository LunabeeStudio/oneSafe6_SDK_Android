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
 * Created by Lunabee Studio / Date - 9/13/2024 - for the oneSafe6 SDK.
 * Last modified 13/09/2024 09:17
 */

package studio.lunabee.onesafe.navigation.autodestruction

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.lunabee.lbcore.model.LBResult
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.spyk
import org.junit.Rule
import org.junit.Test
import studio.lunabee.compose.androidtest.extension.waitAndPrintRootToCacheDir
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.onesafe.OSMainActivityTest
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.di.Inject
import studio.lunabee.onesafe.domain.usecase.authentication.DeleteSafeUseCase
import studio.lunabee.onesafe.domain.usecase.autodestruction.EnableAutoDestructionUseCase
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.firstSafeId
import studio.lunabee.onesafe.ui.UiConstants

@HiltAndroidTest
class LoginAutoDestructionNavigationTest : OSMainActivityTest() {
    @get:Rule(order = 0)
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @Inject
    lateinit var enableAutoDestructionUseCase: EnableAutoDestructionUseCase

    override val initialTestState: InitialTestState = InitialTestState.SignedUp {
        enableAutoDestructionUseCase(password)
    }

    @BindValue
    val deleteSafeUseCase: DeleteSafeUseCase = spyk(mockk<DeleteSafeUseCase>()) {
        coEvery { this@spyk(any()) } returns LBResult.Success(Unit)
    }

    private val password: CharArray
        get() = charArrayOf('b')

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun login_auto_destruction_test(): Unit = invoke {
        hasTestTag(UiConstants.TestTag.Screen.Login)
            .waitAndPrintRootToCacheDir(printRule)
            .assertIsDisplayed()
        hasTestTag(UiConstants.TestTag.Item.LoginPasswordTextField)
            .waitUntilExactlyOneExists()
            .performTextInput(password.concatToString())
        hasTestTag(UiConstants.TestTag.Item.LoginButtonIcon)
            .waitAndPrintRootToCacheDir(useUnmergedTree = true, printRule = printRule)
            .performScrollTo()
            .performClick()
        hasText(getString(OSString.signInScreen_form_password_error))
            .waitAndPrintRootToCacheDir(useUnmergedTree = true, printRule = printRule, suffix = "_after_auto_destruction_password")
            .performScrollTo()
            .assertIsDisplayed()
        coVerify(exactly = 1) { deleteSafeUseCase(firstSafeId) }
    }
}
