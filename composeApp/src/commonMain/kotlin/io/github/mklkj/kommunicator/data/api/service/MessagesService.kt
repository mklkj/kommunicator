package io.github.mklkj.kommunicator.data.api.service

import de.jensklingenberg.ktorfit.http.GET
import io.github.mklkj.kommunicator.data.models.Chat

interface MessagesService {

    @GET("chats")
    suspend fun getChats(): List<Chat>
}
