package io.github.mklkj.kommunicator.data.service

import io.github.mklkj.kommunicator.data.models.Message
import io.github.mklkj.kommunicator.data.models.MessageEntity
import io.github.mklkj.kommunicator.data.repository.ChatRepository
import io.github.mklkj.kommunicator.data.repository.MessageRepository
import kotlinx.uuid.UUID
import org.koin.core.annotation.Singleton

@Singleton
class MessageService(
    private val chatRepository: ChatRepository,
    private val messageRepository: MessageRepository,
) {

    suspend fun saveMessage(entity: MessageEntity) {
        messageRepository.saveMessage(entity)
    }

    suspend fun getMessages(chatId: UUID, userId: UUID): List<Message> {
        val participants = chatRepository.getParticipants(chatId)

        return messageRepository.getMessages(chatId)
            .map { message ->
                val participant = participants.first { it.userId == message.userId }
                Message(
                    id = message.id,
                    isUserMessage = message.userId == userId,
                    authorId = participant.id,
                    authorCustomName = participant.customName,
                    authorName = when (message.userId) {
                        userId -> "You"
                        else -> {
                            participant.customName ?: participant.let {
                                "${it.userFirstName} ${it.userLastName}"
                            }
                        }
                    },
                    createdAt = message.timestamp,
                    content = message.content,
                )
            }
    }
}
