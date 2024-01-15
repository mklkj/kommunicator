package io.github.mklkj.kommunicator.data.service

import io.github.mklkj.kommunicator.data.models.Chat
import io.github.mklkj.kommunicator.data.models.ChatCreateRequest
import io.github.mklkj.kommunicator.data.models.ChatParticipant
import io.github.mklkj.kommunicator.data.repository.ChatRepository
import io.github.mklkj.kommunicator.data.repository.MessageRepository
import kotlinx.uuid.UUID
import org.koin.core.annotation.Singleton

@Singleton
class ChatService(
    private val chatRepository: ChatRepository,
    private val messageRepository: MessageRepository,
) {

    suspend fun addChat(request: ChatCreateRequest) {
        chatRepository.createChat(request)
    }

    suspend fun getChat(chatId: UUID, userId: UUID): Chat? {
        val chat = chatRepository.getChat(chatId, userId) ?: return null
        val lastMessage = messageRepository.getLastMessage(chatId)
        val participants = chatRepository.getParticipants(chatId)
        return Chat(
            id = chat.id,
            avatarUrl = "https://duckduckgo.com/?q=vituperatoribus",
            isUnread = false,
            isActive = false,
            lastMessageTimestamp = lastMessage?.timestamp,
            lastMessage = lastMessage?.content,
            lastMessageAuthor = lastMessage?.authorFirstName,
            name = chat.customName,
            participants = participants.map {
                ChatParticipant(
                    id = it.id,
                    userId = it.userId,
                    username = it.username,
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
            // todo: convert to subquery to fix n+1 problem
            val lastMessage = messageRepository.getLastMessage(chat.id)
            val participants = chatRepository.getParticipants(chat.id)
                .filterNot { it.userId == userId }

            Chat(
                id = chat.id,
                avatarUrl = when (participants.size) {
                    1 -> participants.single().userAvatarUrl
                    else -> "https://i.pravatar.cc/256?=${chat.id}"
                },
                isUnread = false,
                isActive = false,
                lastMessageTimestamp = lastMessage?.timestamp,
                lastMessage = lastMessage?.content,
                lastMessageAuthor = lastMessage?.authorFirstName,
                name = chat.customName.takeIf { !it.isNullOrBlank() } ?: buildString {
                    when (participants.size) {
                        1 -> {
                            append(participants.single().userFirstName)
                            append(" ")
                            append(participants.single().userLastName)
                        }

                        else -> {
                            val names = participants.joinToString(", ") {
                                "${it.userFirstName} ${it.userLastName}"
                            }
                            append(names)
                        }
                    }
                },
                participants = participants.map {
                    ChatParticipant(
                        id = it.id,
                        userId = it.userId,
                        username = it.username,
                        customName = it.customName,
                        firstName = it.userFirstName,
                        lastName = it.userLastName,
                        avatarUrl = it.userAvatarUrl
                    )
                },
            )
        }
    }
}
