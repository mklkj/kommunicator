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
            .filterNot { it.userId == userId }

        return messageRepository.getMessages(chatId)
            .map { message ->
                Message(
                    id = message.id,
                    isUserMessage = message.userId == userId,
                    authorId = message.userId,
                    authorName = when (message.userId) {
                        userId -> "You"
                        else -> {
                            val participant = participants
                                .find { it.userId == message.userId }
                            participant?.customName ?: participant?.let {
                                "${it.userFirstName} ${it.userLastName}"
                            }
                        }
                    }.orEmpty(),
                    timestamp = message.timestamp,
                    content = message.content,
                )
            }
    }
}
