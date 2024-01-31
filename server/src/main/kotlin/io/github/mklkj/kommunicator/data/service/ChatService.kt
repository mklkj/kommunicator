package io.github.mklkj.kommunicator.data.service

import io.github.mklkj.kommunicator.data.models.Chat
import io.github.mklkj.kommunicator.data.models.ChatCreateRequest
import io.github.mklkj.kommunicator.data.models.ChatDetails
import io.github.mklkj.kommunicator.data.models.ChatParticipant
import io.github.mklkj.kommunicator.data.models.Message
import io.github.mklkj.kommunicator.data.repository.ChatRepository
import io.github.mklkj.kommunicator.data.repository.MessageRepository
import io.github.mklkj.kommunicator.utils.md5
import kotlinx.uuid.UUID
import org.koin.core.annotation.Singleton

@Singleton
class ChatService(
    private val chatRepository: ChatRepository,
    private val messageRepository: MessageRepository,
) {

    suspend fun addChat(request: ChatCreateRequest): UUID {
        val existingChats = chatRepository.getChatsContainingParticipants(request.participants)
        if (existingChats.isNotEmpty()) {
            return existingChats.first()
        }

        return chatRepository.createChat(request)
    }

    suspend fun getChat(chatId: UUID, userId: UUID): ChatDetails? {
        val chat = chatRepository.getChat(chatId, userId) ?: return null
        val messages = messageRepository.getMessages(chatId)
        val participants = chatRepository.getParticipants(chatId)
        val notCurrentUserParticipants = participants.filterNot { it.userId == userId }

        return ChatDetails(
            id = chat.id,
            avatarUrl = "https://i.pravatar.cc/256?u=${chat.id}",
            name = chat.customName ?: buildString {
                val name = notCurrentUserParticipants.first().let {
                    it.customName ?: it.userFirstName
                }
                append(name)
            },
            messages = messages.map { message ->
                val participant = participants.find { it.userId == message.userId }
                Message(
                    id = message.id,
                    isUserMessage = message.userId == userId,
                    authorId = message.userId,
                    authorCustomName = participant?.customName,
                    authorName = when (message.userId) {
                        userId -> "You"
                        else -> {
                            participant?.customName ?: participant?.let {
                                "${it.userFirstName} ${it.userLastName}"
                            } ?: chat.customName
                        }
                    }.orEmpty(),
                    createdAt = message.timestamp,
                    content = message.content,
                )
            },
            participants = participants.map {
                ChatParticipant(
                    id = it.id,
                    userId = it.userId,
                    customName = it.customName,
                    firstName = it.userFirstName,
                    lastName = it.userLastName,
                    avatarUrl = it.userAvatarUrl
                )
            }
        )
    }

    suspend fun getChats(userId: UUID): List<Chat> {
        return chatRepository.getChats(userId).map { chat ->
            val notCurrentUserParticipants = chat.participants.filterNot { it.userId == userId }
            Chat(
                id = chat.id,
                avatarUrl = when (chat.participants.size) {
                    2 -> "https://gravatar.com/avatar/${md5(notCurrentUserParticipants.first().email)}"
                    else -> "https://i.pravatar.cc/256?u=${chat.id}"
                },
                isUnread = false,
                isActive = false,
                lastMessage = Message(
                    id = chat.lastMessage.messageId,
                    isUserMessage = chat.lastMessage.authorId == userId,
                    authorId = chat.lastMessage.authorId,
                    authorName = "${chat.lastMessage.authorFirstName} ${chat.lastMessage.authorLastName}",
                    authorCustomName = chat.lastMessage.authorCustomName,
                    createdAt = chat.lastMessage.createdAt,
                    content = chat.lastMessage.content,
                ),
                customName = chat.customName.takeIf { !it.isNullOrBlank() } ?: buildString {
                    when (chat.participants.size) {
                        2 -> {
                            append(notCurrentUserParticipants.single().firstName)
                            append(" ")
                            append(notCurrentUserParticipants.single().lastName)
                        }

                        else -> {
                            val names = chat.participants.joinToString(", ") {
                                "${it.firstName} ${it.lastName}"
                            }
                            append(names)
                        }
                    }
                },
                participants = chat.participants.map {
                    ChatParticipant(
                        id = it.id,
                        userId = it.userId,
                        customName = it.customName,
                        firstName = it.firstName,
                        lastName = it.lastName,
                        avatarUrl = "https://gravatar.com/avatar/${md5(it.email)}",
                    )
                },
            )
        }
    }
}
