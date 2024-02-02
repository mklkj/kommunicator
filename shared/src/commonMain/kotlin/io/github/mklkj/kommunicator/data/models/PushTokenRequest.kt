package io.github.mklkj.kommunicator.data.models

import kotlinx.serialization.Serializable

@Serializable
data class PushTokenRequest(
    val token: String,
    val deviceIdHash: String,
    val deviceName: String?,
)
