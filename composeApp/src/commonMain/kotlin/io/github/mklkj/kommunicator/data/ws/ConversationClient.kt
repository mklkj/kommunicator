package io.github.mklkj.kommunicator.data.ws

import co.touchlab.kermit.Logger
import io.github.mklkj.kommunicator.data.models.Message
import io.github.mklkj.kommunicator.data.models.MessageRequest
import io.github.mklkj.kommunicator.data.repository.MessagesRepository
import io.github.mklkj.kommunicator.getDeserialized
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.serialization.WebsocketContentConverter
import io.ktor.websocket.Frame
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.uuid.UUID
import org.koin.core.annotation.Factory

private const val TAG = "ConversationClient"

@Factory
class ConversationClient(
    private val messagesRepository: MessagesRepository,
    private val websocketConverter: WebsocketContentConverter,
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var chatSession: DefaultClientWebSocketSession? = null
    private var chatId: UUID? = null

    fun connect(chatId: UUID, onFailure: (Throwable) -> Unit) {
        Logger.withTag(TAG).i("Connecting to websocket on $chatId chat")
        this.chatId = chatId
        scope.launch {
            runCatching {
                chatSession = messagesRepository.getChatSession(chatId)
                observeIncomingMessages()
            }.onFailure(onFailure)
        }
    }

    private suspend fun observeIncomingMessages() {
        for (frame in chatSession?.incoming ?: return) {
            if (frame !is Frame.Text) continue

            val message = frame.getDeserialized<Message>(websocketConverter)
            Logger.withTag(TAG).i("Receive message from: ${message.authorId}")
            messagesRepository.handleReceivedMessage(chatId ?: return, message)
        }
    }

    fun sendMessage(chatId: UUID, message: MessageRequest) {
        scope.launch {
            when (chatSession) {
                null -> {
                    Logger.withTag(TAG).i("Sending message with REST API")
                    messagesRepository.sendMessage(chatId, message)
                }
                else -> {
                    Logger.withTag(TAG).i("Sending message with websockets")
                    messagesRepository.saveMessageToSend(chatId, message)
                    chatSession?.sendSerialized(message)
                }
            }
        }
    }

    fun onDispose() {
        chatSession?.cancel()
    }
}
