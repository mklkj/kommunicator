package io.github.mklkj.kommunicator.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: String,
    val content: String,
)
