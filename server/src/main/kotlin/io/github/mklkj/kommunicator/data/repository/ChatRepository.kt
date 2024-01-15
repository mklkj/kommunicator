package io.github.mklkj.kommunicator.data.repository

import io.github.mklkj.kommunicator.data.dao.ChatParticipantsDao
import io.github.mklkj.kommunicator.data.dao.ChatsDao
import io.github.mklkj.kommunicator.data.models.ChatCreateRequest
import io.github.mklkj.kommunicator.data.models.ChatEntity
import io.github.mklkj.kommunicator.data.models.ChatParticipantEntity
import kotlinx.uuid.UUID
import org.koin.core.annotation.Singleton

@Singleton
class ChatRepository(
    private val chatsDao: ChatsDao,
    private val chatParticipantsDao: ChatParticipantsDao,
) {

    suspend fun createChat(chatCreateRequest: ChatCreateRequest) {
        chatsDao.createChat(chatCreateRequest)
    }

    suspend fun getChat(chatId: UUID, userId: UUID): ChatEntity? {
        return chatsDao.getChat(chatId, userId)
    }

    suspend fun getParticipants(chatId: UUID): List<ChatParticipantEntity> {
        return chatParticipantsDao.getParticipants(chatId)
    }

    suspend fun getChats(userId: UUID): List<ChatEntity> {
        return chatsDao.getChats(userId)
    }
}
