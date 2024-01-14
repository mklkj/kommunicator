package io.github.mklkj.kommunicator.data.service

import io.github.mklkj.kommunicator.data.exceptions.AlreadyExist
import io.github.mklkj.kommunicator.data.models.User
import io.github.mklkj.kommunicator.data.models.UserRequest
import io.github.mklkj.kommunicator.data.models.UserTokenEntity
import io.github.mklkj.kommunicator.data.repository.UserRepository
import io.github.mklkj.kommunicator.routes.createRefreshToken
import io.github.mklkj.kommunicator.routes.toResponse
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import kotlinx.uuid.UUID
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.koin.core.annotation.Singleton
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.Duration
import java.time.Instant
import kotlin.time.toKotlinDuration

@Singleton
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
) {

    suspend fun findAll(): List<User> = userRepository.findAll()

    suspend fun findById(id: UUID): User? = userRepository.findById(id)

    suspend fun findByUsername(username: String): User? = userRepository.findByUsername(username)

    suspend fun save(user: UserRequest) {
        val idUser = userRepository.findById(user.id)
        if (idUser != null) {
            if (passwordEncoder.matches(user.password, idUser.password)) {
                // probably retry request, do nothing
                return
            } else throw AlreadyExist()
        }

        val usernameUser = userRepository.findByUsername(user.username)
        if (usernameUser != null) {
            throw AlreadyExist()
        }

        userRepository.save(
            user.copy(
                password = passwordEncoder.encode(user.password),
            )
        )
    }

    suspend fun refreshUserToken(refreshToken: String): Pair<String, UserTokenEntity>? {
        // todo: review this
        return newSuspendedTransaction {
            val tokenInfo = getTokenInfo(refreshToken) ?: return@newSuspendedTransaction null

            if (!tokenInfo.validTo.toJavaInstant().isAfter(Instant.now())) {
                return@newSuspendedTransaction null
            }

            val user = findById(tokenInfo.userId) ?: return@newSuspendedTransaction null

            val (id, token) = jwtService.createJwtToken(user)
            deleteTokenInfo(tokenInfo.id)
            val userToken = UserTokenEntity(
                id = UUID(),
                userId = id,
                refreshToken = createRefreshToken(),
                timestamp = Clock.System.now(),
                validTo = Clock.System.now().plus(Duration.ofDays(30).toKotlinDuration()),
            )
            saveUserToken(userToken)

            token to userToken
        }
    }

    suspend fun saveUserToken(userToken: UserTokenEntity) {
        userRepository.saveUserToken(userToken)
    }

    private suspend fun getTokenInfo(refreshToken: String): UserTokenEntity? {
        return userRepository.getTokenInfo(refreshToken)
    }

    private suspend fun deleteTokenInfo(tokenId: UUID) {
        userRepository.removeTokenInfo(tokenId)
    }
}
