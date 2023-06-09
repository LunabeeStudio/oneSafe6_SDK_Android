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
 * Created by Lunabee Studio / Date - 5/22/2023 - for the oneSafe6 SDK.
 * Last modified 5/22/23, 3:11 PM
 */

package studio.lunabee.onesafe.bubbles.ui.delegate

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import studio.lunabee.onesafe.bubbles.ui.model.BubblesUser
import studio.lunabee.onesafe.bubbles.domain.usecase.StoreBubblesContactsListUseCase
import studio.lunabee.onesafe.domain.usecase.authentication.IsCryptoDataReadyInMemoryUseCase
import java.io.IOException
import javax.inject.Inject

// TODO : Temporary class used to test the Bubbles feature with oneSafeK
class BubbleUserDelegateImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val storeBubblesContactsListUseCase: StoreBubblesContactsListUseCase,
    private val isCryptoDataReadyInMemoryUseCase: IsCryptoDataReadyInMemoryUseCase,
) : BubbleUserDelegate {

    private val json = Json { ignoreUnknownKeys = true }

    private val _availableBubbleUsers: MutableStateFlow<List<BubblesUser>> = MutableStateFlow(listOf())
    override val availableBubbleUsers: StateFlow<List<BubblesUser>> = _availableBubbleUsers.asStateFlow()

    init {
        try {
            val jsonString = context.assets.open(MockJsonFileName).bufferedReader().use { it.readText() }
            _availableBubbleUsers.value = json.decodeFromString(string = jsonString)
        } catch (ioException: IOException) {
            ioException.printStackTrace()
        }
    }

    override suspend fun importContact(name: String) {
        if (isCryptoDataReadyInMemoryUseCase().first()) {
            availableBubbleUsers.value.firstOrNull { it.name == name }?.let {
                storeBubblesContactsListUseCase(it.contacts)
            }
        }
    }

    companion object {
        private const val MockJsonFileName: String = "mockBubblesContacts.json"
    }
}
