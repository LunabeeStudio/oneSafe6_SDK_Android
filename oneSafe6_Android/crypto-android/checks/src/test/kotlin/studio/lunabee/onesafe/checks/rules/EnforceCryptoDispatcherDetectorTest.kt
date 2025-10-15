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
 * Created by Lunabee Studio / Date - 3/6/2024 - for the oneSafe6 SDK.
 * Last modified 3/6/24, 8:51 AM
 */

package studio.lunabee.onesafe.checks.rules

import com.android.tools.lint.checks.infrastructure.TestFiles.kt
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import studio.lunabee.onesafe.checks.EnforceCryptoDispatcherDetector
import studio.lunabee.onesafe.domain.qualifier.CryptoDispatcher
import studio.lunabee.onesafe.domain.repository.MainCryptoRepository
import kotlin.test.Test

class EnforceCryptoDispatcherDetectorTest {
    // Make sure we always test correct classes
    private val cryptoDispatcherName = CryptoDispatcher::class.simpleName
    private val mainCryptoRepoName = MainCryptoRepository::class.simpleName
    private val mainCryptoRepoQualified = MainCryptoRepository::class.qualifiedName

    private val cryptoQualifierStub = kt(
        """
                package studio.lunabee.onesafe.domain.qualifier
                annotation class $cryptoDispatcherName    
                """,
    ).indented()

    private val flowStub = kt(
        """
                package kotlinx.coroutines.flow
                interface Flow<out T> { }
                fun <T> Flow<T>.flowOn(context: CoroutineContext): Flow<T> { return this }
                fun <T> Flow<T>.distinctUntilChanged(): Flow<T> = this
                """,
    ).indented()

    private val mainRepoStub = kt(
        """
                package studio.lunabee.onesafe.domain.repository
                interface $mainCryptoRepoName { }
                """,
    ).indented()

    @Test
    fun suspend_call_with_withContext_check_test() {
        lint()
            .files(
                cryptoQualifierStub,
                mainRepoStub,
                kt(
                    """
                package studio.lunabee.onesafe.cryptography.android
                import ${CryptoDispatcher::class.qualifiedName}
                import $mainCryptoRepoQualified
                class AndroidMainCryptoRepository(
                    @$cryptoDispatcherName private val myCryptoDispatcher: CoroutineDispatcher,
                ) : $mainCryptoRepoName {
                    suspend fun publicSuspendReturnMethod(): Unit = withContext(myCryptoDispatcher) {
                        delay(0)
                    }
                    suspend fun publicSuspendBlockMethod() { 
                        withContext(myCryptoDispatcher) {
                            delay(0)
                        }
                    }
                }
                """,
                ).indented(),
            ).allowMissingSdk()
            .issues(EnforceCryptoDispatcherDetector.SuspendCryptoDispatcherIssue)
            .run()
            .expect("No warnings.")
    }

    @Test
    fun suspend_call_without_withContext_check_test() {
        lint()
            .files(
                cryptoQualifierStub,
                mainRepoStub,
                kt(
                    """
                package studio.lunabee.onesafe.cryptography.android
                import ${CryptoDispatcher::class.qualifiedName}
                import $mainCryptoRepoQualified
                class AndroidMainCryptoRepository(
                    @$cryptoDispatcherName private val myCryptoDispatcher: CoroutineDispatcher,
                    private val wrongDispatcher: CoroutineDispatcher,
                ) : $mainCryptoRepoName {
                    suspend fun publicSuspendMethod() { 
                        delay(0)
                    }
                    suspend fun publicSuspendMethodWrong(): Unit = withContext(wrongDispatcher) {
                        delay(0)
                    }
                }
                """,
                ).indented(),
            ).issues(EnforceCryptoDispatcherDetector.SuspendCryptoDispatcherIssue)
            .allowMissingSdk()
            .run()
            .expect(
                """
                src/studio/lunabee/onesafe/cryptography/android/AndroidMainCryptoRepository.kt:8: Error: Expected call to withContext(CryptoDispatcher) not found [EnforceCryptoDispatcherContext]
                    suspend fun publicSuspendMethod() { 
                                ~~~~~~~~~~~~~~~~~~~
                src/studio/lunabee/onesafe/cryptography/android/AndroidMainCryptoRepository.kt:11: Error: Expected call to withContext(CryptoDispatcher) not found [EnforceCryptoDispatcherContext]
                    suspend fun publicSuspendMethodWrong(): Unit = withContext(wrongDispatcher) {
                                ~~~~~~~~~~~~~~~~~~~~~~~~
                2 errors, 0 warnings
                """.trimIndent(),
            )
    }

    @Test
    fun non_suspend_call_without_withContext_check_test() {
        lint()
            .files(
                mainRepoStub,
                cryptoQualifierStub,
                kt(
                    """
                package studio.lunabee.onesafe.cryptography.android
                class AndroidMainCryptoRepository : $mainCryptoRepoName {
                    fun publicMethod() { 
                        println("Hello")
                    }
                }
                """,
                ).indented(),
            ).issues(EnforceCryptoDispatcherDetector.SuspendCryptoDispatcherIssue)
            .allowMissingSdk()
            .run()
            .expect("No warnings.")
    }

    @Test
    fun flow_call_with_flowOn_check_test() {
        lint()
            .files(
                flowStub,
                cryptoQualifierStub,
                mainRepoStub,
                kt(
                    """
                package studio.lunabee.onesafe.cryptography.android

                import ${CryptoDispatcher::class.qualifiedName}
                import kotlinx.coroutines.flow.Flow
                import kotlinx.coroutines.flow.flowOn
                import kotlinx.coroutines.flow.distinctUntilChanged

                import $mainCryptoRepoQualified
                class AndroidMainCryptoRepository(
                    @$cryptoDispatcherName private val myCryptoDispatcher: CoroutineDispatcher,
                ) : $mainCryptoRepoName {
                    fun publicFlowBlockMethod(): Flow<Unit> { 
                        return flowOf(Unit).flowOn(myCryptoDispatcher).distinctUntilChanged()
                    }
                    fun publicFlowReturnMethod(): Flow<Unit> = flowOf(Unit).flowOn(myCryptoDispatcher)
                }
                """,
                ).indented(),
            ).issues(EnforceCryptoDispatcherDetector.FlowCryptoDispatcherIssue)
            .allowMissingSdk()
            .run()
            .expect("No warnings.")
    }

    @Test
    fun flow_call_without_flowOn_check_test() {
        lint()
            .files(
                flowStub,
                cryptoQualifierStub,
                mainRepoStub,
                kt(
                    """
                package studio.lunabee.onesafe.cryptography.android

                import ${CryptoDispatcher::class.qualifiedName}
                import kotlinx.coroutines.flow.Flow
                import kotlinx.coroutines.flow.flowOn

                import $mainCryptoRepoQualified
                
                class AndroidMainCryptoRepository(
                    @$cryptoDispatcherName private val myCryptoDispatcher: CoroutineDispatcher,
                    private val wrongDispatcher: CoroutineDispatcher,
                ) : $mainCryptoRepoName {
                    fun publicFlowBlockMethod(): Flow<Unit> { 
                        return flowOf(Unit)
                    }
                    fun publicFlowReturnMethod(): Flow<Unit> = flowOf(Unit)
                    fun publicFlowReturnMethod(): Flow<Unit> = flowOf(Unit).flowOn(wrongDispatcher)
                }
                """,
                ).indented(),
            ).issues(EnforceCryptoDispatcherDetector.FlowCryptoDispatcherIssue)
            .allowMissingSdk()
            .run()
            .expect(
                """
                src/studio/lunabee/onesafe/cryptography/android/AndroidMainCryptoRepository.kt:13: Error: Expected call to flowOn(CryptoDispatcher) not found [EnforceCryptoDispatcherFlow]
                    fun publicFlowBlockMethod(): Flow<Unit> { 
                        ~~~~~~~~~~~~~~~~~~~~~
                src/studio/lunabee/onesafe/cryptography/android/AndroidMainCryptoRepository.kt:16: Error: Expected call to flowOn(CryptoDispatcher) not found [EnforceCryptoDispatcherFlow]
                    fun publicFlowReturnMethod(): Flow<Unit> = flowOf(Unit)
                        ~~~~~~~~~~~~~~~~~~~~~~
                src/studio/lunabee/onesafe/cryptography/android/AndroidMainCryptoRepository.kt:17: Error: Expected call to flowOn(CryptoDispatcher) not found [EnforceCryptoDispatcherFlow]
                    fun publicFlowReturnMethod(): Flow<Unit> = flowOf(Unit).flowOn(wrongDispatcher)
                        ~~~~~~~~~~~~~~~~~~~~~~
                3 errors, 0 warnings                    
                """.trimIndent(),
            )
    }
}
