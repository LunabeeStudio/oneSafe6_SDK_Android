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
 * Created by Lunabee Studio / Date - 6/19/2024 - for the oneSafe6 SDK.
 * Last modified 6/19/24, 5:39 PM
 */

package studio.lunabee.onesafe.domain.usecase.settings

import com.lunabee.lbcore.model.LBResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.model.verifypassword.VerifyPasswordInterval
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.domain.repository.SecuritySettingsRepository
import studio.lunabee.onesafe.error.OSError
import java.time.Instant
import javax.inject.Inject
import kotlin.time.Duration

/**
 * Retrieve individual safe security setting as a flow or as a [LBResult]
 */
class GetSecuritySettingUseCase @Inject constructor(
    private val securitySettingsRepository: SecuritySettingsRepository,
    private val safeRepository: SafeRepository,
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    fun autoLockInactivityDelayFlow(): Flow<Duration> = safeRepository.currentSafeIdFlow().flatMapLatest { safeId ->
        safeId?.let {
            securitySettingsRepository.autoLockInactivityDelayFlow(safeId)
        } ?: flowOf()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun autoLockAppChangeDelayFlow(): Flow<Duration> = safeRepository.currentSafeIdFlow().flatMapLatest { safeId ->
        safeId?.let {
            securitySettingsRepository.autoLockAppChangeDelayFlow(safeId)
        } ?: flowOf()
    }

    /**
     * Retrieve the user param delay before cleaning the clipboard when moving the app to the background
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun clipboardDelayFlow(): Flow<Duration> = safeRepository.currentSafeIdFlow().flatMapLatest { safeId ->
        safeId?.let {
            securitySettingsRepository.clipboardDelayFlow(safeId)
        } ?: flowOf()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun verifyPasswordIntervalFlow(): Flow<VerifyPasswordInterval> = safeRepository.currentSafeIdFlow().flatMapLatest { safeId ->
        safeId?.let {
            securitySettingsRepository.verifyPasswordIntervalFlow(safeId)
        } ?: flowOf()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun bubblesResendMessageDelayFlow(): Flow<Duration> = safeRepository.currentSafeIdFlow().flatMapLatest { safeId ->
        safeId?.let {
            securitySettingsRepository.bubblesResendMessageDelayFlow(safeId)
        } ?: flowOf()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun autoLockOSKInactivityDelayFlow(): Flow<Duration> = safeRepository.currentSafeIdFlow().flatMapLatest { safeId ->
        safeId?.let {
            securitySettingsRepository.autoLockOSKInactivityDelayFlow(safeId)
        } ?: flowOf()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun autoLockOSKHiddenDelayFlow(): Flow<Duration> = safeRepository.currentSafeIdFlow().flatMapLatest { safeId ->
        safeId?.let {
            securitySettingsRepository.autoLockOSKHiddenDelayFlow(safeId)
        } ?: flowOf()
    }

    suspend fun autoLockInactivityDelay(): LBResult<Duration> = OSError.runCatching {
        val safeId = safeRepository.currentSafeId()
        securitySettingsRepository.autoLockInactivityDelay(safeId)
    }

    suspend fun autoLockAppChangeDelay(): LBResult<Duration> = OSError.runCatching {
        val safeId = safeRepository.currentSafeId()
        securitySettingsRepository.autoLockAppChangeDelay(safeId)
    }

    /**
     * @see clipboardDelayFlow
     */
    suspend fun clipboardClearDelay(safeId: SafeId? = null): LBResult<Duration> = OSError.runCatching {
        val currentSafeId = safeId ?: safeRepository.currentSafeId()
        securitySettingsRepository.clipboardClearDelay(currentSafeId)
    }

    suspend fun verifyPasswordInterval(): LBResult<VerifyPasswordInterval> = OSError.runCatching {
        val safeId = safeRepository.currentSafeId()
        securitySettingsRepository.verifyPasswordInterval(safeId)
    }

    suspend fun lastPasswordVerificationInstant(): LBResult<Instant?> = OSError.runCatching {
        val safeId = safeRepository.currentSafeId()
        securitySettingsRepository.lastPasswordVerificationInstant(safeId)
    }

    suspend fun autoLockOSKInactivityDelay(): LBResult<Duration> = OSError.runCatching {
        val safeId = safeRepository.currentSafeId()
        securitySettingsRepository.autoLockOSKInactivityDelay(safeId)
    }

    suspend fun autoLockOSKHiddenDelay(): LBResult<Duration> = OSError.runCatching {
        val safeId = safeRepository.currentSafeId()
        securitySettingsRepository.autoLockOSKHiddenDelay(safeId)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun shakeToLockFlow(): Flow<Boolean> = safeRepository.currentSafeIdFlow().flatMapLatest { safeId ->
        safeId?.let {
            securitySettingsRepository.shakeToLockFlow(safeId)
        } ?: flowOf()
    }

    suspend fun shakeToLock(): Boolean = securitySettingsRepository.shakeToLock(safeRepository.currentSafeId())
}
