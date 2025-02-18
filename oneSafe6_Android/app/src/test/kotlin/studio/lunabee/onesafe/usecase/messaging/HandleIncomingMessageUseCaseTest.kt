package studio.lunabee.onesafe.usecase.messaging

import com.lunabee.lbcore.model.LBResult
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
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
import studio.lunabee.doubleratchet.model.DRMessageKey
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.messaging.domain.model.DecryptIncomingMessageData
import studio.lunabee.messaging.domain.model.SharedMessage
import studio.lunabee.messaging.domain.usecase.DecryptIncomingMessageUseCase
import studio.lunabee.messaging.domain.usecase.HandleIncomingMessageUseCase
import studio.lunabee.messaging.domain.usecase.IncomingMessageState
import studio.lunabee.messaging.domain.usecase.SaveMessageUseCase
import studio.lunabee.onesafe.domain.usecase.authentication.IsSafeReadyUseCase
import studio.lunabee.onesafe.test.firstSafeId
import java.util.UUID
import javax.inject.Inject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.assertEquals
import kotlin.test.assertIs

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(
    application = HiltTestApplication::class,
)
class HandleIncomingMessageUseCaseTest {

    @get:Rule
    val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @BindValue val isSafeReadyUseCase: IsSafeReadyUseCase = mockk {
        every { this@mockk.flow() } returns flowOf(true)
    }

    @BindValue val decryptIncomingMessageUseCase: DecryptIncomingMessageUseCase = mockk()

    @BindValue val saveMessageUseCase: SaveMessageUseCase = mockk()

    @Inject
    lateinit var useCase: HandleIncomingMessageUseCase

    @OptIn(ExperimentalEncodingApi::class)
    @Test
    fun enqueue_test(): TestResult = runTest {
        every { isSafeReadyUseCase.flow() } returns flowOf(false)
        val actual = useCase(Base64.decode("aGVsbG8="), null, false)
        assertEquals(IncomingMessageState.Enqueued, actual.data)
    }

    @OptIn(ExperimentalEncodingApi::class)
    @Test
    fun processed_test(): TestResult = runTest {
        val contact = Contact(
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
        val message = SharedMessage(
            content = "",
            recipientId = DoubleRatchetUUID(UUID.randomUUID()),
            date = Clock.System.now(),
        )
        coEvery { decryptIncomingMessageUseCase.invoke(any()) } returns LBResult.Success(
            DecryptIncomingMessageData.NewMessage(
                contact.id,
                message,
                DRMessageKey(byteArrayOf()),
            ),
        )
        coEvery { saveMessageUseCase.invoke(any(), any(), any(), any(), any()) } returns LBResult.Success(0f)
        val actual = useCase(Base64.decode("aGVsbG8="), null, true).data
        assertIs<IncomingMessageState.Processed>(actual)
        assertEquals(contact.id, actual.contactId)
    }

    @OptIn(ExperimentalEncodingApi::class)
    @Test
    fun decrypt_error_test(): TestResult = runTest {
        coEvery { decryptIncomingMessageUseCase.invoke(any()) } returns LBResult.Failure(null)
        val actual = useCase(Base64.decode("aGVsbG8="), null, true)
        assertIs<LBResult.Failure<IncomingMessageState>>(actual)
    }

    @OptIn(ExperimentalEncodingApi::class)
    @Test
    fun save_error_test(): TestResult = runTest {
        val contact = Contact(
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
        val message = SharedMessage(
            content = "",
            recipientId = DoubleRatchetUUID(UUID.randomUUID()),
            date = Clock.System.now(),
        )
        coEvery { decryptIncomingMessageUseCase.invoke(any()) } returns LBResult.Success(
            DecryptIncomingMessageData.NewMessage(contact.id, message, DRMessageKey(byteArrayOf())),
        )
        coEvery { saveMessageUseCase.invoke(any(), any(), any(), any(), any()) } returns LBResult.Failure(null)

        val actual = useCase(Base64.decode("aGVsbG8="), null, true)
        assertIs<LBResult.Failure<IncomingMessageState>>(actual)
    }
}
