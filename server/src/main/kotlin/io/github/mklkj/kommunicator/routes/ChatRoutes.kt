package io.github.mklkj.kommunicator.routes

import io.github.mklkj.kommunicator.data.models.ChatCreateRequest
import io.github.mklkj.kommunicator.data.models.ChatCreateResponse
import io.github.mklkj.kommunicator.data.models.MessageEntity
import io.github.mklkj.kommunicator.data.models.MessageRequest
import io.github.mklkj.kommunicator.data.service.ChatService
import io.github.mklkj.kommunicator.data.service.MessageService
import io.github.mklkj.kommunicator.data.service.NotificationService
import io.github.mklkj.kommunicator.utils.principalId
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.util.getOrFail
import kotlinx.datetime.Clock
import kotlinx.uuid.toUUID
import kotlinx.uuid.toUUIDOrNull
import org.koin.ktor.ext.inject

fun Route.chatRoutes() {
    val chatService by inject<ChatService>()
    val messageService by inject<MessageService>()
    val notificationService by inject<NotificationService>()

    get {
        val userId = call.principalId ?: error("Invalid JWT!")
        val chats = chatService.getChats(userId)

        call.respond(chats)
    }
    post {
        val request = call.receive<ChatCreateRequest>()
        val createdChatId = chatService.addChat(
            request.copy(
                participants = request.participants + listOfNotNull(call.principalId)
            )
        )
        call.respond(HttpStatusCode.Created, ChatCreateResponse(createdChatId))
    }
    get("/{id}") {
        val userId = call.principalId ?: error("Invalid JWT!")
        val chatId = call.parameters["id"]?.toUUIDOrNull() ?: error("Invalid chat id")

        val chat = chatService.getChat(chatId = chatId, userId = userId)
            ?: return@get call.response.status(HttpStatusCode.NotFound)

        call.respond(chat)
    }
    get("/{id}/messages") {
        val userId = call.principalId ?: error("Invalid JWT!")
        // todo: add verification whether a given user can read messages from that chat!!!
        val chatId = call.parameters["id"]?.toUUIDOrNull() ?: error("Invalid chat id")

        call.respond(messageService.getMessages(chatId, userId))
    }
    post("/{id}/messages") {
        val userId = call.principalId ?: error("Invalid JWT token")
        val chatId = call.parameters.getOrFail("id").toUUID()
        val message = call.receive<MessageRequest>()

        // todo: add verification whether a given user can write to that chat!!!
        messageService.saveMessage(
            MessageEntity(
                id = message.id,
                chatId = chatId,
                userId = userId,
                timestamp = Clock.System.now(),
                content = message.content,
            )
        )
        notificationService.notifyParticipants(chatId, message.id, userId)

        call.respond(message = HttpStatusCode.Created)
    }
}
