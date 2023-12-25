package io.github.mklkj.kommunicator.data.api.service

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path
import io.github.mklkj.kommunicator.data.models.Chat
import io.github.mklkj.kommunicator.data.models.ChatDetails
import kotlinx.uuid.UUID

interface MessagesService {

    @GET("chats")
    suspend fun getChats(): List<Chat>

    @GET("chat/{id}")
    suspend fun getChat(@Path("id") id: UUID): ChatDetails
}
