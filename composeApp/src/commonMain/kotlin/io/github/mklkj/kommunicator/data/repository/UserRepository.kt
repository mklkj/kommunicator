package io.github.mklkj.kommunicator.data.repository

import io.github.mklkj.kommunicator.data.api.service.UserService
import io.github.mklkj.kommunicator.data.db.Database
import io.github.mklkj.kommunicator.data.db.entity.LocalUser
import io.github.mklkj.kommunicator.data.models.LoginRequest
import io.github.mklkj.kommunicator.data.models.UserGender
import io.github.mklkj.kommunicator.data.models.UserRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.uuid.UUID
import org.koin.core.annotation.Singleton

@Singleton
class UserRepository(
    private val userService: UserService,
    private val database: Database,
) {

    suspend fun registerUser(username: String, password: String) {
        userService.registerUser(
            UserRequest(
                id = UUID(),
                username = username,
                password = password,
                email = "marlene.henry@example.com",
                firstName = "Alice Roy",
                lastName = "Marvin Merritt",
                dateOfBirth = LocalDate(2000, 1, 1),
                gender = UserGender.MALE,
            )
        )
    }

    fun isUserLoggedIn(): Flow<Boolean> {
        return database.getAlLUsers().map { it.isNotEmpty() }
    }

    suspend fun logout() {
        withContext(Dispatchers.IO) {
            database.clearDatabase()
        }
    }

    suspend fun loginUser(username: String, password: String) {
        val response = userService.loginUser(
            LoginRequest(
                username = username,
                password = password,
            )
        )
        val user = userService.getUser(
            token = "Bearer ${response.token}",
            id = response.id,
        )
        database.insertUser(
            LocalUser(
                id = response.id,
                email = user.email,
                username = user.username,
                token = response.token,
                firstName = user.firstName,
                lastName = user.lastName
            )
        )
    }
}
