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
 * Created by Lunabee Studio / Date - 6/7/2023 - for the oneSafe6 SDK.
 * Last modified 6/7/23, 4:41 PM
 */

package studio.lunabee.onesafe.messaging.domain.usecase

import com.google.protobuf.timestamp
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.di.FrameworkTestModule
import studio.lunabee.onesafe.bubbles.domain.model.PlainContact
import studio.lunabee.onesafe.bubbles.domain.usecase.CreateContactUseCase
import studio.lunabee.onesafe.cryptography.CryptoEngine
import studio.lunabee.onesafe.domain.common.FeatureFlags
import studio.lunabee.onesafe.domain.qualifier.VersionName
import studio.lunabee.onesafe.error.OSDomainError
import studio.lunabee.onesafe.messagecompanion.messageData
import studio.lunabee.onesafe.messaging.domain.extension.toInstant
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltTest
import studio.lunabee.onesafe.test.assertFailure
import studio.lunabee.onesafe.test.assertSuccess
import java.util.UUID
import javax.inject.Inject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalEncodingApi::class)
@HiltAndroidTest
@UninstallModules(FrameworkTestModule::class)
class DecryptIncomingMessageUseCaseTest : OSHiltTest() {

    @get:Rule
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.LoggedIn

    @Inject
    lateinit var useCase: DecryptIncomingMessageUseCase

    @Inject
    lateinit var createContactUseCase: CreateContactUseCase

    @Inject
    lateinit var crypto: CryptoEngine

    @BindValue
    val flags: FeatureFlags = object : FeatureFlags {
        override fun florisBoard(): Boolean = false
        override fun accessibilityService(): Boolean = false
        override fun oneSafeK(): Boolean = false
        override fun bubbles(): Boolean = true
        override fun quickSignIn(): Boolean = false
    }

    @BindValue
    @VersionName
    val versionName: String = "test"

    private val contact1: PlainContact = PlainContact(UUID.randomUUID(), "contact", Base64.encode(Random.nextBytes(32)))
    private val contact2: PlainContact = PlainContact(UUID.randomUUID(), "contact2", Base64.encode(Random.nextBytes(32)))

    private val plainTimestamp = timestamp {
        seconds = 1686694686L
        nanos = 123
    }
    private val plainDate = plainTimestamp.toInstant()

    /**
     * Store one contact and decrypt a message from it
     */
    @Test
    fun match_single_contact_test(): TestResult = runTest {
        createContactUseCase(listOf(contact1))
        val plainContent = UUID.randomUUID().toString()
        val plainData = messageData {
            content = plainContent
            recipientId = contact1.id.toString()
            sentAt = plainTimestamp
        }
        val encData = crypto.encrypt(plainData.toByteArray(), Base64.decode(contact1.sharedKey), null)
        val encodedEncData = Base64.encode(encData)

        val expected = studio.lunabee.onesafe.messaging.domain.model.OSPlainMessage(
            content = plainContent,
            recipientId = contact1.id,
            sentAt = plainDate,
        )
        val actual = useCase(encodedEncData)
        assertSuccess(actual)
        assertEquals(expected, actual.successData.second)
    }

    /**
     * Store one contact and decrypt a message from another contact
     */
    @Test
    fun no_match_contact_test(): TestResult = runTest {
        createContactUseCase(listOf(contact1))
        val plainContent = UUID.randomUUID().toString()
        val plainData = messageData {
            content = plainContent
            recipientId = contact1.id.toString()
            sentAt = plainTimestamp
        }
        val encData = crypto.encrypt(plainData.toByteArray(), Base64.decode(contact2.sharedKey), null)
        val encodedEncData = Base64.encode(encData)

        val actual = useCase(encodedEncData)
        val error = assertFailure(actual).throwable
        assertIs<OSDomainError>(error)
        assertEquals(OSDomainError.Code.NO_MATCHING_CONTACT, error.code)
    }

    /**
     * Store two contacts and decrypt a message from the second contact
     */
    @Test
    fun match_multi_contact_test(): TestResult = runTest {
        createContactUseCase(listOf(contact1, contact2))
        val plainContent = UUID.randomUUID().toString()
        val plainData = messageData {
            content = plainContent
            recipientId = contact2.id.toString()
            sentAt = plainTimestamp
        }
        val encData = crypto.encrypt(plainData.toByteArray(), Base64.decode(contact2.sharedKey), null)
        val encodedEncData = Base64.encode(encData)

        val expected = studio.lunabee.onesafe.messaging.domain.model.OSPlainMessage(
            content = plainContent,
            recipientId = contact2.id,
            sentAt = plainDate,
        )
        val actual = useCase(encodedEncData)
        assertSuccess(actual)
        assertEquals(expected, actual.successData.second)
    }
}
