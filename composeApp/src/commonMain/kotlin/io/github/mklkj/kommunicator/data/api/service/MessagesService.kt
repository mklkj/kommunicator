package io.github.mklkj.kommunicator.data.api.service

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import io.github.mklkj.kommunicator.data.models.Chat
import io.github.mklkj.kommunicator.data.models.ChatCreateRequest
import io.github.mklkj.kommunicator.data.models.ChatCreateResponse
import io.github.mklkj.kommunicator.data.models.Message
import io.github.mklkj.kommunicator.data.models.MessageEvent
import kotlinx.uuid.UUID

interface MessagesService {

    @GET("/api/chats")
    suspend fun getChats(): List<Chat>

    @POST("/api/chats")
    suspend fun createChat(@Body body: ChatCreateRequest): ChatCreateResponse

    @GET("/api/chats/{id}")
    suspend fun getChat(@Path("id") id: UUID): Chat

    @GET("/api/chats/{chatId}/messages")
    suspend fun getMessages(@Path("chatId") chatId: UUID): List<Message>

    @POST("/api/chats/{chatId}/messages")
    suspend fun sendMessage(
        @Path("chatId") chatId: UUID,
        @Body message: MessageEvent,
    )
}
