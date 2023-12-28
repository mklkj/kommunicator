package io.github.mklkj.kommunicator.routes

import io.github.mklkj.kommunicator.data.models.User
import io.github.mklkj.kommunicator.data.models.UserGender
import io.github.mklkj.kommunicator.data.models.UserRequest
import io.github.mklkj.kommunicator.data.models.UserResponse
import io.github.mklkj.kommunicator.data.service.UserService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlinx.datetime.LocalDate
import kotlinx.uuid.UUID
import org.koin.ktor.ext.inject

fun Route.userRoutes() {
    val userService by inject<UserService>()

    post {
        val userRequest = call.receive<UserRequest>()
        val createdUser = userService.save(
            user = userRequest.toModel()
        ) ?: return@post call.respond(HttpStatusCode.BadRequest)

        call.response.header(
            name = "id",
            value = createdUser.id.toString()
        )
        call.respond(
            message = HttpStatusCode.Created
        )
    }
    authenticate {
        get {
            val users = userService.findAll()
            call.respond(
                message = users.map(User::toResponse)
            )
        }
    }
    authenticate("another-auth") {
        get("/{id}") {
            val id: String = call.parameters["id"]
                ?: return@get call.respond(HttpStatusCode.BadRequest)
            val foundUser = userService.findById(id)
                ?: return@get call.respond(HttpStatusCode.NotFound)
            if (foundUser.username != extractPrincipalUsername(call))
                return@get call.respond(HttpStatusCode.NotFound)
            call.respond(
                message = foundUser.toResponse()
            )
        }
    }
}

private fun UserRequest.toModel(): User =
    User(
        id = UUID(),
        username = username,
        password = password,
        email = "lorena.koch@example.com",
        firstName = "Charmaine Daniel",
        lastName = "Bret Adkins",
        dateOfBirth = LocalDate.fromEpochDays(0),
        gender = UserGender.MALE,
    )

private fun User.toResponse(): UserResponse = UserResponse(
    id = id,
    username = username,
    email = email,
    firstName = firstName,
    lastName = lastName,
    dateOfBirth = dateOfBirth,
    gender = gender,
)

private fun extractPrincipalUsername(call: ApplicationCall): String? =
    call.principal<JWTPrincipal>()
        ?.payload
        ?.getClaim("username")
        ?.asString()
