package io.github.mklkj.kommunicator.data.service

import io.github.mklkj.kommunicator.data.models.Message
import io.github.mklkj.kommunicator.data.models.MessageEntity
import io.github.mklkj.kommunicator.data.models.ParticipantReadEntity
import io.github.mklkj.kommunicator.data.repository.ChatRepository
import io.github.mklkj.kommunicator.data.repository.MessageRepository
import kotlinx.uuid.UUID
import org.koin.core.annotation.Singleton

@Singleton
class MessageService(
    private val chatRepository: ChatRepository,
    private val messageRepository: MessageRepository,
) {

    suspend fun saveParticipantReadStatus(entity: ParticipantReadEntity) {
        chatRepository.saveParticipantReadStatus(entity)
    }

    suspend fun saveMessage(entity: MessageEntity) {
        messageRepository.saveMessage(entity)
    }

    suspend fun getChatParticipantId(chatId: UUID, userId: UUID): UUID {
        return chatRepository.getChatParticipantId(chatId, userId)
    }

    suspend fun getMessages(chatId: UUID): List<Message> {
        return messageRepository.getMessages(chatId)
            .map { message ->
                Message(
                    id = message.id,
                    participantId = message.participantId,
                    createdAt = message.timestamp,
                    content = message.content,
                )
            }
    }
}
