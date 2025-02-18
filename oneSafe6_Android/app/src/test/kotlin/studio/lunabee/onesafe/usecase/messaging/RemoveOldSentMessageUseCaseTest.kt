package studio.lunabee.onesafe.usecase.messaging

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.toKotlinInstant
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import studio.lunabee.bubbles.domain.model.contact.PlainContact
import studio.lunabee.bubbles.domain.usecase.CreateContactUseCase
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.doubleratchet.model.createRandomUUID
import studio.lunabee.messaging.domain.model.SharedMessage
import studio.lunabee.messaging.domain.repository.MessageRepository
import studio.lunabee.messaging.domain.repository.SentMessageRepository
import studio.lunabee.messaging.domain.usecase.RemoveOldSentMessagesUseCase
import studio.lunabee.messaging.domain.usecase.SaveMessageUseCase
import studio.lunabee.messaging.domain.usecase.SaveSentMessageUseCase
import studio.lunabee.onesafe.domain.repository.SecuritySettingsRepository
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltUnitTest
import studio.lunabee.onesafe.test.OSTestConfig
import studio.lunabee.onesafe.test.assertSuccess
import studio.lunabee.onesafe.test.firstSafeId
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@HiltAndroidTest
class RemoveOldSentMessageUseCaseTest : OSHiltUnitTest() {

    private val contactId: DoubleRatchetUUID = DoubleRatchetUUID(UUID.randomUUID())
    private val recipientId: DoubleRatchetUUID = DoubleRatchetUUID(UUID.randomUUID())

    @get:Rule
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.Home()

    @Inject lateinit var saveSentMessageUseCase: SaveSentMessageUseCase

    @Inject lateinit var saveMessageUseCase: SaveMessageUseCase

    @Inject lateinit var createContactUseCase: CreateContactUseCase

    @Inject lateinit var messageRepository: MessageRepository

    @Inject lateinit var sentMessageRepository: SentMessageRepository

    @Inject lateinit var removeOldSentMessagesUseCase: RemoveOldSentMessagesUseCase

    @Inject lateinit var securitySettingsRepository: SecuritySettingsRepository

    @Before
    fun setup(): TestResult = runTest {
        createContactUseCase(
            PlainContact(
                id = contactId,
                name = contactId.toString(),
                sharedKey = null,
                sharedConversationId = DoubleRatchetUUID(UUID.randomUUID()),
            ),
        )
    }

    @Test
    fun save_sent_message_and_remove_outdated_message_test(): TestResult = runTest {
        val ttl = securitySettingsRepository.bubblesResendMessageDelayFlow(firstSafeId).first()
        val timeMessage1 = Instant.now(OSTestConfig.clock).minusMillis(ttl.inWholeMilliseconds * 2).toKotlinInstant()

        val id1 = saveSentMessageUseCase(
            plainMessage = SharedMessage("", recipientId, timeMessage1),
            messageString = byteArrayOf(),
            contactId = contactId,
            createdAt = timeMessage1,
            channel = "",
        ).data!!.id
        val id2 = saveSentMessageUseCase(
            plainMessage = SharedMessage("", recipientId, Instant.now(OSTestConfig.clock).toKotlinInstant()),
            messageString = byteArrayOf(),
            contactId = contactId,
            createdAt = Instant.now(OSTestConfig.clock).toKotlinInstant(),
            channel = "",
        ).data!!.id

        removeOldSentMessagesUseCase.invoke(DoubleRatchetUUID(firstSafeId.id))
        assertNull(sentMessageRepository.getSentMessage(id1))
        assertNotNull(sentMessageRepository.getSentMessage(id2))
    }

    @Test
    fun test_cascade_delete_on_message_deletion_test(): TestResult = runTest {
        // First message is not deleted (invitation)
        saveMessageUseCase(
            plainMessage = SharedMessage("", recipientId, Instant.now(OSTestConfig.clock).toKotlinInstant()),
            contactId = contactId,
            channel = "",
            id = createRandomUUID(),
            safeItemId = createRandomUUID(),
        ).data
        val result = saveSentMessageUseCase(
            plainMessage = SharedMessage("", recipientId, Instant.now(OSTestConfig.clock).plusMillis(1).toKotlinInstant()),
            messageString = byteArrayOf(),
            contactId = contactId,
            createdAt = Instant.now(OSTestConfig.clock).toKotlinInstant(),
            channel = "",
        )
        val idMessage = assertSuccess(result).successData?.id
        assertNotNull(idMessage)
        messageRepository.deleteAllMessages(contactId)
        assertNull(sentMessageRepository.getSentMessage(idMessage))
    }
}
