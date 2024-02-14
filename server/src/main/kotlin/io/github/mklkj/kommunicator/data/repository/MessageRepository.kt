package io.github.mklkj.kommunicator.data.repository

import io.github.mklkj.kommunicator.data.dao.MessagesDao
import io.github.mklkj.kommunicator.data.dao.MessagesReadDao
import io.github.mklkj.kommunicator.data.models.MessageEntity
import io.github.mklkj.kommunicator.data.models.MessageReadEntity
import kotlinx.uuid.UUID
import org.koin.core.annotation.Singleton

@Singleton
class MessageRepository(
    private val messagesDao: MessagesDao,
    private val messagesReadDao: MessagesReadDao,
) {

    suspend fun saveMessageReadStatus(entity: MessageReadEntity) {
        messagesReadDao.saveMesssageReadStatus(entity)
    }

    suspend fun saveMessage(entity: MessageEntity) {
        messagesDao.addMessage(entity)
    }

    suspend fun getMessages(chatId: UUID, userId: UUID): List<MessageEntity> {
        return messagesDao.getMessages(chatId, userId)
    }

    suspend fun getMessage(messageId: UUID): MessageEntity {
        return messagesDao.getMessage(messageId)
    }
}
