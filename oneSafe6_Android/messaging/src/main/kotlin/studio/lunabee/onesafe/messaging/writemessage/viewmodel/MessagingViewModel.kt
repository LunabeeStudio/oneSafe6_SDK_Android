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
 * Created by Lunabee Studio / Date - 7/5/2023 - for the oneSafe6 SDK.
 * Last modified 7/5/23, 3:56 PM
 */

package studio.lunabee.onesafe.messaging.writemessage.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import studio.lunabee.messaging.domain.usecase.ProcessMessageQueueUseCase
import javax.inject.Inject

@HiltViewModel
class MessagingViewModel @Inject constructor(
    private val processMessageQueueUseCase: ProcessMessageQueueUseCase,
) : ViewModel() {
    suspend fun observeQueue() {
        processMessageQueueUseCase.observe()
    }
}
