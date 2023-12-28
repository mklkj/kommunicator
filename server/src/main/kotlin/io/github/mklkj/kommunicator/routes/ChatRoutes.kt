package io.github.mklkj.kommunicator.routes

import io.github.mklkj.kommunicator.data.models.Chat
import io.github.mklkj.kommunicator.data.models.ChatDetails
import io.github.mklkj.kommunicator.data.models.Message
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlinx.uuid.UUID

val chatId1 = UUID("be5707b4-68d2-4370-b8e7-a5a9593990a0")
val chatId2 = UUID("dcd8023e-852e-4c11-9845-1c9edcf2c2b1")
val chatId3 = UUID("02f8066d-be94-49e5-ad91-6df32133c2c1")
val chatId4 = UUID("f1c78ea2-d3c8-46b9-9d49-e7a98f0898fe")

fun Route.chatRoutes() {
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
        call.respond(chats + chats + chats + chats)
    }
    get("/{id}") {
        val chat = chats.first { it.id.toString() == call.parameters["id"] }
        call.respond(
            ChatDetails(
                name = chat.name,
                messages = listOf(
                    Message(
                        id = UUID(),
                        content = " Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nullam eros risus, ullamcorper vitae mauris placerat, lobortis maximus nisi. Quisque consequat lobortis aliquam. Praesent ultrices dignissim nisi, hendrerit venenatis libero. Suspendisse nec sem sodales, iaculis leo eget, efficitur tellus. Suspendisse nec neque commodo, cursus lorem ac, rutrum nulla.",
                        isUserMessage = false,
                        authorName = chat.name,
                    ),
                    Message(
                        id = UUID(),
                        content = "Nullam pharetra quam vel nulla vestibulum scelerisque. Mauris eget dui lectus. Quisque eu dui porttitor, tempus justo nec, varius mauris.",
                        isUserMessage = true,
                        authorName = "You",
                    ),
                    Message(
                        id = UUID(),
                        content = "fastidii",
                        isUserMessage = false,
                        authorName = chat.name,
                    ),
                    Message(
                        id = UUID(),
                        content = "Nullam ultrices porttitor est, sit amet sodales massa efficitur sit amet. Phasellus pharetra id libero a placerat. Maecenas lacinia tincidunt nisi accumsan lobortis. Donec egestas velit et hendrerit fermentum",
                        isUserMessage = false,
                        authorName = chat.name,
                    ),
                    Message(
                        id = UUID(),
                        content = "Praesent commodo vestibulum nisl",
                        isUserMessage = true,
                        authorName = "You",
                    ),
                )
            )
        )
    }
}
