package io.github.mklkj.kommunicator.ui.modules.conversation

import io.github.mklkj.kommunicator.data.models.MessageRequest
import io.github.mklkj.kommunicator.data.repository.MessagesRepository
import io.github.mklkj.kommunicator.data.ws.ConversationClient
import io.github.mklkj.kommunicator.ui.base.BaseViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
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

    fun sendMessage(chatId: UUID, message: MessageRequest) {
        conversationClient.sendMessage(chatId, message)
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

    private fun observeMessages(chatId: UUID) {
        launch("observe_messages", isFlowObserver = true) {
            messagesRepository.observeMessages(chatId)
                .onEach { messages ->
                    mutableState.update {
                        it.copy(messages = messages)
                    }
                }
                .collect()
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
