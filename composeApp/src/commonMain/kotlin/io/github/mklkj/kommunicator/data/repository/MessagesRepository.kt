package io.github.mklkj.kommunicator.data.repository

import io.github.mklkj.kommunicator.Chats
import io.github.mklkj.kommunicator.data.api.service.MessagesService
import io.github.mklkj.kommunicator.data.db.Database
import io.github.mklkj.kommunicator.data.db.entity.LocalContact
import io.github.mklkj.kommunicator.data.models.Chat
import io.github.mklkj.kommunicator.data.models.ChatCreateRequest
import io.github.mklkj.kommunicator.data.models.ChatDetails
import io.github.mklkj.kommunicator.data.models.MessageRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.uuid.UUID
import org.koin.core.annotation.Singleton

@Singleton
class MessagesRepository(
    private val messagesService: MessagesService,
    private val database: Database,
) {

    suspend fun createChat(chatId: UUID, contacts: List<LocalContact>) {
        messagesService.createChat(
            ChatCreateRequest(
                chatId = chatId,
                customName = null,
                participants = contacts.map {
                    it.contactUserId
                },
            )
        )
    }

    fun observeChats(): Flow<List<Chats>> {
        return flow {
            val userId = database.getCurrentUser()?.id ?: error("There is no current user!")
            if (database.getChats(userId).isEmpty()) {
                refreshChats()
            }
            emitAll(database.observeChats(userId))
        }
    }

    suspend fun refreshChats() {
        val userId = database.getCurrentUser()?.id ?: error("There is no current user!")
        val remoteChats = messagesService.getChats()
        // todo
//        database.insertChats(userId, remoteChats)
    }

    suspend fun getChats(): List<Chat> {
        return messagesService.getChats()
    }

    suspend fun getChatDetails(id: UUID): ChatDetails {
        return messagesService.getChat(id)
    }

    suspend fun sendMessage(chatId: UUID, message: MessageRequest) {
        messagesService.sendMessage(chatId, message)
    }
}
