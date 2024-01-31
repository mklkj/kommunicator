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
            .filterNot { it.userId == userId }

        return ChatDetails(
            id = chat.id,
            avatarUrl = "https://i.pravatar.cc/256?u=${chat.id}",
            name = chat.customName ?: buildString {
                val name = participants.first().let {
                    it.customName ?: it.userFirstName
                }
                append(name)
            },
            messages = messages.map { message ->
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
                            } ?: chat.customName
                        }
                    }.orEmpty(),
                    timestamp = message.timestamp,
                    content = message.content,
                )
            },
            participants = participants.map {
                ChatParticipant(
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
            val participants = chat.participants.filterNot { it.userId == userId }
            Chat(
                id = chat.id,
                avatarUrl = when (participants.size) {
                    1 -> "https://gravatar.com/avatar/${md5(participants.single().email)}"
                    else -> "https://i.pravatar.cc/256?u=${chat.id}"
                },
                isUnread = false,
                isActive = false,
                lastMessageTimestamp = chat.lastMessageCreatedAt,
                lastMessage = chat.lastMessageContent,
                lastMessageAuthor = chat.lastMessageAuthorFirstName,
                name = chat.customName.takeIf { !it.isNullOrBlank() } ?: buildString {
                    when (participants.size) {
                        1 -> {
                            append(participants.single().firstName)
                            append(" ")
                            append(participants.single().lastName)
                        }

                        else -> {
                            val names = participants.joinToString(", ") {
                                "${it.firstName} ${it.lastName}"
                            }
                            append(names)
                        }
                    }
                },
                participants = participants.map {
                    ChatParticipant(
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
