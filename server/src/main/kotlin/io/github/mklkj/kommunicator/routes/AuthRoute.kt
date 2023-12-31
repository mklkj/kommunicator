package io.github.mklkj.kommunicator.routes

import io.github.mklkj.kommunicator.data.models.LoginRefreshRequest
import io.github.mklkj.kommunicator.data.models.LoginRequest
import io.github.mklkj.kommunicator.data.models.LoginResponse
import io.github.mklkj.kommunicator.data.models.UserTokenEntity
import io.github.mklkj.kommunicator.data.service.JwtService
import io.github.mklkj.kommunicator.data.service.UserService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlinx.datetime.Clock
import kotlinx.uuid.UUID
import org.koin.ktor.ext.inject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.random.Random
import kotlin.time.toKotlinDuration

fun Route.authRoute() {
    val jwtService by inject<JwtService>()
    val userService by inject<UserService>()

    post {
        val loginRequest = call.receive<LoginRequest>()
        val (id, token) = jwtService.createJwtToken(loginRequest) ?: return@post call.respond(
            message = HttpStatusCode.Unauthorized,
        )
        val userToken = UserTokenEntity(
            id = UUID(),
            userId = id,
            refreshToken = createRefreshToken(),
            timestamp = Clock.System.now(),
            validTo = Clock.System.now().plus(java.time.Duration.ofDays(30).toKotlinDuration()),
        )
        userService.saveUserToken(userToken)

        call.respond(
            LoginResponse(
                id = id,
                token = token,
                refreshToken = userToken.refreshToken,
            )
        )
    }

    post("/refresh") {
        val refreshToken = call.receive<LoginRefreshRequest>().refreshToken

        val (token, userToken) = userService.refreshUserToken(refreshToken)
            ?: return@post call.response.status(HttpStatusCode.Unauthorized)

        call.respond(
            LoginResponse(
                id = userToken.userId,
                token = token,
                refreshToken = userToken.refreshToken,
            )
        )
    }
}

@Suppress("UnnecessaryOptInAnnotation")
@OptIn(ExperimentalEncodingApi::class)
fun createRefreshToken(): String {
    return Base64.encode(Random.nextBytes(10))
        .take(16)
}
