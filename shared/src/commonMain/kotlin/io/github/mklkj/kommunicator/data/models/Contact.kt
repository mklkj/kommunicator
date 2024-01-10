package io.github.mklkj.kommunicator.data.models

import kotlinx.serialization.Serializable
import kotlinx.uuid.UUID

@Serializable
data class Contact(
    val id: UUID,
    val contactUserId: UUID,
    val avatarUrl: String,
    val name: String,
    val username: String,
    val isActive: Boolean,
)
