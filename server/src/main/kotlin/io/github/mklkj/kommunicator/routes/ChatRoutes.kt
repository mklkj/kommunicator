package io.github.mklkj.kommunicator.routes

import io.github.mklkj.kommunicator.data.ChatConnections
import io.github.mklkj.kommunicator.data.models.ChatCreateRequest
import io.github.mklkj.kommunicator.data.models.ChatCreateResponse
import io.github.mklkj.kommunicator.data.models.MessageBroadcast
import io.github.mklkj.kommunicator.data.models.MessageEntity
import io.github.mklkj.kommunicator.data.models.MessageEvent
import io.github.mklkj.kommunicator.data.models.MessagePush
import io.github.mklkj.kommunicator.data.models.ParticipantReadEntity
import io.github.mklkj.kommunicator.data.models.ParticipantReadBroadcast
import io.github.mklkj.kommunicator.data.models.ChatReadPush
import io.github.mklkj.kommunicator.data.models.TypingBroadcast
import io.github.mklkj.kommunicator.data.models.TypingPush
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
//        val userId = call.principalId ?: error("Invalid JWT!")
        // todo: add verification whether a given user can read messages from that chat!!!
        val chatId = call.parameters.getOrFail("id").toUUID()

        call.respond(messageService.getMessages(chatId))
    }
    post("/{id}/messages") {
        val userId = call.principalId ?: error("Invalid JWT token")
        val chatId = call.parameters.getOrFail("id").toUUID()
        val message = call.receive<MessageEvent>()
        val participantId = messageService.getChatParticipantId(chatId, userId)

        if (message !is MessagePush) return@post

        val entity = MessageEntity(
            id = message.id,
            chatId = chatId,
            participantId = participantId,
            timestamp = Clock.System.now(),
            content = message.content,
        )
        // todo: add verification whether a given user can write to that chat!!!
        messageService.saveMessage(entity)

        val event = MessageBroadcast(
            id = entity.id,
            participantId = participantId,
            createdAt = entity.timestamp,
            content = entity.content,
        )
        val connections = chatConnections.getConnections(chatId)
        connections
            .filterNot { it.userId == userId }
            .forEach {
                println("Notify user: ${it.userId}")
                it.session.sendSerialized<MessageEvent>(event)
            }
        notificationService.notifyParticipants(
            chatId = chatId,
            messageId = message.id,
            alreadyNotifiedUsers = connections.map { it.userId } + userId,
            broadcast = event,
        )

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
        val participantId = messageService.getChatParticipantId(chatId, userId)

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
                    val connections = chatConnections.getConnections(chatId)

                    when (message) {
                        is MessagePush -> {
                            val entity = MessageEntity(
                                id = message.id,
                                chatId = chatId,
                                participantId = participantId,
                                timestamp = Clock.System.now(),
                                content = message.content,
                            )
                            messageService.saveMessage(entity)

                            val event = MessageBroadcast(
                                id = entity.id,
                                participantId = participantId,
                                content = entity.content,
                                createdAt = entity.timestamp
                            )
                            connections.forEach { connection ->
                                println("Notify user: ${connection.userId}")
                                connection.session.sendSerialized<MessageEvent>(event)
                            }
                            // todo: add to some queue?
                            notificationService.notifyParticipants(
                                chatId = chatId,
                                messageId = message.id,
                                alreadyNotifiedUsers = connections.map { it.userId },
                                broadcast = event,
                            )
                        }

                        is ChatReadPush -> {
                            val entity = ParticipantReadEntity(
                                participantId = participantId,
                                readAt = message.readAt,
                            )
                            messageService.saveParticipantReadStatus(entity)

                            connections.forEach { connection ->
                                val event = ParticipantReadBroadcast(
                                    participantId = participantId,
                                    readAt = entity.readAt,
                                )
                                connection.session.sendSerialized<MessageEvent>(event)
                            }
                        }

                        is TypingPush -> {
                            connections
                                .filterNot { it.userId == userId }
                                .forEach { connection ->
                                    val event = TypingBroadcast(
                                        participantId = participantId,
                                        isStop = message.isStop,
                                    )
                                    connection.session.sendSerialized<MessageEvent>(event)
                                }
                        }

                        // not handled on server
                        is MessageBroadcast -> Unit
                        is TypingBroadcast -> Unit
                        is ParticipantReadBroadcast -> Unit
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
