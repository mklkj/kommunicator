package io.github.mklkj.kommunicator.data.repository

import io.github.mklkj.kommunicator.data.api.service.UserService
import io.github.mklkj.kommunicator.data.models.LoginRequest
import io.github.mklkj.kommunicator.data.models.UserRequest
import org.koin.core.annotation.Singleton

@Singleton
class UserRepository(
    private val userService: UserService,
) {

    suspend fun registerUser(username: String, password: String) {
        userService.registerUser(
            UserRequest(
                username = username,
                password = password,
            )
        )
    }

    suspend fun loginUser(username: String, password: String) {
        val response = userService.loginUser(
            LoginRequest(
                username = username,
                password = password,
            )
        )
        response.token // todo: save this to some source
    }
}
