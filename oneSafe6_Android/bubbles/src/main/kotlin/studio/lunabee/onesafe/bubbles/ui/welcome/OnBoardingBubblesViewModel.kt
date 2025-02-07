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
 * Created by Lunabee Studio / Date - 7/24/2023 - for the oneSafe6 SDK.
 * Last modified 24/07/2023 15:04
 */

package studio.lunabee.onesafe.bubbles.ui.welcome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.domain.usecase.settings.SetAppVisitUseCase
import javax.inject.Inject

@HiltViewModel
class OnBoardingBubblesViewModel @Inject constructor(
    private val setAppVisitUseCase: SetAppVisitUseCase,
) : ViewModel() {
    fun setHasDoneOnBoarding() {
        viewModelScope.launch {
            setAppVisitUseCase.setHasDoneOnboardingBubbles()
        }
    }
}
