package io.github.mklkj.kommunicator.data.service

import io.github.mklkj.kommunicator.data.models.User
import io.github.mklkj.kommunicator.data.repository.UserRepository
import kotlinx.uuid.UUID
import org.koin.core.annotation.Singleton

@Singleton
class UserService(
    private val userRepository: UserRepository,
) {
    fun findAll(): List<User> = userRepository.findAll()

    fun findById(id: String): User? = userRepository.findById(id = UUID(id))

    fun findByUsername(username: String): User? = userRepository.findByUsername(username)

    fun save(user: User): User? {
        val foundUser = userRepository.findByUsername(user.username)
        return if (foundUser == null) {
            userRepository.save(user)
            user
        } else null
    }
}
