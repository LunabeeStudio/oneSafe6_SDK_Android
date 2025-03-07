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
 * Created by Lunabee Studio / Date - 7/2/2024 - for the oneSafe6 SDK.
 * Last modified 6/28/24, 4:50 PM
 */

package studio.lunabee.onesafe.migration

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.onesafe.domain.usecase.authentication.IsSignUpUseCase
import studio.lunabee.onesafe.error.OSCryptoError
import studio.lunabee.onesafe.error.OSDomainError
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltUnitTest
import studio.lunabee.onesafe.test.assertFailure
import studio.lunabee.onesafe.test.assertSuccess
import javax.inject.Inject
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

@HiltAndroidTest
class LoginAndMigrateUseCaseTest : OSHiltUnitTest() {

    @get:Rule
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)
    override val initialTestState: InitialTestState = InitialTestState.SignedOut

    @Inject lateinit var isSignUpUseCase: IsSignUpUseCase

    @Test
    fun login_logout_test(): TestResult = runTest {
        signup("password".toCharArray())
        assertTrue { isSignUpUseCase() }
        logout()
        assertSuccess(loginUseCase("password".toCharArray()))
        signOut()
        assertFalse { isSignUpUseCase() }
    }

    @Test
    fun login_wrong_password_test(): TestResult = runTest {
        signup("password".toCharArray())
        logout()
        val loginResult = loginUseCase("wrong_password".toCharArray())
        val error = assertFailure(loginResult).throwable
        assertIs<OSCryptoError>(error)
        assertEquals(OSCryptoError.Code.NO_SAFE_MATCH_KEY, error.code)
    }

    @Test
    fun login_not_signed_up_test(): TestResult = runTest {
        val result = loginUseCase(charArrayOf())
        assertFailure(result)
        val error = result.throwable
        assertIs<OSDomainError>(error)
        assertEquals(OSDomainError.Code.SIGNIN_NOT_SIGNED_UP, error.code)
    }

    @Test
    fun login_already_signed_in_test(): TestResult = runTest {
        signup("password".toCharArray())
        logout()
        assertSuccess(loginUseCase("password".toCharArray()))
        val error = assertFailure(loginUseCase("password".toCharArray())).throwable
        assertIs<OSCryptoError>(error)
        assertEquals(OSCryptoError.Code.MASTER_KEY_ALREADY_LOADED, error.code)
    }
}
