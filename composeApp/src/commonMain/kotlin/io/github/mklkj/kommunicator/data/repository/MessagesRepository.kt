package io.github.mklkj.kommunicator.data.repository

import io.github.mklkj.kommunicator.BuildKonfig
import io.github.mklkj.kommunicator.Chats
import io.github.mklkj.kommunicator.Contacts
import io.github.mklkj.kommunicator.data.api.service.MessagesService
import io.github.mklkj.kommunicator.data.db.Database
import io.github.mklkj.kommunicator.data.db.entity.LocalChat
import io.github.mklkj.kommunicator.data.db.entity.LocalMessage
import io.github.mklkj.kommunicator.data.models.ChatCreateRequest
import io.github.mklkj.kommunicator.data.models.MessageBroadcast
import io.github.mklkj.kommunicator.data.models.MessagePush
import io.github.mklkj.kommunicator.data.models.MessageRequest
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.host
import io.ktor.client.request.port
import io.ktor.http.URLBuilder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
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
        val userId = database.getCurrentUser()?.id ?: error("There is no current user!")

        val chat = database.getChat(chatId)
        if (chat == null) {
            val remoteChat = messagesService.getChat(chatId)
            database.insertChats(userId, listOf(remoteChat))
            return requireNotNull(database.getChat(chatId))
        }
        return chat
    }

    suspend fun refreshChats() {
        val userId = database.getCurrentUser()?.id ?: error("There is no current user!")
        val remoteChats = messagesService.getChats()
        database.insertChats(userId, remoteChats)
    }

    suspend fun refreshMessages(chatId: UUID) {
        val messages = messagesService.getMessages(chatId)
        database.insertMessages(chatId, messages)
    }

    fun observeMessages(chatId: UUID): Flow<List<LocalMessage>> {
        return flow {
            val userId = database.getCurrentUser()?.id ?: error("There is no current user!")
            emitAll(database.observeMessages(chatId, userId))
        }
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
        val session = httpClient.webSocketSession("/ws/chats/$chatId/messages") {
            val url = URLBuilder(BuildKonfig.BASE_URL)
            host = url.host
            port = url.port
        }
        return session
    }

    suspend fun handleReceivedMessage(chatId: UUID, message: MessageBroadcast) {
        database.insertIncomingMessage(chatId, message)
    }
}
