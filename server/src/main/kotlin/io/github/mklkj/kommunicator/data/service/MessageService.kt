package io.github.mklkj.kommunicator.data.service

import io.github.mklkj.kommunicator.data.models.Message
import io.github.mklkj.kommunicator.data.models.MessageEntity
import io.github.mklkj.kommunicator.data.models.MessageReadEntity
import io.github.mklkj.kommunicator.data.repository.ChatRepository
import io.github.mklkj.kommunicator.data.repository.MessageRepository
import kotlinx.uuid.UUID
import org.koin.core.annotation.Singleton

@Singleton
class MessageService(
    private val chatRepository: ChatRepository,
    private val messageRepository: MessageRepository,
) {

    suspend fun saveMessageReadStatus(entity: MessageReadEntity) {
        messageRepository.saveMessageReadStatus(entity)
    }

    suspend fun saveMessage(entity: MessageEntity) {
        messageRepository.saveMessage(entity)
    }

    suspend fun getChatParticipantId(chatId: UUID, userId: UUID): UUID {
        return chatRepository.getChatParticipantId(chatId, userId)
    }

    suspend fun getMessages(chatId: UUID, userId: UUID): List<Message> {
        return messageRepository.getMessages(chatId, userId)
            .map { message ->
                Message(
                    id = message.id,
                    participantId = message.participantId,
                    createdAt = message.timestamp,
                    content = message.content,
                    readAt = message.readAt,
                )
            }
    }
}
