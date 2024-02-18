package io.github.mklkj.kommunicator.data.models

import kotlinx.serialization.Serializable
import kotlinx.uuid.UUID

@Serializable
data class Contact(
    val id: UUID,
    val contactUserId: UUID,
    val avatarUrl: String,
    val firstName: String,
    val lastName: String,
    val username: String,
)
