package io.github.mklkj.kommunicator.data.models

import kotlinx.uuid.UUID

data class UserPushTokenEntity(
    val userId: UUID,
    val token: String,
)
