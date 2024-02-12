package io.github.mklkj.kommunicator.ui.modules.conversation

import io.github.mklkj.kommunicator.data.models.MessageRequest
import io.github.mklkj.kommunicator.data.repository.MessagesRepository
import io.github.mklkj.kommunicator.data.ws.ConversationClient
import io.github.mklkj.kommunicator.ui.base.BaseViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.uuid.UUID
import org.koin.core.annotation.Factory

@Factory
class ConversationViewModel(
    private val messagesRepository: MessagesRepository,
    private val conversationClient: ConversationClient,
) : BaseViewModel<ConversationState>(ConversationState()) {

    fun loadData(chatId: UUID) {
        loadChatDetails(chatId)
        observeMessages(chatId)
        refreshChat(chatId)
        initializeWebSocketSession(chatId)
    }

    private fun initializeWebSocketSession(chatId: UUID) {
        conversationClient.connect(
            chatId = chatId,
            onFailure = { error ->
                proceedError(error)
                mutableState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message,
                    )
                }
            }
        )
    }

    fun sendMessage(message: MessageRequest) {
        conversationClient.sendMessage(message)
    }

    fun onTyping(currentMessage: String) {
        conversationClient.onTyping(currentMessage.isBlank())
    }

    override fun onDispose() {
        super.onDispose()
        conversationClient.onDispose()
    }

    private fun loadChatDetails(chatId: UUID) {
        launch("chat_load_$chatId", cancelExisting = false) {
            runCatching { messagesRepository.getChat(chatId) }
                .onFailure { error ->
                    proceedError(error)
                    mutableState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message,
                        )
                    }
                }
                .onSuccess { chat ->
                    mutableState.update {
                        it.copy(
                            isLoading = false,
                            chat = chat,
                        )
                    }
                }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeMessages(chatId: UUID) {
        launch("observe_messages", isFlowObserver = true) {
            combine(
                flow = messagesRepository.observeMessages(chatId),
                flow2 = messagesRepository.observeParticipants(chatId)
                    .flatMapLatest { participants ->
                        conversationClient.typingParticipants.map { typing ->
                            participants.filter { it.id in typing.keys }
                        }
                    },
            ) { messages, typingParticipants ->
                mutableState.update {
                    it.copy(
                        messages = messages,
                        typingParticipants = typingParticipants,
                    )
                }
            }.collect()
        }
    }

    private fun refreshChat(chatId: UUID) {
        launch("chat_refresh_$chatId", cancelExisting = false) {
            runCatching { messagesRepository.refreshMessages(chatId) }
                .onFailure { error ->
                    proceedError(error)
                    mutableState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message,
                        )
                    }
                }
        }
    }
}
