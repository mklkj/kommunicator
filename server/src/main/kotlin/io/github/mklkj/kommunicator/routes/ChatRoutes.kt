package io.github.mklkj.kommunicator.routes

import io.github.mklkj.kommunicator.data.models.ChatCreateRequest
import io.github.mklkj.kommunicator.data.models.ChatDetails
import io.github.mklkj.kommunicator.data.models.Message
import io.github.mklkj.kommunicator.data.models.MessageEntity
import io.github.mklkj.kommunicator.data.models.MessageRequest
import io.github.mklkj.kommunicator.data.service.ChatService
import io.github.mklkj.kommunicator.data.service.MessageService
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

    get {
        val userId = call.principalId ?: error("Invalid JWT!")
        val chats = chatService.getChats(userId)

        call.respond(chats)
    }
    post {
        val request = call.receive<ChatCreateRequest>()
        chatService.addChat(
            request.copy(
                participants = request.participants + listOfNotNull(call.principalId)
            )
        )
        call.response.status(HttpStatusCode.Created)
    }
    get("/{id}") {
        val chat = chatService.getChat(
            chatId = call.parameters["id"]?.toUUIDOrNull() ?: error("Invalid chat id"),
            userId = call.principalId ?: error("Invalid JWT"),
        ) ?: return@get call.response.status(HttpStatusCode.NotFound)

        val currentUserId = call.principalId
        val chatMessages = messageService.getMessages(chat.id)
        val participants = chat.participants

        call.respond(
            ChatDetails(
                name = chat.name ?: buildString {
                    val name = chat.participants.first().let {
                        it.customName ?: it.firstName
                    }
                    append(name)
                },
                messages = chatMessages.map { message ->
                    Message(
                        id = message.id,
                        isUserMessage = message.userId == currentUserId,
                        authorId = message.userId,
                        authorName = when (message.userId) {
                            currentUserId -> "You"
                            else -> {
                                val participant = participants
                                    .find { it.userId == message.userId }
                                participant?.customName ?: participant?.let {
                                    "${it.firstName} ${it.lastName}"
                                } ?: chat.name
                            }
                        }.orEmpty(),
                        timestamp = message.timestamp,
                        content = message.content,
                    )
                },
            )
        )
    }
    post("/{id}/messages") {
        val message = call.receive<MessageRequest>()

        messageService.saveMessage(
            MessageEntity(
                id = message.id,
                chatId = call.parameters.getOrFail("id").toUUID(),
                userId = call.principalId ?: error("Invalid JWT token"),
                timestamp = Clock.System.now(),
                content = message.content
            )
        )

        call.respond(message = HttpStatusCode.Created)
    }
}
