package io.github.mklkj.kommunicator.data.ws

import co.touchlab.kermit.Logger
import io.github.mklkj.kommunicator.data.models.MessageBroadcast
import io.github.mklkj.kommunicator.data.models.MessageEvent
import io.github.mklkj.kommunicator.data.models.MessagePush
import io.github.mklkj.kommunicator.data.models.MessageRequest
import io.github.mklkj.kommunicator.data.models.TypingBroadcast
import io.github.mklkj.kommunicator.data.models.TypingPush
import io.github.mklkj.kommunicator.data.repository.MessagesRepository
import io.github.mklkj.kommunicator.getDeserialized
import io.github.mklkj.kommunicator.utils.throttleFirst
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.serialization.WebsocketContentConverter
import io.ktor.websocket.Frame
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.uuid.UUID
import org.koin.core.annotation.Factory
import kotlin.time.Duration.Companion.seconds

private const val TAG = "ConversationClient"
private val TYPING_SAMPLE_DURATION = 3.seconds

@Factory
class ConversationClient(
    private val messagesRepository: MessagesRepository,
    private val websocketConverter: WebsocketContentConverter,
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var chatSession: DefaultClientWebSocketSession? = null
    private var chatId: UUID? = null
    private val typingChannel = Channel<Unit>()
    val typingParticipants = MutableStateFlow<Map<UUID, Instant>>(emptyMap())

    fun connect(chatId: UUID, onFailure: (Throwable) -> Unit) {
        Logger.withTag(TAG).i("Connecting to websocket on $chatId chat")
        this.chatId = chatId
        scope.launch {
            runCatching {
                chatSession = messagesRepository.getChatSession(chatId)
                initializeTypingObserver()
                initializeTypingStaleTimer()
                observeIncomingMessages()
            }.onFailure(onFailure)
        }
    }

    private suspend fun observeIncomingMessages() {
        for (frame in chatSession?.incoming ?: return) {
            if (frame !is Frame.Text) continue

            when (val messageEvent = frame.getDeserialized<MessageEvent>(websocketConverter)) {
                is MessageBroadcast -> {
                    Logger.withTag(TAG).i("Receive message from: ${messageEvent.participantId}")
                    messagesRepository.handleReceivedMessage(chatId ?: return, messageEvent)
                }

                is TypingBroadcast -> {
                    Logger.withTag(TAG).i("Receive typing from: ${messageEvent.participantId}")

                    typingParticipants.update {
                        it + mapOf(messageEvent.participantId to Clock.System.now())
                    }
                }

                // not implemented on server
                is MessagePush -> Unit
                TypingPush -> Unit
            }
        }
    }

    fun sendMessage(message: MessageRequest) {
        val chatId = chatId ?: return

        // todo: add error handler!
        scope.launch {
            when (chatSession) {
                null -> {
                    Logger.withTag(TAG).i("Sending message with REST API")
                    messagesRepository.sendMessage(chatId, message)
                }

                else -> {
                    Logger.withTag(TAG).i("Sending message with websockets")
                    messagesRepository.saveMessageToSend(chatId, message)
                    chatSession?.sendSerialized<MessageEvent>(
                        MessagePush(
                            id = message.id,
                            content = message.content,
                        )
                    )
                }
            }
        }
    }

    private fun initializeTypingObserver() {
        scope.launch {
            typingChannel.consumeAsFlow()
                .throttleFirst(TYPING_SAMPLE_DURATION.inWholeMilliseconds)
                .onEach {
                    chatSession?.sendSerialized<MessageEvent>(TypingPush)
                }
                .collect()
        }
    }

    private fun initializeTypingStaleTimer() {
        scope.launch {
            while (true) {
                typingParticipants.update { participants ->
                    participants.filterValues { lastUpdate ->
                        Clock.System.now().minus(lastUpdate) <= TYPING_SAMPLE_DURATION
                    }
                }
                delay(TYPING_SAMPLE_DURATION)
            }
        }
    }

    fun onTyping() {
        typingChannel.trySend(Unit)
    }

    fun onDispose() {
        chatSession?.cancel()
    }
}
