package io.github.mklkj.kommunicator.routes

import io.github.mklkj.kommunicator.data.ChatConnections
import io.github.mklkj.kommunicator.data.models.ChatCreateRequest
import io.github.mklkj.kommunicator.data.models.ChatCreateResponse
import io.github.mklkj.kommunicator.data.models.Message
import io.github.mklkj.kommunicator.data.models.MessageEntity
import io.github.mklkj.kommunicator.data.models.MessageRequest
import io.github.mklkj.kommunicator.data.service.ChatService
import io.github.mklkj.kommunicator.data.service.MessageService
import io.github.mklkj.kommunicator.data.service.NotificationService
import io.github.mklkj.kommunicator.utils.getDeserialized
import io.github.mklkj.kommunicator.utils.principalId
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.engine.logError
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.util.getOrFail
import io.ktor.server.websocket.WebSocketServerSession
import io.ktor.server.websocket.sendSerialized
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.Clock
import kotlinx.uuid.UUID
import kotlinx.uuid.toUUID
import kotlinx.uuid.toUUIDOrNull
import org.koin.ktor.ext.inject

fun Route.chatRoutes() {
    val chatService by inject<ChatService>()
    val messageService by inject<MessageService>()
    val notificationService by inject<NotificationService>()
    val chatConnections by inject<ChatConnections>()

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
        val chatId = call.parameters.getOrFail("id").toUUID()

        call.respond(messageService.getMessages(chatId, userId))
    }
    post("/{id}/messages") {
        val userId = call.principalId ?: error("Invalid JWT token")
        val chatId = call.parameters.getOrFail("id").toUUID()
        val message = call.receive<MessageRequest>()

        // todo: add verification whether a given user can write to that chat!!!
        val entity = MessageEntity(
            id = message.id,
            chatId = chatId,
            userId = userId,
            timestamp = Clock.System.now(),
            content = message.content,
        )
        messageService.saveMessage(entity)

        chatConnections.getConnections(chatId).forEach {
            println("Notify user: ${it.userId}")
            it.session.sendSerialized(
                Message(
                    id = entity.id,
                    isUserMessage = false,
                    authorId = userId,
                    authorName = entity.firstName.orEmpty(), // todo
                    authorCustomName = entity.author,
                    createdAt = entity.timestamp,
                    content = entity.content,
                )
            )
        }
        notificationService.notifyParticipants(chatId, message.id, userId)

        call.respond(message = HttpStatusCode.Created)
    }
}

fun Route.chatWebsockets() {
    val messageService by inject<MessageService>()
    val notificationService by inject<NotificationService>()
    val chatConnections by inject<ChatConnections>()

    webSocket("/{id}/messages") {
        val userId = call.principalId ?: error("Invalid JWT token")
        val chatId = call.parameters.getOrFail("id").toUUID()

        println("Adding user!")
        val thisConnection = Connection(this, userId)

        chatConnections.addConnection(chatId, thisConnection)

        try {
            incoming.consumeAsFlow()
                .mapNotNull { frame ->
                    if (frame is Frame.Text) {
                        frame.getDeserialized<MessageRequest>(this)
                    } else null
                }
                .onEach { message ->
                    val entity = MessageEntity(
                        id = message.id,
                        chatId = chatId,
                        userId = userId,
                        timestamp = Clock.System.now(),
                        content = message.content,
                    )
                    messageService.saveMessage(entity)
                    chatConnections.getConnections(chatId)
//                    .filterNot { it.userId == userId }
                        .forEach {
                            println("Notify user: ${it.userId}")
                            it.session.sendSerialized(
                                Message(
                                    id = entity.id,
                                    isUserMessage = false,
                                    authorId = userId,
                                    authorName = entity.firstName.orEmpty(), // todo
                                    authorCustomName = entity.author,
                                    createdAt = entity.timestamp,
                                    content = entity.content,
                                )
                            )
                        }
                    // todo: add to some queue?
                    notificationService.notifyParticipants(chatId, message.id, userId)
                }
                .collect()
        } catch (e: Throwable) {
            logError(call, e)
        } finally {
            println("Removing $thisConnection!")
            chatConnections.removeConnection(chatId, thisConnection)
        }
    }
}

class Connection(val session: WebSocketServerSession, val userId: UUID)
