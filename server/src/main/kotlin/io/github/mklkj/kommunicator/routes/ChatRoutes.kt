package io.github.mklkj.kommunicator.routes

import io.github.mklkj.kommunicator.data.ChatConnections
import io.github.mklkj.kommunicator.data.models.ChatCreateRequest
import io.github.mklkj.kommunicator.data.models.ChatCreateResponse
import io.github.mklkj.kommunicator.data.models.MessageBroadcast
import io.github.mklkj.kommunicator.data.models.MessageEntity
import io.github.mklkj.kommunicator.data.models.MessageEvent
import io.github.mklkj.kommunicator.data.models.MessagePush
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
        val message = call.receive<MessagePush>()
        val participants = chatService.getParticipants(chatId)

        // todo: add verification whether a given user can write to that chat!!!
        val entity = MessageEntity(
            id = message.id,
            chatId = chatId,
            userId = userId,
            timestamp = Clock.System.now(),
            content = message.content,
        )
        messageService.saveMessage(entity)

        val connections = chatConnections.getConnections(chatId)
        connections
            .filterNot { it.userId == userId }
            .forEach {
                println("Notify user: ${it.userId}")
                val event = MessageBroadcast(
                    id = entity.id,
                    participantId = participants.single { participant ->
                        it.userId == participant.userId
                    }.id,
                    createdAt = entity.timestamp,
                    content = entity.content,
                )
                it.session.sendSerialized<MessageEvent>(event)
            }
        notificationService.notifyParticipants(
            chatId = chatId,
            messageId = message.id,
            alreadyNotifiedUsers = connections.map { it.userId },
        )

        call.respond(message = HttpStatusCode.Created)
    }
}

fun Route.chatWebsockets() {
    val chatService by inject<ChatService>()
    val messageService by inject<MessageService>()
    val notificationService by inject<NotificationService>()
    val chatConnections by inject<ChatConnections>()

    webSocket("/{id}/messages") {
        val userId = call.principalId ?: error("Invalid JWT token")
        val chatId = call.parameters.getOrFail("id").toUUID()
        val participants = chatService.getParticipants(chatId)

        val thisConnection = Connection(this, userId)

        chatConnections.addConnection(chatId, thisConnection)

        try {
            incoming.consumeAsFlow()
                .mapNotNull { frame ->
                    if (frame is Frame.Text) {
                        frame.getDeserialized<MessageEvent>(this)
                    } else null
                }
                .onEach { message ->
                    when (message) {
                        is MessagePush -> {
                            val entity = MessageEntity(
                                id = message.id,
                                chatId = chatId,
                                userId = userId,
                                timestamp = Clock.System.now(),
                                content = message.content,
                            )
                            messageService.saveMessage(entity)

                            val connections = chatConnections.getConnections(chatId)
                            connections
                                .filterNot { it.userId == userId }
                                .forEach { connection ->
                                    println("Notify user: ${connection.userId}")
                                    val event = MessageBroadcast(
                                        id = entity.id,
                                        participantId = participants
                                            .single { userId == it.userId }
                                            .id,
                                        content = entity.content,
                                        createdAt = entity.timestamp
                                    )
                                    connection.session.sendSerialized<MessageEvent>(event)
                                }
                            // todo: add to some queue?
                            notificationService.notifyParticipants(
                                chatId = chatId,
                                messageId = message.id,
                                alreadyNotifiedUsers = connections.map { it.userId },
                            )
                        }

                        // not handled on server
                        is MessageBroadcast -> Unit
                    }
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
