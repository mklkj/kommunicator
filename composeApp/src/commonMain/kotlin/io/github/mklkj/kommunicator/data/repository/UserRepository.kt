package io.github.mklkj.kommunicator.data.repository

import io.github.mklkj.kommunicator.data.api.service.UserService
import io.github.mklkj.kommunicator.data.db.Database
import io.github.mklkj.kommunicator.data.db.entity.LocalUser
import io.github.mklkj.kommunicator.data.models.LoginRequest
import io.github.mklkj.kommunicator.data.models.UserGender
import io.github.mklkj.kommunicator.data.models.UserRequest
import io.ktor.client.HttpClient
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.plugins.plugin
import io.ktor.http.HttpStatusCode
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
    private val httpClient: HttpClient,
) {

    suspend fun registerUser(username: String, password: String) {
        runCatching {
            userService.registerUser(
                UserRequest(
                    id = UUID(),
                    username = username,
                    password = password,
                    // todo: fill fields in the app
                    email = "marlene.henry@example.com",
                    firstName = "Alice Roy",
                    lastName = "Marvin Merritt",
                    dateOfBirth = LocalDate(2000, 1, 1),
                    gender = UserGender.MALE,
                )
            )
        }.onFailure {
            if (it is ClientRequestException) {
                when (it.response.status) {
                    HttpStatusCode.BadRequest -> error("Unknown error during registration")
                    HttpStatusCode.Conflict -> error("User already exist")
                }
            } else throw it
        }
    }

    fun isUserLoggedIn(): Flow<Boolean> {
        return database.getAlLUsers().map { it.isNotEmpty() }
    }

    suspend fun logout() {
        withContext(Dispatchers.IO) {
            database.clearDatabase()
            invalidateBearerTokens()
        }
    }

    suspend fun loginUser(username: String, password: String) {
        val response = runCatching {
            userService.loginUser(
                LoginRequest(
                    username = username,
                    password = password,
                )
            )
        }.onFailure {
            if (it is ClientRequestException) {
                when (it.response.status) {
                    HttpStatusCode.Unauthorized -> error("Invalid credentials")
                }
            } else throw it
        }.getOrThrow()

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
        invalidateBearerTokens()
    }

    /**
     * Force the Auth plugin to invoke the `loadTokens` block again on the next client request.
     * @see [https://youtrack.jetbrains.com/issue/KTOR-4759/Auth-BearerAuthProvider-caches-result-of-loadToken-until-process-death#focus=Comments-27-6422735.0-0]
     */
    private fun invalidateBearerTokens() {
        try {
            httpClient.plugin(Auth).providers
                .filterIsInstance<BearerAuthProvider>()
                .first()
                .clearToken()
        } catch (e: IllegalStateException) {
            // No-op; plugin not installed
        }
    }
}
