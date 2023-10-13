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

package studio.lunabee.onesafe.test

import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import dagger.hilt.android.testing.HiltAndroidRule
import io.mockk.unmockkAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import studio.lunabee.onesafe.OSAppSettings
import studio.lunabee.onesafe.domain.repository.MainCryptoRepository
import studio.lunabee.onesafe.domain.repository.PersistenceManager
import studio.lunabee.onesafe.domain.usecase.authentication.LocalSignInUseCase
import studio.lunabee.onesafe.domain.usecase.autolock.LockAppUseCase
import studio.lunabee.onesafe.domain.usecase.onboarding.CreateMasterKeyUseCase
import studio.lunabee.onesafe.domain.usecase.onboarding.FinishOnboardingUseCase
import studio.lunabee.onesafe.migration.MigrationConstant
import studio.lunabee.onesafe.storage.MainDatabase
import javax.inject.Inject

/**
 * This class should be use for test that needs Hilt injection but no UI.
 *
 * Example:
 * ```
 * class MyHiltTest: OSHiltTest() {
 *     @get:Rule(order = 0) // this is mandatory
 *     override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)
 *
 *     override val initialTestState: InitialTestState = InitialTestState.SignedIn {
 *         val item = createItemUseCase(itemName, null, false, null, null).data!!
 *         moveToBinItemUseCase(item)
 *     }
 *      @Test
 *      fun my_hilt_test() {
 *          runTest {
 *             val result = suspendableUseCase()
 *             assertSuccess(result)
 *         }
 *      }
 * }
 * ```
 */
abstract class OSHiltTest : OSTest() {
    abstract val hiltRule: HiltAndroidRule

    @Inject lateinit var persistenceManager: PersistenceManager

    @Inject lateinit var createMasterKeyUseCase: CreateMasterKeyUseCase

    @Inject lateinit var finishOnboardingUseCase: FinishOnboardingUseCase

    @Inject lateinit var cryptoRepository: MainCryptoRepository

    @Inject lateinit var lockAppUseCase: LockAppUseCase

    @Inject lateinit var localSignInUseCase: LocalSignInUseCase

    @Inject lateinit var workerFactory: HiltWorkerFactory

    @Inject lateinit var osAppSettings: OSAppSettings

    @Inject lateinit var mainDatabase: MainDatabase

    /**
     * See [InitialTestState] for more details.
     * Additional action linked to [InitialTestState] are executed before each test. See [initialTestState]
     */
    abstract val initialTestState: InitialTestState

    protected val testPassword: String = "a"

    @Before
    fun injectAndInit() {
        hiltRule.inject()
        initializeWorkManager()
        // Use runBlocking to make sure the test cannot start before/while initialize
        runBlocking {
            initialize()
        }
    }

    private fun initializeWorkManager() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .setWorkerFactory(workerFactory)
            .build()
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
    }

    /**
     * Default is signing in and do things for logout
     */
    private suspend fun initialize() {
        signOut()
        when (initialTestState) {
            is InitialTestState.SignedUp -> {
                signup()
                (initialTestState as? InitialTestState.SignedUp)?.thingsToDoLoggedIn?.invoke()
                unloadMasterKey()
            }

            is InitialTestState.SignedOut -> {} // Nothing to do
            is InitialTestState.LoggedIn -> {
                signup()
            }
        }
    }

    protected suspend fun signOut() {
        cryptoRepository.resetCryptography()
        persistenceManager.clearItems()
        mainDatabase.clearAllTables()
    }

    /**
     * Signup user with [password] and save credential. A master key will be generated at this point.
     */
    suspend fun signup(password: CharArray = testPassword.toCharArray()) {
        osAppSettings.setMigrationVersionSetting(MigrationConstant.LastVersion)
        createMasterKeyUseCase(password)
        finishOnboardingUseCase()
    }

    /**
     * Force a logout. Can be useful if you need to execute actions for a specific test.
     *
     * Example:
     * ```
     *      runTest {
     *          localSignInUseCase(testPassword.toCharArray())
     *          val item = createItemUseCase.test()
     *          moveToBinItemUseCase(item)
     *          unloadMasterKey()
     *      }
     *      // You will not be sign-in anymore but you can now test with a database filled with the new item.
     * ```
     */
    fun unloadMasterKey() {
        lockAppUseCase()
    }

    @After
    fun cleanDataAfterTest() {
        runTest { signOut() }
    }

    @After
    fun teardown() {
        unmockkAll()
    }
}
