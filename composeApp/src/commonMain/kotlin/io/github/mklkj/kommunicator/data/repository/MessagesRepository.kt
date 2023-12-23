package io.github.mklkj.kommunicator.data.repository

import io.github.mklkj.kommunicator.data.api.service.MessagesService
import io.github.mklkj.kommunicator.data.models.Chat
import io.github.mklkj.kommunicator.data.models.ChatDetails
import org.koin.core.annotation.Singleton

@Singleton
class MessagesRepository(
    private val messagesService: MessagesService,
) {

    suspend fun getChats(): List<Chat> {
        return messagesService.getChats()
    }

    suspend fun getChatDetails(id: String): ChatDetails {
        return messagesService.getChat(id)
    }
}
