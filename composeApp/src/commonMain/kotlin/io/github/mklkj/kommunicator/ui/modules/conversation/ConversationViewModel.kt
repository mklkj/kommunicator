package io.github.mklkj.kommunicator.ui.modules.conversation

import io.github.mklkj.kommunicator.data.models.Message
import io.github.mklkj.kommunicator.data.models.MessageRequest
import io.github.mklkj.kommunicator.data.repository.MessagesRepository
import io.github.mklkj.kommunicator.getDeserialized
import io.github.mklkj.kommunicator.ui.base.BaseViewModel
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.websocket.Frame
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json
import kotlinx.uuid.UUID
import org.koin.core.annotation.Factory

@Factory
class ConversationViewModel(
    private val messagesRepository: MessagesRepository,
    private val json: Json,
) : BaseViewModel<ConversationState>(ConversationState()) {

    private var chatSession: DefaultClientWebSocketSession? = null

    fun loadData(chatId: UUID) {
        loadChatDetails(chatId)
        observeMessages(chatId)
        refreshChat(chatId)
        initializeWebSocketSession(chatId)
    }

    private fun initializeWebSocketSession(chatId: UUID) {
        launch("chat_session_$chatId", cancelExisting = false) {
            val session = messagesRepository.getChatSession(chatId)
            chatSession = session

            for (frame in session.incoming) {
                if (frame !is Frame.Text) continue

                val message = frame.getDeserialized<Message>(
                    KotlinxWebsocketSerializationConverter(json)
                )
                messagesRepository.handleReceivedMessage(chatId, message)
            }
        }
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

    fun sendMessage(chatId: UUID, message: MessageRequest) {
        launch("chat_send_message") {
            mutableState.update { it.copy(isLoading = true) }

            when (chatSession) {
                null -> messagesRepository.sendMessage(chatId, message)
                else -> chatSession?.sendSerialized(message)
            }

            mutableState.update { it.copy(isLoading = false) }
        }
    }
}
