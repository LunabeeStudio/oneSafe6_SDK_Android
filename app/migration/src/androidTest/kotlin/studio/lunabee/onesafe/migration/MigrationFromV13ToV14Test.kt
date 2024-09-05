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
 * Created by Lunabee Studio / Date - 3/12/2024 - for the oneSafe6 SDK.
 * Last modified 12/03/2024 09:05
 */

package studio.lunabee.onesafe.migration

import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.toKotlinInstant
import org.junit.Rule
import org.junit.Test
import studio.lunabee.bubbles.domain.model.EncryptEntry
import studio.lunabee.bubbles.domain.model.MessageSharingMode
import studio.lunabee.bubbles.domain.model.contact.PlainContact
import studio.lunabee.bubbles.domain.model.contactkey.ContactLocalKey
import studio.lunabee.bubbles.domain.repository.BubblesCryptoRepository
import studio.lunabee.bubbles.domain.repository.ContactKeyRepository
import studio.lunabee.bubbles.domain.repository.ContactRepository
import studio.lunabee.bubbles.domain.usecase.ContactLocalDecryptUseCase
import studio.lunabee.bubbles.domain.usecase.CreateContactUseCase
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.onesafe.cryptography.PasswordHashEngine
import studio.lunabee.onesafe.cryptography.SaltProvider
import studio.lunabee.onesafe.migration.migration.MigrationFromV13ToV14
import studio.lunabee.onesafe.migration.utils.MigrationCryptoV1UseCase
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltTest
import studio.lunabee.onesafe.test.OSTestConfig
import studio.lunabee.onesafe.test.assertSuccess
import studio.lunabee.onesafe.test.firstSafeId
import studio.lunabee.onesafe.test.testUUIDs
import java.time.Instant
import javax.inject.Inject
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@HiltAndroidTest
class MigrationFromV13ToV14Test : OSHiltTest() {
    @get:Rule override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)
    override val initialTestState: InitialTestState = InitialTestState.Home()

    @Inject lateinit var migrationFromV13ToV14: MigrationFromV13ToV14

    @Inject lateinit var bubblesCryptoRepository: BubblesCryptoRepository

    @Inject lateinit var contactRepository: ContactRepository

    @Inject lateinit var contactKeyRepository: ContactKeyRepository

    @Inject lateinit var createContactUseCase: CreateContactUseCase

    @Inject lateinit var contactLocalDecryptUseCase: ContactLocalDecryptUseCase

    @Inject lateinit var migrationCryptoV1UseCase: MigrationCryptoV1UseCase

    private val salt: ByteArray = OSTestConfig.random.nextBytes(32)

    @Inject lateinit var hashEngine: PasswordHashEngine

    @BindValue
    val saltProvider: SaltProvider = mockk {
        every { this@mockk.invoke(any()) } returns salt.copyOf()
    }

    private val masterKey: ByteArray by lazy { runBlocking { hashEngine.deriveKey(testPassword.toCharArray(), salt) } }

    @Test
    fun run_migrationFromV13ToV14_test(): TestResult = runTest {
        // Setup V13 state
        listOf(
            defaultContact(CypherName, MessageSharingMode.CypherText) to false,
            defaultContact(DeeplinkName, MessageSharingMode.Deeplink) to true,
        ).forEach { (contact, isDeepLink) ->
            createContactUseCase(contact)
            val localKey: ContactLocalKey = contactKeyRepository.getContactLocalKey(contact.id)
            contactRepository.updateContact(
                id = contact.id,
                encSharingMode = bubblesCryptoRepository.localEncrypt(localKey, EncryptEntry(isDeepLink)), // Set a boolean like in v13
                encName = bubblesCryptoRepository.localEncrypt(localKey, EncryptEntry(contact.name)),
                updateAt = Instant.now(testClock).toKotlinInstant(),
            )
        }
        contactRepository.getAllContactsFlow(DoubleRatchetUUID(firstSafeId.id)).first().forEach { contact ->
            val decryptNameResult = contactLocalDecryptUseCase(
                contact.encName,
                contact.id,
                String::class,
            )
            val decryptedName = assertSuccess(decryptNameResult).successData
            val decryptedMessageSharingModeResult = contactLocalDecryptUseCase(
                contact.encSharingMode,
                contact.id,
                Boolean::class,
            )
            val decryptedMessageSharingMode = assertSuccess(decryptedMessageSharingModeResult).successData

            when (decryptedName) {
                CypherName -> assertFalse(decryptedMessageSharingMode)
                DeeplinkName -> assertTrue(decryptedMessageSharingMode)
                else -> error("Unexpected value: $decryptedName")
            }
        }

        // Migrate
        val safeCrypto = safeRepository.getAllSafeOrderByLastOpenAsc().first()
        val bubblesMasterKey = migrationCryptoV1UseCase.decrypt(safeCrypto.encBubblesKey!!, masterKey)
        migrationFromV13ToV14(bubblesMasterKey, safeCrypto.id)

        // Check V14 state
        contactRepository.getAllContactsFlow(DoubleRatchetUUID(firstSafeId.id)).first().forEach { contact ->
            val contactResult = contactLocalDecryptUseCase(
                contact.encName,
                contact.id,
                String::class,
            )
            val decryptedName = assertSuccess(contactResult).successData
            val decryptedMessageSharingModeResult = contactLocalDecryptUseCase(
                contact.encSharingMode,
                contact.id,
                MessageSharingMode::class,
            )
            val decryptedMessageSharingMode = assertSuccess(decryptedMessageSharingModeResult).successData

            when (decryptedName) {
                CypherName -> assertEquals(MessageSharingMode.CypherText, decryptedMessageSharingMode)
                DeeplinkName -> assertEquals(MessageSharingMode.Deeplink, decryptedMessageSharingMode)
                else -> error("Unexpected value: $decryptedName")
            }
        }
    }

    private fun defaultContact(name: String, sharingMode: MessageSharingMode): PlainContact = PlainContact(
        id = DoubleRatchetUUID(testUUIDs.random(OSTestConfig.random)),
        name = name,
        sharedKey = null,
        sharedConversationId = DoubleRatchetUUID(testUUIDs.random(OSTestConfig.random)),
        sharingMode = sharingMode,
    )

    companion object {
        private const val DeeplinkName = "Deeplinks"
        private const val CypherName = "Cypher"
    }
}
