package io.github.mklkj.kommunicator.data.models

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.uuid.UUID

@Serializable
data class UserRequest(
    val id: UUID,
    val email: String,
    val username: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: LocalDate,
    val gender: UserGender,
)

@Serializable
data class UserResponse(
    val id: UUID,
    val email: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: LocalDate,
    val gender: UserGender,
    val avatarUrl: String,
)
