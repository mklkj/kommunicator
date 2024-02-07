package io.github.mklkj.kommunicator.plugins

import io.github.mklkj.kommunicator.Greeting
import io.github.mklkj.kommunicator.routes.authRoute
import io.github.mklkj.kommunicator.routes.chatRoutes
import io.github.mklkj.kommunicator.routes.chatWebsockets
import io.github.mklkj.kommunicator.routes.contactRoutes
import io.github.mklkj.kommunicator.routes.userRoutes
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Ktor: ${Greeting().greet()}")
        }

        route("/api/auth") { authRoute() }
        route("/api/user") { userRoutes() }
        authenticate {
            route("/api/contacts") { contactRoutes() }
            route("/api/chats") { chatRoutes() }
            route("/ws/chats") { chatWebsockets() }
        }
    }
}
