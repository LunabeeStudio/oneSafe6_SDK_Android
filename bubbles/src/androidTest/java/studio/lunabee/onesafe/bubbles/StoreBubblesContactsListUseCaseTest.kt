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
 * Created by Lunabee Studio / Date - 5/22/2023 - for the oneSafe6 SDK.
 * Last modified 5/22/23, 6:00 PM
 */

package studio.lunabee.onesafe.bubbles

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.TestResult
import org.junit.Rule
import org.junit.Test
import java.util.UUID
import javax.inject.Inject
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import studio.lunabee.onesafe.bubbles.domain.model.PlainBubblesContact
import studio.lunabee.onesafe.bubbles.domain.repository.BubblesContactRepository
import studio.lunabee.onesafe.bubbles.domain.usecase.StoreBubblesContactsListUseCase
import studio.lunabee.onesafe.error.OSCryptoError
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltTest
import kotlin.test.assertFailsWith

@HiltAndroidTest
class StoreBubblesContactsListUseCaseTest : OSHiltTest() {

    @get:Rule
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.LoggedIn

    @Inject lateinit var storeBubblesContactsListUseCase: StoreBubblesContactsListUseCase

    @Inject lateinit var bubblesContactsRepository: BubblesContactRepository

    @Test
    fun test_store_list_of_contact(): TestResult = runTest {
        val contacts = listOf(
            PlainBubblesContact(
                id = UUID.randomUUID(),
                name = "Florian",
                key = "azerty",
            ),
            PlainBubblesContact(
                id = UUID.randomUUID(),
                name = "Rémi",
                key = "qwerty",
            ),
        )

        // As we want to test prod behavior we test that storing contact fail (not enable in prod) for now.
        val error = assertFailsWith<OSCryptoError> {
            storeBubblesContactsListUseCase(contacts)
        }
        assertEquals(OSCryptoError.Code.BUBBLES_CONTACT_KEY_NOT_LOADED, error.code)
    }
}
