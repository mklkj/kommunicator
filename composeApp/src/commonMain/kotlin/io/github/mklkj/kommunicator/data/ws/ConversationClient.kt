package io.github.mklkj.kommunicator.data.ws

import co.touchlab.kermit.Logger
import io.github.mklkj.kommunicator.data.models.ChatReadPush
import io.github.mklkj.kommunicator.data.models.MessageBroadcast
import io.github.mklkj.kommunicator.data.models.MessageEvent
import io.github.mklkj.kommunicator.data.models.MessagePush
import io.github.mklkj.kommunicator.data.models.MessageRequest
import io.github.mklkj.kommunicator.data.models.ParticipantReadBroadcast
import io.github.mklkj.kommunicator.data.models.TypingBroadcast
import io.github.mklkj.kommunicator.data.models.TypingPush
import io.github.mklkj.kommunicator.data.repository.MessagesRepository
import io.github.mklkj.kommunicator.getDeserialized
import io.github.mklkj.kommunicator.utils.throttleFirst
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.serialization.WebsocketContentConverter
import io.ktor.websocket.Frame
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.uuid.UUID
import org.koin.core.annotation.Factory
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

private const val TAG = "ConversationClient"
private val TYPING_SAMPLE_DURATION = 3.seconds
const val RECONNECTION_WAIT_SECONDS = 3

@Factory
class ConversationClient(
    private val messagesRepository: MessagesRepository,
    private val websocketConverter: WebsocketContentConverter,
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val errorExceptionHandler = CoroutineExceptionHandler { _, it ->
        Logger.e("Error occurred in conversation client", it)
    }

    private var chatSession: DefaultClientWebSocketSession? = null
    private var chatId: UUID? = null
    private val typingChannel = Channel<Boolean>()
    val typingParticipants = MutableStateFlow<Map<UUID, Instant>>(emptyMap())
    val connectionStatus = MutableStateFlow<ConnectionStatus>(ConnectionStatus.NotConnected)

    private val isConnectionShouldRetry = Channel<Boolean>()
    private var connectJob: Job? = null

    init {
        initializeTypingObserver()
        initializeTypingStaleTimer()
    }

    fun connect(chatId: UUID) {
        Logger.withTag(TAG).i("Connecting to websocket on $chatId chat")
        this.chatId = chatId
        initializeConnectionRetryingObserver(chatId)
        connectAndObserveIncomingMessages(chatId)
    }

    private fun initializeConnectionRetryingObserver(chatId: UUID) {
        scope.launch(errorExceptionHandler) {
            isConnectionShouldRetry.consumeAsFlow()
                .throttleFirst(1.seconds)
                .onEach { shouldReconnect ->
                    if (shouldReconnect) {
                        val previousError = connectionStatus.value as? ConnectionStatus.Error
                        var retryIn = previousError?.retryIn ?: RECONNECTION_WAIT_SECONDS
                        while (retryIn > 0) {
                            val updatedState = ConnectionStatus.Error(
                                error = previousError?.error,
                                retryIn = retryIn,
                            )
                            connectionStatus.update { updatedState }
                            retryIn--
                            delay(1.seconds)
                        }
                        connectAndObserveIncomingMessages(chatId)
                    }
                }
                .collect()
        }
    }

    private fun connectAndObserveIncomingMessages(chatId: UUID) {
        connectJob?.cancel()
        connectJob = scope.launch(errorExceptionHandler) {
            connectionStatus.update { ConnectionStatus.Connecting }
            chatSession = runCatching { messagesRepository.getChatSession(chatId) }
                .onFailure { error ->
                    connectionStatus.update { ConnectionStatus.Error(error) }
                    isConnectionShouldRetry.send(true)
                }
                .onSuccess {
                    connectionStatus.update { ConnectionStatus.Connected }
                    isConnectionShouldRetry.send(false)
                }
                .getOrThrow()

            chatSession?.incoming?.consumeAsFlow()
                ?.onEach { frame ->
                    if (frame is Frame.Text) {
                        handleIncomingFrame(frame)
                    }
                }
                ?.catch { error ->
                    chatSession = null
                    isConnectionShouldRetry.send(true)
                    connectionStatus.update { ConnectionStatus.Error(error) }
                }
                ?.collect()
        }
    }

    private suspend fun handleIncomingFrame(frame: Frame) {
        when (val messageEvent = frame.getDeserialized<MessageEvent>(websocketConverter)) {
            is MessageBroadcast -> {
                Logger.withTag(TAG).i("Receive message from: ${messageEvent.participantId}")
                messagesRepository.handleReceivedMessage(chatId ?: return, messageEvent)

                // mark chat as read when message arrive
                onChatRead()
            }

            is ParticipantReadBroadcast -> {
                Logger.withTag(TAG).i("Receive message read from: ${messageEvent.participantId}")
                messagesRepository.handleMessageReadStatus(messageEvent)
            }

            is TypingBroadcast -> {
                Logger.withTag(TAG).i("Receive typing from: ${messageEvent.participantId}")

                typingParticipants.update { typingMap ->
                    when {
                        messageEvent.isStop -> typingMap.filterKeys { it != messageEvent.participantId }
                        else -> typingMap + mapOf(messageEvent.participantId to Clock.System.now())
                    }
                }
            }

            // not implemented on server
            is MessagePush -> Unit
            is TypingPush -> Unit
            is ChatReadPush -> Unit
        }
    }

    fun sendMessage(message: MessageRequest) {
        val chatId = chatId ?: return

        scope.launch(errorExceptionHandler) {
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
                    onTyping(isEmptyMessage = true)
                }
            }
        }
    }

    private fun initializeTypingObserver() {
        scope.launch(errorExceptionHandler) {
            typingChannel.consumeAsFlow()
                .throttleFirst(TYPING_SAMPLE_DURATION)
                .onEach {
                    chatSession?.sendSerialized<MessageEvent>(TypingPush(it))
                }
                .catch { Logger.e("Error during typing", it) }
                .collect()
        }
    }

    private fun initializeTypingStaleTimer() {
        scope.launch(errorExceptionHandler) {
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

    fun onTyping(isEmptyMessage: Boolean) {
        typingChannel.trySend(isEmptyMessage)
    }

    fun onChatRead() {
        scope.launch {
            Logger.withTag(TAG).i("Marking chat as read")
            val now = Clock.System.now()
            val lastMessageTimestamp = messagesRepository
                .getLastMessageTimestamp(chatId ?: return@launch)
                ?.plus(1.milliseconds) // hack to fix timestamp comparison in sql query

            val readTimestamp = when {
                lastMessageTimestamp != null && lastMessageTimestamp > now -> lastMessageTimestamp
                else -> now
            }

            Logger.i("Mark $chatId as read on $readTimestamp (now: $now, lastMessage: $lastMessageTimestamp)")
            chatSession?.sendSerialized<MessageEvent>(ChatReadPush(readAt = readTimestamp))
        }
    }

    fun onDispose() {
        chatSession?.cancel()
        scope.cancel()
    }
}
