package studio.lunabee.onesafe.feature.home

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import studio.lunabee.bubbles.domain.usecase.ContactLocalDecryptUseCase
import studio.lunabee.bubbles.domain.usecase.GetRecentContactsUseCase
import studio.lunabee.messaging.domain.repository.MessageRepository
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.bubbles.ui.conversation.ConversationSubtitleFromMessageDelegate
import studio.lunabee.onesafe.bubbles.ui.extension.getNameProvider
import studio.lunabee.onesafe.bubbles.ui.model.BubblesConversationInfo
import studio.lunabee.onesafe.bubbles.ui.model.ConversationSubtitle
import studio.lunabee.onesafe.commonui.CommonUiConstants
import studio.lunabee.onesafe.commonui.utils.CloseableCoroutineScope
import studio.lunabee.onesafe.commonui.utils.CloseableMainCoroutineScope
import studio.lunabee.onesafe.feature.home.model.HomeConversationSectionData
import javax.inject.Inject

interface HomeConversationSectionDelegate {
    val homeConversationSection: StateFlow<HomeConversationSectionData>
}

class HomeConversationSectionDelegateImpl @Inject constructor(
    getEncryptedBubblesContactList: GetRecentContactsUseCase,
    contactLocalDecryptUseCase: ContactLocalDecryptUseCase,
    private val messageRepository: MessageRepository,
    private val conversationSubtitleFromMessageDelegate: ConversationSubtitleFromMessageDelegate,
) : HomeConversationSectionDelegate, CloseableCoroutineScope by CloseableMainCoroutineScope() {
    override val homeConversationSection: StateFlow<HomeConversationSectionData> =
        getEncryptedBubblesContactList(AppConstants.Ui.HomeConversation.MaxShowAmount)
            .map { encryptedContacts ->
                encryptedContacts.map { contact ->
                    val decryptedNameResult = contactLocalDecryptUseCase(contact.encName, contact.id, String::class)
                    val lastMessage = messageRepository.getLastMessage(contact.id).firstOrNull()
                    BubblesConversationInfo(
                        nameProvider = decryptedNameResult.getNameProvider(),
                        subtitle = if (contact.encSharedKey != null) {
                            lastMessage?.let { conversationSubtitleFromMessageDelegate(lastMessage) }
                        } else {
                            ConversationSubtitle.NotReady
                        },
                        id = contact.id,
                        hasUnreadMessage = lastMessage?.isRead == false,
                    )
                }
                    .let(::HomeConversationSectionData)
            }.stateIn(
                coroutineScope,
                CommonUiConstants.Flow.DefaultSharingStarted,
                HomeConversationSectionData(emptyList()),
            )
}
