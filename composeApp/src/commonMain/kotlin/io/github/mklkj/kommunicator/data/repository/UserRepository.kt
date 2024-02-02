package io.github.mklkj.kommunicator.data.repository

import com.mmk.kmpnotifier.notification.NotifierManager
import io.github.mklkj.kommunicator.data.api.service.UserService
import io.github.mklkj.kommunicator.data.db.Database
import io.github.mklkj.kommunicator.data.db.entity.LocalUser
import io.github.mklkj.kommunicator.data.models.LoginRequest
import io.github.mklkj.kommunicator.data.models.PushTokenRequest
import io.github.mklkj.kommunicator.data.models.UserRequest
import io.github.mklkj.kommunicator.ui.modules.registration.RegistrationCredentials
import io.github.mklkj.kommunicator.ui.utils.PlatformInfo
import io.ktor.client.HttpClient
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.plugins.plugin
import io.ktor.http.HttpStatusCode
import io.ktor.util.hex
import io.ktor.util.sha1
import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.uuid.UUID
import org.koin.core.annotation.Singleton

@Singleton
class UserRepository(
    private val userService: UserService,
    private val database: Database,
    private val httpClient: HttpClient,
    private val platformInfo: PlatformInfo,
) {

    suspend fun registerUser(credentials: RegistrationCredentials) {
        runCatching {
            userService.registerUser(
                UserRequest(
                    id = UUID(),
                    username = credentials.username.trim(),
                    password = credentials.password.trim(),
                    email = credentials.email.trim(),
                    firstName = credentials.firstName.trim(),
                    lastName = credentials.lastName.trim(),
                    dateOfBirth = credentials.dateOfBirth!!,
                    gender = credentials.gender!!,
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
        return database.getAllUsers().map { it.isNotEmpty() }
    }

    suspend fun logout() {
        withContext(Dispatchers.IO) {
            database.deleteCurrentUser()
            invalidateBearerTokens()
        }
    }

    suspend fun getCurrentUser(): LocalUser {
        return database.getCurrentUser() ?: error("There is no currently logged in user!")
    }

    suspend fun loginUser(username: String, password: String) {
        val response = runCatching {
            userService.getToken(
                LoginRequest(
                    username = username.trim(),
                    password = password.trim(),
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
                refreshToken = response.refreshToken,
                firstName = user.firstName,
                lastName = user.lastName,
                avatarUrl = user.avatarUrl,
            )
        )
        invalidateBearerTokens()

        sendPushToken(NotifierManager.getPushNotifier().getToken(), isFromLogin = true)
    }

    suspend fun sendPushToken(pushToken: String?, isFromLogin: Boolean = false) {
        if (database.getCurrentUser() == null && !isFromLogin) return

        userService.sendPushToken(
            PushTokenRequest(
                token = pushToken ?: return,
                deviceIdHash = hex(sha1((platformInfo.deviceId.orEmpty()).toByteArray())),
                deviceName = platformInfo.deviceName,
            )
        )
    }

    /**
     * Force the Auth plugin to invoke the `loadTokens` block again on the next client request.
     * @see [https://youtrack.jetbrains.com/issue/KTOR-4759/Auth-BearerAuthProvider-caches-result-of-loadToken-until-process-death#focus=Comments-27-6422735.0-0]
     */
    private fun invalidateBearerTokens() {
        try {
            httpClient.plugin(Auth).providers
                .filterIsInstance<BearerAuthProvider>()
                .firstOrNull()
                ?.clearToken()
        } catch (e: IllegalStateException) {
            // No-op; plugin not installed
        }
    }
}
