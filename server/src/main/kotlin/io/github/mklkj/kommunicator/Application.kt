package io.github.mklkj.kommunicator

import io.github.mklkj.kommunicator.data.models.Chat
import io.github.mklkj.kommunicator.data.models.ChatDetails
import io.github.mklkj.kommunicator.data.models.Message
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.UUID

fun main() {
    embeddedServer(
        factory = Netty,
        port = SERVER_PORT,
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }

    routing {
        get("/") {
            call.respondText("Ktor: ${Greeting().greet()}")
        }
        get("/chats") {
            call.respond(
                listOf(
                    Chat(
                        isUnread = false,
                        lastMessage = "himenaeos",
                        lastMessageAuthor = "ridens",
                        name = "Edmond Hobbs",
                        avatarUrl = "https://placehold.co/64x64/orange/white.jpg",
                        lastMessageTimestamp = Clock.System.now().toLocalDateTime(TimeZone.UTC),
                        id = UUID.randomUUID().toString(),
                    ),
                    Chat(
                        isUnread = false,
                        lastMessage = "aenean",
                        lastMessageAuthor = "aptent",
                        name = "Alexander Benton",
                        avatarUrl = "https://placehold.co/64x64/green/black.png",
                        lastMessageTimestamp = Clock.System.now().toLocalDateTime(TimeZone.UTC),
                        id = UUID.randomUUID().toString(),
                    ),
                )
            )
        }
        get("/chat/{id}") {
            call.respond(
                ChatDetails(
                    name = "Edmond Hobbs",
                    messages = listOf(
                        Message(
                            id = UUID.randomUUID().toString(),
                            content = "lorem ipsum dolor sit amet",
                        ),
                        Message(
                            id = UUID.randomUUID().toString(),
                            content = "arcu",
                        ),
                        Message(
                            id = UUID.randomUUID().toString(),
                            content = "fastidii",
                        ),
                    )
                )
            )
        }
    }
}
