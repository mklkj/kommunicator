package io.github.mklkj.kommunicator.data.repository

import io.github.mklkj.kommunicator.data.models.User
import kotlinx.uuid.UUID
import org.koin.core.annotation.Singleton

@Singleton
class UserRepository {
    private val users = mutableListOf<User>()

    fun findAll(): List<User> = users

    fun findById(id: UUID): User? = users.firstOrNull { it.id == id }

    fun findByUsername(username: String): User? = users.firstOrNull { it.username == username }

    fun save(user: User): Boolean = users.add(user)
}
