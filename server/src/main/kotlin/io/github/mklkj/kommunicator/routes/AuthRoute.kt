package io.github.mklkj.kommunicator.routes

import io.github.mklkj.kommunicator.data.models.LoginRequest
import io.github.mklkj.kommunicator.data.models.LoginResponse
import io.github.mklkj.kommunicator.data.service.JwtService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import org.koin.ktor.ext.inject

fun Route.authRoute() {
    val jwtService by inject<JwtService>()

    post {
        val loginRequest = call.receive<LoginRequest>()
        val (id, token) = jwtService.createJwtToken(loginRequest) ?: return@post call.respond(
            message = HttpStatusCode.Unauthorized,
        )
        call.respond(
            LoginResponse(
                id = id,
                token = token,
            )
        )
    }
}
