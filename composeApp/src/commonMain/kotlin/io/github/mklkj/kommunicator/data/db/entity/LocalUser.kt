package io.github.mklkj.kommunicator.data.db.entity

import kotlinx.uuid.UUID

data class LocalUser(
    val id: UUID,
    val email: String,
    val username: String,
    val token: String,
    val refreshToken: String,
    val firstName: String,
    val lastName: String,
) {

    // todo: make this as field filled from API
    val avatarUrl: String = "https://i.pravatar.cc/256?u=$id"
}
