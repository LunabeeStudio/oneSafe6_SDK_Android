package studio.lunabee.onesafe.usecase.messaging

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.toKotlinInstant
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import studio.lunabee.bubbles.domain.model.contact.PlainContact
import studio.lunabee.bubbles.domain.usecase.ContactLocalDecryptUseCase
import studio.lunabee.bubbles.domain.usecase.CreateContactUseCase
import studio.lunabee.onesafe.error.BubblesMessagingError
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.doubleratchet.model.createRandomUUID
import studio.lunabee.messaging.domain.model.SharedMessage
import studio.lunabee.messaging.domain.repository.MessageRepository
import studio.lunabee.messaging.domain.usecase.SaveMessageUseCase
import studio.lunabee.onesafe.domain.common.MessageIdProvider
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltUnitTest
import studio.lunabee.onesafe.test.assertFailure
import studio.lunabee.onesafe.test.assertSuccess
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

@HiltAndroidTest
class SaveMessageUseCaseTest : OSHiltUnitTest() {

    private val contactId: DoubleRatchetUUID = DoubleRatchetUUID(UUID.randomUUID())
    private val recipientId: DoubleRatchetUUID = DoubleRatchetUUID(UUID.randomUUID())

    @get:Rule
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.Home()

    @Inject lateinit var saveMessageUseCase: SaveMessageUseCase

    @Inject lateinit var saveMessageUseCase2: SaveMessageUseCase

    @Inject lateinit var createContactUseCase: CreateContactUseCase

    @Inject lateinit var messageRepository: MessageRepository

    @Inject lateinit var contactLocalDecryptUseCase: ContactLocalDecryptUseCase

    @Inject lateinit var messageIdProvider: MessageIdProvider

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

    /**
     * Fail if running without critical section protected by mutex. Also make sure that the mutex is shared between use case instances
     */
    @Test
    fun save_concurrent_messages_test(): TestResult = runTest {
        val messages = List(100) {
            it.toString() to Instant.ofEpochSecond(it.toLong()).toKotlinInstant()
        }

        messages.forEachIndexed { idx, message ->
            val useCase = if (idx % 2 == 0) saveMessageUseCase else saveMessageUseCase2
            launch {
                useCase(
                    plainMessage = SharedMessage(message.first, recipientId, message.second),
                    contactId = contactId,
                    channel = "",
                    id = DoubleRatchetUUID(messageIdProvider()),
                    safeItemId = createRandomUUID(),
                )
            }
        }
    }

    /**
     * Insert a known sequences of message and check order after save
     */
    @Test
    fun save_messages_sequence_test(): TestResult = runTest {
        // Map of sentAt instant associated to the expected resulting order after sequential insertion
        val inputExpectedMap = listOf(
            Instant.ofEpochSecond(5).toKotlinInstant() to 0f,
            Instant.ofEpochSecond(2).toKotlinInstant() to -1f,
            Instant.ofEpochSecond(15).toKotlinInstant() to 1f,
            Instant.ofEpochSecond(10).toKotlinInstant() to 0.5f,
            Instant.ofEpochSecond(3).toKotlinInstant() to -0.5f,
            Instant.ofEpochSecond(1).toKotlinInstant() to -2f,
            Instant.ofEpochSecond(20).toKotlinInstant() to 2f,
            Instant.ofEpochSecond(12).toKotlinInstant() to 0.75f,
            Instant.ofEpochSecond(15).toKotlinInstant() to 1.5f,
        )

        inputExpectedMap.forEachIndexed { idx, input ->
            saveMessageUseCase(
                plainMessage = SharedMessage(idx.toString(), recipientId, input.first),
                contactId = contactId,
                channel = "",
                id = DoubleRatchetUUID(messageIdProvider()),
                safeItemId = createRandomUUID(),
            )
        }

        val actualMessagesId = mutableListOf<DoubleRatchetUUID>()
        inputExpectedMap.sortedByDescending { it.second }.forEach { entry ->
            val message = messageRepository.getByContactByOrder(contactId, entry.second)
            val sentAt = contactLocalDecryptUseCase(message.encSentAt, contactId, kotlinx.datetime.Instant::class).data!!
            // Assert each entry by getting it manually
            assertEquals(entry.first, sentAt)
            // Build a list of id from these queries
            actualMessagesId += message.id
        }

        val allMessagesId = messageRepository.getAllByContact(contactId).map { it.id }
        // Check the built list against the get all query
        assertContentEquals(actualMessagesId, allMessagesId)
    }

    @Test
    fun save_messages_duplicate_test(): TestResult = runTest {
        val content = "abc"
        val sentAt = Instant.ofEpochSecond(0L).toKotlinInstant()

        val saveResults = List(2) {
            saveMessageUseCase(
                plainMessage = SharedMessage(content, recipientId, sentAt),
                contactId = contactId,
                channel = "",
                id = DoubleRatchetUUID(messageIdProvider()),
                safeItemId = createRandomUUID(),
            )
        }

        val result1 = saveResults[0]
        val result2 = saveResults[1]
        val result3 = saveMessageUseCase(
            plainMessage = SharedMessage("123", recipientId, sentAt),
            contactId = contactId,
            channel = "",
            id = DoubleRatchetUUID(messageIdProvider()),
            safeItemId = createRandomUUID(),
        )

        assertSuccess(result1)
        assertFailure(result2)
        assertEquals(BubblesMessagingError.Code.DUPLICATED_MESSAGE, (result2.throwable as BubblesMessagingError).code)
        assertSuccess(result3)
    }
}
