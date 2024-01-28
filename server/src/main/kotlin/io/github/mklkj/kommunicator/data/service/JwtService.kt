package io.github.mklkj.kommunicator.data.service

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.typesafe.config.ConfigFactory
import io.github.mklkj.kommunicator.data.models.LoginRequest
import io.github.mklkj.kommunicator.data.models.User
import io.github.mklkj.kommunicator.data.repository.UserRepository
import io.ktor.server.auth.jwt.JWTCredential
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.config.HoconApplicationConfig
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import kotlinx.datetime.toJavaInstant
import kotlinx.uuid.UUID
import org.koin.core.annotation.Singleton
import org.springframework.security.crypto.password.PasswordEncoder

@Singleton
class JwtService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
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

    suspend fun createJwtToken(loginRequest: LoginRequest): Pair<UUID, String>? {
        val foundUser = userRepository.findByUsername(loginRequest.username) ?: return null
        return if (passwordEncoder.matches(loginRequest.password, foundUser.password)) {
            createJwtToken(foundUser)
        } else null
    }

    fun createJwtToken(user: User): Pair<UUID, String> {
        return user.id to JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("userId", user.id.toString())
            .withClaim("username", user.username)
            .withExpiresAt(Clock.System.now().plus(1, DateTimeUnit.MINUTE).toJavaInstant())
            .sign(Algorithm.HMAC256(secret))
    }

    fun customValidator(credential: JWTCredential): JWTPrincipal? = when {
        audienceMatches(credential) -> JWTPrincipal(credential.payload)
        else -> null
    }

    private fun audienceMatches(
        credential: JWTCredential,
    ): Boolean = credential.payload.audience.contains(audience)

    private fun getConfigProperty(path: String) =
        appConfig.property(path).getString()
}
