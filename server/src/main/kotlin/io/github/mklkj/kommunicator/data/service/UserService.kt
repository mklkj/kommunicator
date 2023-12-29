package io.github.mklkj.kommunicator.data.service

import io.github.mklkj.kommunicator.data.exceptions.AlreadyExist
import io.github.mklkj.kommunicator.data.models.User
import io.github.mklkj.kommunicator.data.models.UserRequest
import io.github.mklkj.kommunicator.data.repository.UserRepository
import kotlinx.uuid.UUID
import org.koin.core.annotation.Singleton
import org.springframework.security.crypto.password.PasswordEncoder

@Singleton
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {

    suspend fun findAll(): List<User> = userRepository.findAll()

    suspend fun findById(id: String): User? = userRepository.findById(id = UUID(id))

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
}
