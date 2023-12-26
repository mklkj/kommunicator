package io.github.mklkj.kommunicator.data.api.service

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.Header
import de.jensklingenberg.ktorfit.http.POST
import io.github.mklkj.kommunicator.data.models.LoginRequest
import io.github.mklkj.kommunicator.data.models.LoginResponse
import io.github.mklkj.kommunicator.data.models.UserRequest
import io.github.mklkj.kommunicator.data.models.UserResponse

interface UserService {

    @POST("/api/user")
    suspend fun registerUser(
        @Body body: UserRequest,
        @Header("Content-Type") contentType: String = "application/json",
    ): UserResponse

    @POST("/api/auth")
    suspend fun loginUser(
        @Body body: LoginRequest,
        @Header("Content-Type") contentType: String = "application/json",
    ): LoginResponse
}
