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
 * Created by Lunabee Studio / Date - 9/23/2024 - for the oneSafe6 SDK.
 * Last modified 9/23/24, 10:25 AM
 */

package studio.lunabee.onesafe.migration.utils

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.error.OSCryptoError
import studio.lunabee.onesafe.error.OSDomainError
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltUnitTest
import studio.lunabee.onesafe.test.assertFailure
import studio.lunabee.onesafe.test.assertSuccess
import studio.lunabee.onesafe.test.testUUIDs
import javax.inject.Inject
import kotlin.test.assertEquals

@HiltAndroidTest
class MigrationGetSafeCryptoUseCaseTest : OSHiltUnitTest() {
    @get:Rule
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)
    override val initialTestState: InitialTestState = InitialTestState.SignedOut

    @Inject
    lateinit var migrationGetSafeCryptoUseCase: MigrationGetSafeCryptoUseCase

    @Test
    fun no_safe_test(): TestResult = runTest {
        val result = migrationGetSafeCryptoUseCase.invoke(testPassword.toCharArray())
        assertFailure(result, OSDomainError.Code.SIGNIN_NOT_SIGNED_UP)
    }

    @Test
    fun no_safe_match_test(): TestResult = runTest {
        signup()
        val result = migrationGetSafeCryptoUseCase.invoke("no_match".toCharArray())
        assertFailure(result, OSCryptoError.Code.NO_SAFE_MATCH_KEY)
    }

    @Test
    fun success_test(): TestResult = runTest {
        signup(charArrayOf('a'), SafeId(testUUIDs[0]))
        signup(charArrayOf('b'), SafeId(testUUIDs[1]))

        migrationGetSafeCryptoUseCase.invoke(charArrayOf('a')).let { result ->
            val data = assertSuccess(result).successData
            assertEquals(testUUIDs[0], data.id.id)
        }
        migrationGetSafeCryptoUseCase.invoke(charArrayOf('b')).let { result ->
            val data = assertSuccess(result).successData
            assertEquals(testUUIDs[1], data.id.id)
            println(data)
        }
    }
}
