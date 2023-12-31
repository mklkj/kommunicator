package io.github.mklkj.kommunicator.routes

import io.github.mklkj.kommunicator.data.models.Chat
import io.github.mklkj.kommunicator.data.models.ChatDetails
import io.github.mklkj.kommunicator.data.models.Message
import io.github.mklkj.kommunicator.data.models.MessageEntity
import io.github.mklkj.kommunicator.data.models.MessageRequest
import io.github.mklkj.kommunicator.data.service.MessageService
import io.github.mklkj.kommunicator.utils.extractPrincipalUsername
import io.github.mklkj.kommunicator.utils.principalId
import io.github.mklkj.kommunicator.utils.principalUsername
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.util.getOrFail
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlinx.uuid.UUID
import kotlinx.uuid.toUUID
import org.koin.ktor.ext.inject

val chatId1 = UUID("be5707b4-68d2-4370-b8e7-a5a9593990a0")
val chatId2 = UUID("dcd8023e-852e-4c11-9845-1c9edcf2c2b1")
val chatId3 = UUID("02f8066d-be94-49e5-ad91-6df32133c2c1")
val chatId4 = UUID("f1c78ea2-d3c8-46b9-9d49-e7a98f0898fe")

fun Route.chatRoutes() {
    val messageService by inject<MessageService>()

    val chats = listOf(
        Chat(
            isUnread = false,
            lastMessage = "lorem ipsum dolor sit amet",
            lastMessageAuthor = "ridens",
            name = "Oliwka Wojtaszek",
            avatarUrl = "https://i.pravatar.cc/256?u=$chatId1",
            lastMessageTimestamp = Clock.System.now()
                .minus(2, DateTimeUnit.SECOND)
                .toLocalDateTime(TimeZone.UTC),
            isActive = false,
            id = chatId1,
        ),
        Chat(
            isUnread = true,
            lastMessage = "aenean",
            lastMessageAuthor = "aptent",
            name = "Zuzanna Baran",
            avatarUrl = "https://i.pravatar.cc/256?u=$chatId2",
            lastMessageTimestamp = Clock.System.now()
                .minus(12, DateTimeUnit.HOUR)
                .toLocalDateTime(TimeZone.UTC),
            isActive = true,
            id = chatId2,
        ),
        Chat(
            isUnread = true,
            lastMessage = "aenean",
            lastMessageAuthor = "aptent",
            name = "Dominika Mydłowska",
            avatarUrl = "https://i.pravatar.cc/256?u=$chatId3",
            lastMessageTimestamp = Clock.System.now()
                .minus(128, DateTimeUnit.HOUR)
                .toLocalDateTime(TimeZone.UTC),
            isActive = false,
            id = chatId3,
        ),
        Chat(
            isUnread = false,
            lastMessage = "aenean",
            lastMessageAuthor = "aptent",
            name = "Adrian Duda",
            avatarUrl = "https://i.pravatar.cc/256?u=$chatId4",
            lastMessageTimestamp = Clock.System.now()
                .minus(1280, DateTimeUnit.HOUR)
                .toLocalDateTime(TimeZone.UTC),
            isActive = false,
            id = chatId4,
        ),
    )
    get {
        val username = extractPrincipalUsername(call)
        call.respond(chats.map {
            it.copy(
                lastMessageAuthor = username ?: it.lastMessageAuthor,
            )
        })
    }
    get("/{id}") {
        val chat = chats.first { it.id.toString() == call.parameters["id"] }
        val currentUserId = call.principalId

        val chatMessages = messageService.getMessages(chat.id)

        call.respond(
            ChatDetails(
                name = chat.name,
                messages = chatMessages.map {
                    Message(
                        id = it.id,
                        isUserMessage = it.userId == currentUserId,
                        authorName = when (it.userId) {
                            currentUserId -> call.principalUsername.orEmpty()
                            else -> chat.name // todo: get real name from Users table
                        },
                        timestamp = it.timestamp,
                        content = it.content,
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
