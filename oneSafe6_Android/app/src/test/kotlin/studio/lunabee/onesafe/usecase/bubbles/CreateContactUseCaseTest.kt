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

package studio.lunabee.onesafe.usecase.bubbles

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.bubbles.domain.model.contact.PlainContact
import studio.lunabee.bubbles.domain.usecase.ContactLocalDecryptUseCase
import studio.lunabee.bubbles.domain.usecase.CreateContactUseCase
import studio.lunabee.bubbles.domain.usecase.GetAllContactsUseCase
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltUnitTest
import studio.lunabee.onesafe.test.assertPropertiesEquals
import java.util.UUID
import javax.inject.Inject

@HiltAndroidTest
class CreateContactUseCaseTest : OSHiltUnitTest() {

    @get:Rule
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.Home()

    @Inject lateinit var createContactUseCase: CreateContactUseCase

    @Inject lateinit var getAllContactsUseCase: GetAllContactsUseCase

    @Inject lateinit var contactLocalDecryptUseCase: ContactLocalDecryptUseCase

    @Test
    fun store_contact_test(): TestResult = runTest {
        val expected = PlainContact(
            id = DoubleRatchetUUID(UUID.randomUUID()),
            name = "Rémi",
            sharedKey = "qwerty".toByteArray(),
            sharedConversationId = DoubleRatchetUUID(UUID.randomUUID()),
        )
        createContactUseCase(expected)
        val savedContact = getAllContactsUseCase().first().first()
        val actual = PlainContact(
            id = savedContact.id,
            sharedKey = contactLocalDecryptUseCase(savedContact.encSharedKey?.encKey!!, savedContact.id, ByteArray::class).data!!,
            name = contactLocalDecryptUseCase(savedContact.encName, savedContact.id, String::class).data!!,
            sharedConversationId = savedContact.sharedConversationId,
        )
        assertPropertiesEquals(expected, actual)
    }
}
