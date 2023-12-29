package io.github.mklkj.kommunicator.data.service

import io.github.mklkj.kommunicator.data.models.User
import io.github.mklkj.kommunicator.data.models.UserRequest
import io.github.mklkj.kommunicator.data.repository.UserRepository
import kotlinx.uuid.UUID
import org.koin.core.annotation.Singleton

@Singleton
class UserService(
    private val userRepository: UserRepository,
) {

    suspend fun findAll(): List<User> = userRepository.findAll()

    suspend fun findById(id: String): User? = userRepository.findById(id = UUID(id))

    suspend fun findByUsername(username: String): User? = userRepository.findByUsername(username)

    suspend fun save(user: UserRequest) {
        user.id // todo: upsert
        user.username // todo: conflict
        userRepository.save(user.copy(
            password = user.password // todo: add hashing
        ))
    }
}
