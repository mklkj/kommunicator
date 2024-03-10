package io.github.mklkj.kommunicator.data.repository

import io.github.mklkj.kommunicator.BuildKonfig
import io.github.mklkj.kommunicator.Chats
import io.github.mklkj.kommunicator.Contacts
import io.github.mklkj.kommunicator.Participants
import io.github.mklkj.kommunicator.SelectParticipantsWithLastReadMessage
import io.github.mklkj.kommunicator.data.api.service.MessagesService
import io.github.mklkj.kommunicator.data.db.Database
import io.github.mklkj.kommunicator.data.db.entity.LocalChat
import io.github.mklkj.kommunicator.data.db.entity.LocalMessage
import io.github.mklkj.kommunicator.data.models.ChatCreateRequest
import io.github.mklkj.kommunicator.data.models.MessageBroadcast
import io.github.mklkj.kommunicator.data.models.MessagePush
import io.github.mklkj.kommunicator.data.models.MessageRequest
import io.github.mklkj.kommunicator.data.models.ParticipantReadBroadcast
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.http.path
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.uuid.UUID
import org.koin.core.annotation.Singleton

@Singleton
class MessagesRepository(
    private val messagesService: MessagesService,
    private val httpClient: HttpClient,
    private val database: Database,
) {

    suspend fun createChat(contacts: List<Contacts>): UUID {
        return messagesService.createChat(
            ChatCreateRequest(
                customName = null,
                participants = contacts.map {
                    it.contactUserId
                },
            )
        ).chatId
    }

    fun observeChats(): Flow<List<LocalChat>> {
        return flow {
            val userId = database.getCurrentUser()?.id ?: error("There is no current user!")
            if (database.getChats(userId).isEmpty()) {
                refreshChats()
            }
            emitAll(database.observeChats(userId).map { chats ->
                chats.sortedByDescending { it.lastMessage?.createdAt }
            })
        }
    }

    suspend fun getChat(chatId: UUID): Chats {
        val chat = database.getChat(chatId)
        if (chat == null) {
            refreshChat(chatId)
            return requireNotNull(database.getChat(chatId))
        }
        return chat
    }

    suspend fun refreshChat(chatId: UUID) {
        val userId = database.getCurrentUser()?.id ?: error("There is no current user!")
        val remoteChat = messagesService.getChat(chatId)
        database.insertChats(userId, listOf(remoteChat))
    }

    suspend fun refreshMessages(chatId: UUID) {
        val messages = messagesService.getMessages(chatId)
        database.insertMessages(chatId, messages)
    }

    suspend fun refreshChats() {
        val userId = database.getCurrentUser()?.id ?: error("There is no current user!")
        val remoteChats = messagesService.getChats()
        database.insertChats(userId, remoteChats)
    }

    fun observeMessages(chatId: UUID): Flow<List<LocalMessage>> {
        return flow {
            val userId = database.getCurrentUser()?.id ?: error("There is no current user!")
            emitAll(database.observeMessages(chatId, userId))
        }
    }

    suspend fun getLastMessageTimestamp(chatId: UUID): Instant? {
        return database.getLastMessageTimestamp(chatId)
    }

    fun observeParticipants(chatId: UUID): Flow<List<Participants>> {
        return database.observeParticipants(chatId)
    }

    fun observeParticipantsLastRead(chatId: UUID): Flow<List<SelectParticipantsWithLastReadMessage>> {
        return database.observeParticipantsLastRead(chatId)
    }

    suspend fun sendMessage(chatId: UUID, message: MessageRequest) {
        saveMessageToSend(chatId, message)
        messagesService.sendMessage(
            chatId = chatId,
            message = MessagePush(
                id = message.id,
                content = message.content,
            )
        )
    }

    suspend fun saveMessageToSend(chatId: UUID, message: MessageRequest) {
        val userId = database.getCurrentUser()?.id ?: error("There is no current user!")
        val authorId = database.getChatParticipant(chatId, userId)?.id
            ?: error("Current user is not a participant in that chat!")

        database.insertUserMessage(
            chatId = chatId,
            authorId = authorId,
            messageRequest = message,
        )
    }

    suspend fun getChatSession(chatId: UUID): DefaultClientWebSocketSession {
        val url = (URLBuilder(BuildKonfig.BASE_URL)).apply {
            protocol = when (protocol) {
                URLProtocol.HTTPS -> URLProtocol.WSS
                else -> URLProtocol.WS
            }
            path("/ws/chats/$chatId/messages")
        }
        return runCatching {
            httpClient.webSocketSession(url.buildString())
        }.recover {
            // refresh JWT using "normal" connection
            // workaround for https://youtrack.jetbrains.com/issue/KTOR-4852/Question-Ktor-client-Refresh-token-and-retry
            messagesService.getChat(chatId)

            httpClient.webSocketSession(url.buildString())
        }.getOrThrow()
    }

    suspend fun handleReceivedMessage(chatId: UUID, message: MessageBroadcast) {
        database.insertIncomingMessage(chatId, message)
    }

    suspend fun handleMessageReadStatus(readStatus: ParticipantReadBroadcast) {
        database.updateParticipantReadAt(readStatus)
    }
}
