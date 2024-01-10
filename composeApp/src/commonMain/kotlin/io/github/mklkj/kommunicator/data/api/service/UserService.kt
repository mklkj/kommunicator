package io.github.mklkj.kommunicator.data.api.service

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Header
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import io.github.mklkj.kommunicator.data.models.LoginRequest
import io.github.mklkj.kommunicator.data.models.LoginResponse
import io.github.mklkj.kommunicator.data.models.UserRequest
import io.github.mklkj.kommunicator.data.models.UserResponse
import kotlinx.uuid.UUID

interface UserService {

    @POST("/api/user")
    suspend fun registerUser(@Body body: UserRequest)

    @GET("/api/user/{id}")
    suspend fun getUser(
        @Header("Authorization") token: String, // todo: add this from bearer token block
        @Path("id") id: UUID,
    ): UserResponse

    @POST("/api/auth")
    suspend fun getToken(@Body body: LoginRequest): LoginResponse
}
