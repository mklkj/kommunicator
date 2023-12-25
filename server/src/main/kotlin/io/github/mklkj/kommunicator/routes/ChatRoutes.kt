package io.github.mklkj.kommunicator.routes

import io.github.mklkj.kommunicator.data.models.Chat
import io.github.mklkj.kommunicator.data.models.ChatDetails
import io.github.mklkj.kommunicator.data.models.Message
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.uuid.UUID

fun Route.chatRoutes() {
    get {
        call.respond(
            listOf(
                Chat(
                    isUnread = false,
                    lastMessage = "himenaeos",
                    lastMessageAuthor = "ridens",
                    name = "Edmond Hobbs",
                    avatarUrl = "https://placehold.co/64x64/orange/white.jpg",
                    lastMessageTimestamp = Clock.System.now().toLocalDateTime(TimeZone.UTC),
                    id = UUID(),
                ),
                Chat(
                    isUnread = false,
                    lastMessage = "aenean",
                    lastMessageAuthor = "aptent",
                    name = "Alexander Benton",
                    avatarUrl = "https://placehold.co/64x64/green/black.png",
                    lastMessageTimestamp = Clock.System.now().toLocalDateTime(TimeZone.UTC),
                    id = UUID(),
                ),
            )
        )
    }
    get("/{id}") {
        call.respond(
            ChatDetails(
                name = "Edmond Hobbs",
                messages = listOf(
                    Message(
                        id = UUID(),
                        content = "lorem ipsum dolor sit amet",
                    ),
                    Message(
                        id = UUID(),
                        content = "arcu",
                    ),
                    Message(
                        id = UUID(),
                        content = "fastidii",
                    ),
                )
            )
        )
    }
}
