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
 * Created by Lunabee Studio / Date - 9/12/2024 - for the oneSafe6 SDK.
 * Last modified 12/09/2024 10:32
 */

package studio.lunabee.onesafe.usecase.autodestruction

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.onesafe.domain.repository.MainCryptoRepository
import studio.lunabee.onesafe.domain.usecase.autodestruction.DisabledAutoDestructionUseCase
import studio.lunabee.onesafe.domain.usecase.autodestruction.EnableAutoDestructionUseCase
import studio.lunabee.onesafe.domain.usecase.autodestruction.ExecuteAutoDestructionUseCase
import studio.lunabee.onesafe.domain.usecase.autodestruction.IsAutoDestructionEnabledUseCase
import studio.lunabee.onesafe.jvm.toByteArray
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltUnitTest
import studio.lunabee.onesafe.test.assertFailure
import javax.inject.Inject
import kotlin.test.assertFalse

@HiltAndroidTest
class AutoDestructionUseCasesTest : OSHiltUnitTest() {

    @get:Rule
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.Home()

    @Inject lateinit var enableAutoDestructionUseCase: EnableAutoDestructionUseCase

    @Inject lateinit var isAutoDestructionEnabledUseCase: IsAutoDestructionEnabledUseCase

    @Inject lateinit var disabledAutoDestructionUseCase: DisabledAutoDestructionUseCase

    @Inject lateinit var mainCryptoRepository: MainCryptoRepository

    @Inject lateinit var executeAutoDestructionUseCase: ExecuteAutoDestructionUseCase

    private val password = charArrayOf('b')
        get() = field.copyOf()

    @Test
    fun `Setup auto destruction test`(): TestResult = runTest {
        val currentSafeId = safeRepository.currentSafeId()
        assert(safeRepository.getSafeCrypto(currentSafeId)?.autoDestructionKey == null)
        assertFalse(isAutoDestructionEnabledUseCase())

        enableAutoDestructionUseCase(password)
        assert(safeRepository.getSafeCrypto(currentSafeId)?.autoDestructionKey != null)
        assert(isAutoDestructionEnabledUseCase())

        val currentCrypto = safeRepository.getSafeCrypto(currentSafeId)
        val derivedPassword = mainCryptoRepository.derivePassword(password = password, salt = currentCrypto!!.salt)
        assert(currentCrypto.autoDestructionKey!!.contentEquals(derivedPassword))

        disabledAutoDestructionUseCase()
        assert(safeRepository.getSafeCrypto(currentSafeId)?.autoDestructionKey == null)
        assertFalse(isAutoDestructionEnabledUseCase())
    }

    @Test
    fun `Login auto destruction test`(): TestResult = runTest {
        enableAutoDestructionUseCase(password)
        executeAutoDestructionUseCase.invoke(password.toByteArray())
        assertFailure(loginUseCase.invoke(password = charArrayOf('a')))
    }
}
