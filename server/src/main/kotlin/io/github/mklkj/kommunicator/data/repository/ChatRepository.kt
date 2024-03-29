package io.github.mklkj.kommunicator.data.repository

import io.github.mklkj.kommunicator.data.dao.ChatParticipantsDao
import io.github.mklkj.kommunicator.data.dao.ChatsDao
import io.github.mklkj.kommunicator.data.models.ChatCreateRequest
import io.github.mklkj.kommunicator.data.models.ChatEntity
import io.github.mklkj.kommunicator.data.models.ChatParticipantEntity
import io.github.mklkj.kommunicator.data.models.ChatSummaryEntity
import io.github.mklkj.kommunicator.data.models.ParticipantReadEntity
import io.github.mklkj.kommunicator.data.models.UserPushTokenEntity
import kotlinx.uuid.UUID
import org.koin.core.annotation.Singleton

@Singleton
class ChatRepository(
    private val chatsDao: ChatsDao,
    private val chatParticipantsDao: ChatParticipantsDao,
) {

    suspend fun saveParticipantReadStatus(status: ParticipantReadEntity) {
        chatParticipantsDao.saveParticipantReadStatus(status)
    }

    suspend fun getChatsContainingParticipants(participantsIds: List<UUID>): List<UUID> {
        return chatsDao.getChatsContainingParticipants(participantsIds)
    }

    suspend fun createChat(chatCreateRequest: ChatCreateRequest): UUID {
        return chatsDao.createChat(chatCreateRequest)
    }

    suspend fun getChat(chatId: UUID, userId: UUID): ChatEntity? {
        return chatsDao.getChat(chatId, userId)
    }

    suspend fun getChatParticipantId(chatId: UUID, userId: UUID): UUID {
        return chatParticipantsDao.getChatParticipantId(chatId, userId)
    }

    suspend fun getParticipants(chatId: UUID): List<ChatParticipantEntity> {
        return chatParticipantsDao.getParticipants(chatId)
    }

    suspend fun getChats(userId: UUID): List<ChatSummaryEntity> {
        return chatsDao.getChats(userId)
    }

    suspend fun getChatParticipantsPushTokens(chatId: UUID): List<UserPushTokenEntity> {
        return chatParticipantsDao.getChatParticipantsPushTokens(chatId)
    }
}
