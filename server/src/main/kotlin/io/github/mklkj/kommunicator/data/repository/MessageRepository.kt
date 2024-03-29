package io.github.mklkj.kommunicator.data.repository

import io.github.mklkj.kommunicator.data.dao.MessagesDao
import io.github.mklkj.kommunicator.data.models.MessageEntity
import kotlinx.uuid.UUID
import org.koin.core.annotation.Singleton

@Singleton
class MessageRepository(
    private val messagesDao: MessagesDao,
) {

    suspend fun saveMessage(entity: MessageEntity) {
        messagesDao.addMessage(entity)
    }

    suspend fun getMessages(chatId: UUID): List<MessageEntity> {
        return messagesDao.getMessages(chatId)
    }

    suspend fun getMessage(messageId: UUID): MessageEntity {
        return messagesDao.getMessage(messageId)
    }
}
