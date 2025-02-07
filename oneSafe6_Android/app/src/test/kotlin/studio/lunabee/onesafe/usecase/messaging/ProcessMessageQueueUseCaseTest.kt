package studio.lunabee.onesafe.usecase.messaging

import com.lunabee.lbcore.model.LBResult
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import studio.lunabee.bubbles.domain.model.contact.Contact
import studio.lunabee.bubbles.domain.model.contactkey.ContactSharedKey
import studio.lunabee.onesafe.error.BubblesCryptoError
import studio.lunabee.doubleratchet.model.DRMessageKey
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.messaging.domain.model.DecryptIncomingMessageData
import studio.lunabee.messaging.domain.model.SharedMessage
import studio.lunabee.messaging.domain.repository.EnqueuedMessageRepository
import studio.lunabee.messaging.domain.usecase.DecryptIncomingMessageUseCase
import studio.lunabee.messaging.domain.usecase.ProcessMessageQueueUseCase
import studio.lunabee.messaging.domain.usecase.SaveMessageUseCase
import studio.lunabee.onesafe.domain.usecase.authentication.IsSafeReadyUseCase
import studio.lunabee.onesafe.error.OSCryptoError
import studio.lunabee.onesafe.test.assertDoesNotThrow
import studio.lunabee.onesafe.test.firstSafeId
import java.util.UUID
import javax.inject.Inject
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(
    application = HiltTestApplication::class,
)
class ProcessMessageQueueUseCaseTest {

    @get:Rule
    val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @Inject lateinit var useCase: ProcessMessageQueueUseCase

    @Inject lateinit var enqueuedMessageRepository: EnqueuedMessageRepository

    private val contact: Contact = Contact(
        id = DoubleRatchetUUID(UUID.randomUUID()),
        encName = byteArrayOf(),
        encSharedKey = ContactSharedKey(byteArrayOf()),
        updatedAt = Clock.System.now(),
        sharedConversationId = DoubleRatchetUUID(UUID.randomUUID()),
        encSharingMode = byteArrayOf(),
        consultedAt = null,
        safeId = DoubleRatchetUUID(firstSafeId.id),
        encResetConversationDate = byteArrayOf(),
    )
    private val message: SharedMessage = SharedMessage(
        content = "",
        recipientId = DoubleRatchetUUID(UUID.randomUUID()),
        date = Clock.System.now(),
    )

    @BindValue val isSafeReadyUseCase: IsSafeReadyUseCase = mockk {
        every { this@mockk.flow() } returns flowOf(false, true)
    }

    @BindValue val decryptIncomingMessageUseCase: DecryptIncomingMessageUseCase = mockk {
        coEvery { this@mockk.invoke(any()) } returns LBResult.Success(
            DecryptIncomingMessageData.NewMessage(contact.id, message, DRMessageKey(byteArrayOf())),
        )
    }

    @BindValue val saveMessageUseCase: SaveMessageUseCase = mockk {
        coEvery { this@mockk.invoke(any(), any(), any(), any(), any()) } returns LBResult.Success(0f)
    }

    private val messages = List(10) { byteArrayOf(it.toByte()) }

    @Before
    fun setup() {
        hiltRule.inject()
        runTest {
            messages.forEach {
                enqueuedMessageRepository.save(it, null)
            }
        }
    }

    @Test
    fun flush_no_crypto_test(): TestResult = runTest {
        every { isSafeReadyUseCase.flow() } returns flowOf(false)

        useCase.flush()

        assertEquals(expected = messages.size, actual = enqueuedMessageRepository.getAll().size)
        coVerify(exactly = 0) { decryptIncomingMessageUseCase.invoke(any()) }
        coVerify(exactly = 0) { saveMessageUseCase.invoke(any(), any(), any(), any(), any()) }
    }

    @Test
    fun flush_test(): TestResult = runTest {
        useCase.flush()

        assertTrue(enqueuedMessageRepository.getAll().isEmpty())
        coVerify(exactly = messages.size) { decryptIncomingMessageUseCase.invoke(any()) }
        coVerify(exactly = messages.size) { saveMessageUseCase.invoke(any(), any(), any(), any(), any()) }
    }

    @Test
    fun flush_error_test(): TestResult = runTest {
        coEvery { decryptIncomingMessageUseCase.invoke(any()) } returns
            LBResult.Failure(OSCryptoError(OSCryptoError.Code.BUBBLES_DECRYPTION_FAILED_QUEUE_KEY))
        useCase.flush()

        assertTrue(enqueuedMessageRepository.getAll().isEmpty())
        coVerify(exactly = messages.size) { decryptIncomingMessageUseCase.invoke(any()) }
        coVerify(exactly = 0) { saveMessageUseCase.invoke(any(), any(), any(), any(), any()) }
    }

    // Error BUBBLES_MASTER_KEY_NOT_LOADED should not delete enqueued messages
    @Test
    fun flush_bubbles_master_error_test(): TestResult = runTest {
        coEvery { decryptIncomingMessageUseCase.invoke(any()) } answers {
            if ((firstArg() as ByteArray).first() % 2 == 0) {
                LBResult.Failure(BubblesCryptoError(BubblesCryptoError.Code.BUBBLES_MASTER_KEY_NOT_LOADED))
            } else {
                LBResult.Success(
                    DecryptIncomingMessageData.NewMessage(contact.id, message, DRMessageKey(byteArrayOf())),
                )
            }
        }

        useCase.flush()

        assertEquals(messages.size / 2, enqueuedMessageRepository.getAll().size)
        coVerify(exactly = messages.size) { decryptIncomingMessageUseCase.invoke(any()) }
        coVerify(exactly = messages.size / 2) { saveMessageUseCase.invoke(any(), any(), any(), any(), any()) }
    }

    @Test
    fun observe_process_all_test(): TestResult = runTest {
        val observeJob = launch {
            useCase.observe()
        }

        while (enqueuedMessageRepository.getAll().isNotEmpty()) {
            /* no-op */
        }

        observeJob.cancel()

        assertTrue(enqueuedMessageRepository.getAll().isEmpty())
        coVerify(exactly = messages.size) { decryptIncomingMessageUseCase.invoke(any()) }
        coVerify(exactly = messages.size) { saveMessageUseCase.invoke(any(), any(), any(), any(), any()) }
    }

    @Test
    fun observe_switch_crypto_test(): TestResult = runTest {
        val crypto = MutableStateFlow(false)
        every { isSafeReadyUseCase.flow() } returns crypto

        val observeJob = launch {
            useCase.observe()
        }

        assertEquals(expected = messages.size, actual = enqueuedMessageRepository.getAll().size)
        crypto.value = true

        while (enqueuedMessageRepository.getAll().isNotEmpty()) {
            /* no-op */
        }

        assertTrue(enqueuedMessageRepository.getAll().isEmpty())
        crypto.value = false
        enqueuedMessageRepository.save(byteArrayOf(0), null)
        enqueuedMessageRepository.save(byteArrayOf(1), null)
        assertEquals(expected = 2, actual = enqueuedMessageRepository.getAll().size)

        crypto.value = true

        while (enqueuedMessageRepository.getAll().isNotEmpty()) {
            /* no-op */
        }

        observeJob.cancel()

        coVerify(exactly = messages.size + 2) { decryptIncomingMessageUseCase.invoke(any()) }
        coVerify(exactly = messages.size + 2) { saveMessageUseCase.invoke(any(), any(), any(), any(), any()) }
    }

    /**
     * Make sure observe and flush can run concurrently
     */
    @Test
    fun observe_flush_concurrent(): TestResult = runTest {
        assertDoesNotThrow {
            val observeJob = launch {
                useCase.observe()
            }
            launch {
                useCase.flush()
            }
            while (enqueuedMessageRepository.getAll().isNotEmpty()) {
                /* no-op */
            }

            observeJob.cancel()
        }
    }

    /**
     * Make sure flush and observe can run concurrently
     */
    @Test
    fun flush_observe_concurrent(): TestResult = runTest {
        assertDoesNotThrow {
            launch {
                useCase.flush()
            }
            val observeJob = launch {
                useCase.observe()
            }
            while (enqueuedMessageRepository.getAll().isNotEmpty()) {
                /* no-op */
            }

            observeJob.cancel()
        }
    }
}
