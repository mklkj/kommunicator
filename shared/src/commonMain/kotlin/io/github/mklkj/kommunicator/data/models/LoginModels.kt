package io.github.mklkj.kommunicator.data.models

import kotlinx.serialization.Serializable
import kotlinx.uuid.UUID

@Serializable
data class LoginRequest(
    val username: String,
    val password: String,
)

@Serializable
data class LoginRefreshRequest(
    val refreshToken: String,
)

@Serializable
data class LoginResponse(
    val id: UUID,
    val token: String,
    val refreshToken: String,
)
