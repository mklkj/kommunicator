package io.github.mklkj.kommunicator.data.service

import io.github.mklkj.kommunicator.data.models.MessageEntity
import io.github.mklkj.kommunicator.data.repository.MessageRepository
import kotlinx.uuid.UUID
import org.koin.core.annotation.Singleton

@Singleton
class MessageService(
    private val messageRepository: MessageRepository,
) {

    suspend fun saveMessage(entity: MessageEntity) {
        messageRepository.saveMessage(entity)
    }

    suspend fun getMessages(chatId: UUID): List<MessageEntity> {
        return messageRepository.getMessages(chatId)
    }
}
