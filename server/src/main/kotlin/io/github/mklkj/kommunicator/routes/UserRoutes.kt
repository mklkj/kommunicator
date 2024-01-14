package io.github.mklkj.kommunicator.routes

import io.github.mklkj.kommunicator.data.exceptions.AlreadyExist
import io.github.mklkj.kommunicator.data.models.User
import io.github.mklkj.kommunicator.data.models.UserRequest
import io.github.mklkj.kommunicator.data.models.UserResponse
import io.github.mklkj.kommunicator.data.service.UserService
import io.github.mklkj.kommunicator.utils.extractPrincipalUsername
import io.github.mklkj.kommunicator.utils.md5
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlinx.uuid.UUID
import org.koin.ktor.ext.inject

fun Route.userRoutes() {
    val userService by inject<UserService>()

    post {
        val userRequest = call.receive<UserRequest>()
        val saveResult = runCatching { userService.save(userRequest) }
        if (saveResult.isFailure) {
            val responseCode = when (saveResult.exceptionOrNull()) {
                is AlreadyExist -> HttpStatusCode.Conflict
                else -> HttpStatusCode.BadRequest
            }
            call.response.status(responseCode)
        } else {
            call.response.header(
                name = "uuid",
                value = userRequest.id.toString()
            )
            call.respond(message = HttpStatusCode.Created)
        }
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
            val foundUser = userService.findById(UUID(id))
                ?: return@get call.respond(HttpStatusCode.NotFound)
            if (foundUser.username != extractPrincipalUsername(call))
                return@get call.respond(HttpStatusCode.NotFound)

            call.respond(message = foundUser.toResponse())
        }
    }
}

fun User.toResponse(): UserResponse = UserResponse(
    id = id,
    username = username,
    email = email,
    firstName = firstName,
    lastName = lastName,
    dateOfBirth = dateOfBirth,
    gender = gender,
    avatarUrl = "https://gravatar.com/avatar/${md5(email)}"
)
