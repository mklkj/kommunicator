package io.github.mklkj.kommunicator.data.service

import io.github.mklkj.kommunicator.data.models.Chat
import io.github.mklkj.kommunicator.data.models.ChatCreateRequest
import io.github.mklkj.kommunicator.data.models.ChatParticipant
import io.github.mklkj.kommunicator.data.models.ChatParticipantEntity
import io.github.mklkj.kommunicator.data.models.Message
import io.github.mklkj.kommunicator.data.repository.ChatRepository
import io.github.mklkj.kommunicator.utils.md5
import kotlinx.uuid.UUID
import org.koin.core.annotation.Singleton

@Singleton
class ChatService(
    private val chatRepository: ChatRepository,
) {

    suspend fun addChat(request: ChatCreateRequest): UUID {
        val existingChats = chatRepository.getChatsContainingParticipants(request.participants)
        if (existingChats.isNotEmpty()) {
            return existingChats.first()
        }

        return chatRepository.createChat(request)
    }

    suspend fun getParticipants(chatId: UUID): List<ChatParticipantEntity> {
        return chatRepository.getParticipants(chatId)
    }

    suspend fun getChat(chatId: UUID, userId: UUID): Chat? {
        val chat = chatRepository.getChat(chatId, userId) ?: return null
        val participants = chatRepository.getParticipants(chatId)
        val notCurrentUserParticipants = participants.filterNot { it.userId == userId }

        return Chat(
            id = chat.id,
            avatarUrl = when (notCurrentUserParticipants.size) {
                1 -> "https://gravatar.com/avatar/${md5(notCurrentUserParticipants.first().email)}"
                else -> "https://i.pravatar.cc/256?u=${chat.id}"
            },
            customName = chat.customName.takeIf { !it.isNullOrBlank() } ?: buildString {
                when (notCurrentUserParticipants.size) {
                    1 -> notCurrentUserParticipants.single().let {
                        append(it.userFirstName)
                        append(" ")
                        append(it.userLastName)
                    }

                    else -> {
                        val names = notCurrentUserParticipants.joinToString(", ") {
                            "${it.userFirstName} ${it.userLastName}"
                        }
                        append(names)
                    }
                }
            },
            isUnread = false,
            isActive = false,
            lastMessage = null,
            participants = participants.map {
                ChatParticipant(
                    id = it.id,
                    userId = it.userId,
                    customName = it.customName,
                    firstName = it.userFirstName,
                    lastName = it.userLastName,
                    avatarUrl = it.userAvatarUrl,
                    readAt = it.readAt,
                )
            },
        )
    }

    suspend fun getChats(userId: UUID): List<Chat> {
        return chatRepository.getChats(userId).map { chat ->
            val notCurrentUserParticipants = chat.participants.filterNot { it.userId == userId }
            Chat(
                id = chat.id,
                avatarUrl = when (notCurrentUserParticipants.size) {
                    1 -> "https://gravatar.com/avatar/${md5(notCurrentUserParticipants.first().email)}"
                    else -> "https://i.pravatar.cc/256?u=${chat.id}"
                },
                isUnread = false,
                isActive = false,
                lastMessage = Message(
                    id = chat.lastMessage.messageId,
                    participantId = chat.lastMessage.authorId,
                    createdAt = chat.lastMessage.createdAt,
                    content = chat.lastMessage.content,
                ),
                customName = chat.customName.takeIf { !it.isNullOrBlank() } ?: buildString {
                    when (notCurrentUserParticipants.size) {
                        1 -> {
                            append(notCurrentUserParticipants.single().firstName)
                            append(" ")
                            append(notCurrentUserParticipants.single().lastName)
                        }

                        else -> {
                            val names = notCurrentUserParticipants.joinToString(", ") {
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
                        readAt = it.readAt,
                    )
                },
            )
        }
    }
}
