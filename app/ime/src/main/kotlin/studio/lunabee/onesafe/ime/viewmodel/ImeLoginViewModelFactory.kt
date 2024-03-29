/*
 * Copyright (c) 2023 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 6/16/2023 - for the oneSafe6 SDK.
 * Last modified 6/16/23, 11:31 AM
 */

package studio.lunabee.onesafe.ime.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import studio.lunabee.onesafe.domain.common.FeatureFlags
import studio.lunabee.onesafe.domain.qualifier.StoreBetaTrack
import studio.lunabee.onesafe.domain.qualifier.VersionName
import studio.lunabee.onesafe.domain.repository.MainCryptoRepository
import studio.lunabee.onesafe.domain.usecase.authentication.IsBiometricEnabledUseCase
import studio.lunabee.onesafe.domain.usecase.authentication.IsCryptoDataReadyInMemoryUseCase
import studio.lunabee.onesafe.domain.usecase.authentication.LocalSignInUseCase
import studio.lunabee.onesafe.ime.ui.biometric.ImeBiometricResultRepository
import studio.lunabee.onesafe.login.viewmodel.LoginFromPasswordDelegateImpl
import studio.lunabee.onesafe.login.viewmodel.LoginUiStateHolder
import studio.lunabee.onesafe.migration.MigrateAndSignInUseCase
import studio.lunabee.onesafe.visits.OSAppVisit
import javax.inject.Inject

class ImeLoginViewModelFactory @Inject constructor(
    private val isBiometricEnabledUseCase: IsBiometricEnabledUseCase,
    private val osAppVisit: OSAppVisit,
    private val migrateAndSignInUseCase: MigrateAndSignInUseCase,
    @VersionName val versionName: String,
    private val localSignInUseCase: LocalSignInUseCase,
    private val featureFlags: FeatureFlags,
    private val imeBiometricResultRepository: ImeBiometricResultRepository,
    private val mainCryptoRepository: MainCryptoRepository,
    private val isCryptoDataReadyInMemoryUseCase: IsCryptoDataReadyInMemoryUseCase,
    @StoreBetaTrack private val isBetaVersion: Boolean,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val loginUiStateHolder = LoginUiStateHolder(isCryptoDataReadyInMemoryUseCase, osAppVisit, isBetaVersion)
        @Suppress("UNCHECKED_CAST")
        return ImeLoginViewModel(
            isBiometricEnabledUseCase = isBiometricEnabledUseCase,
            loginUiStateHolder = loginUiStateHolder,
            loginFromPasswordDelegate = LoginFromPasswordDelegateImpl(
                localSignInUseCase = localSignInUseCase,
                migrateAndSignInUseCase = migrateAndSignInUseCase,
                featureFlags = featureFlags,
                loginUiStateHolder = loginUiStateHolder,
            ),
            imeBiometricResultRepository = imeBiometricResultRepository,
            mainCryptoRepository = mainCryptoRepository,
            versionName = versionName,
        ) as T
    }
}
