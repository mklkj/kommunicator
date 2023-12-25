package io.github.mklkj.kommunicator.data.service

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.typesafe.config.ConfigFactory
import io.github.mklkj.kommunicator.data.models.LoginRequest
import io.github.mklkj.kommunicator.data.models.User
import io.ktor.server.auth.jwt.JWTCredential
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.config.HoconApplicationConfig
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import kotlinx.datetime.toJavaInstant
import org.koin.core.annotation.Singleton

@Singleton
class JwtService(
    private val userService: UserService,
) {

    private val appConfig = HoconApplicationConfig(ConfigFactory.load())
    private val secret = getConfigProperty("jwt.secret")
    private val issuer = getConfigProperty("jwt.issuer")
    private val audience = getConfigProperty("jwt.audience")
    val realm = getConfigProperty("jwt.realm")

    val jwtVerifier: JWTVerifier = JWT
        .require(Algorithm.HMAC256(secret))
        .withAudience(audience)
        .withIssuer(issuer)
        .build()

    fun createJwtToken(loginRequest: LoginRequest): String? {
        val foundUser: User? = userService.findByUsername(loginRequest.username)
        return if (foundUser != null && loginRequest.password == foundUser.password) {
            JWT.create()
                .withAudience(audience)
                .withIssuer(issuer)
                .withClaim("username", loginRequest.username)
                .withExpiresAt(Clock.System.now().plus(1, DateTimeUnit.HOUR).toJavaInstant())
                .sign(Algorithm.HMAC256(secret))
        } else null
    }

    fun customValidator(
        credential: JWTCredential,
    ): JWTPrincipal? {
        val username = extractUsername(credential)
        val foundUser = username?.let(userService::findByUsername)
        return foundUser?.let {
            if (audienceMatches(credential)) {
                JWTPrincipal(credential.payload)
            } else null
        }
    }

    private fun audienceMatches(
        credential: JWTCredential,
    ): Boolean = credential.payload.audience.contains(audience)

    private fun getConfigProperty(path: String) =
        appConfig.property(path).getString()

    private fun extractUsername(credential: JWTCredential): String? =
        credential.payload.getClaim("username").asString()
}
