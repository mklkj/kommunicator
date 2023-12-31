package io.github.mklkj.kommunicator.data.models

import kotlinx.datetime.Instant
import kotlinx.uuid.UUID

data class UserTokenEntity(
    val id: UUID,
    val userId: UUID,
    val refreshToken: String,
    val timestamp: Instant,
    val validTo: Instant,
)
