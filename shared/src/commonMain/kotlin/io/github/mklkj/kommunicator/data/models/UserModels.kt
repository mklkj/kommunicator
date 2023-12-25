package io.github.mklkj.kommunicator.data.models

import kotlinx.serialization.Serializable
import kotlinx.uuid.UUID

@Serializable
data class UserRequest(
    val username: String,
    val password: String,
)

@Serializable
data class UserResponse(
    val id: UUID,
    val username: String,
)
