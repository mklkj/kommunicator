package io.github.mklkj.kommunicator.data.service

import io.github.mklkj.kommunicator.data.models.Chat
import io.github.mklkj.kommunicator.data.models.ChatCreateRequest
import io.github.mklkj.kommunicator.data.models.ChatParticipant
import io.github.mklkj.kommunicator.data.models.ChatParticipantEntity
import io.github.mklkj.kommunicator.data.models.Message
import io.github.mklkj.kommunicator.data.repository.ChatRepository
import io.github.mklkj.kommunicator.utils.AvatarHelper
import kotlinx.uuid.UUID
import org.koin.core.annotation.Singleton

@Singleton
class ChatService(
    private val chatRepository: ChatRepository,
    private val avatarHelper: AvatarHelper,
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

        val chatName = chat.customName.takeIf { !it.isNullOrBlank() } ?: buildString {
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
        }

        return Chat(
            id = chat.id,
            avatarUrl = when (notCurrentUserParticipants.size) {
                1 -> avatarHelper.getUserAvatar(
                    firstName = notCurrentUserParticipants.first().userFirstName,
                    lastName = notCurrentUserParticipants.first().userLastName,
                    customName = notCurrentUserParticipants.first().customName,
                )

                else -> avatarHelper.getGroupAvatar(chatName)
            },
            customName = chatName,
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

            val chatName = chat.customName.takeIf { !it.isNullOrBlank() } ?: buildString {
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
            }
            Chat(
                id = chat.id,
                avatarUrl = when (notCurrentUserParticipants.size) {
                    1 -> avatarHelper.getUserAvatar(
                        firstName = notCurrentUserParticipants.first().firstName,
                        lastName = notCurrentUserParticipants.first().lastName,
                        customName = notCurrentUserParticipants.first().customName,
                    )

                    else -> avatarHelper.getGroupAvatar(chatName)
                },
                lastMessage = Message(
                    id = chat.lastMessage.messageId,
                    participantId = chat.lastMessage.authorId,
                    createdAt = chat.lastMessage.createdAt,
                    content = chat.lastMessage.content,
                ),
                customName = chatName,
                participants = chat.participants.map {
                    ChatParticipant(
                        id = it.id,
                        userId = it.userId,
                        customName = it.customName,
                        firstName = it.firstName,
                        lastName = it.lastName,
                        avatarUrl = avatarHelper.getUserAvatar(
                            firstName = it.firstName,
                            lastName = it.lastName,
                            customName = it.customName,
                        ),
                        readAt = it.readAt,
                    )
                },
            )
        }
    }
}
