package io.github.mklkj.kommunicator.data.models

import kotlinx.serialization.Serializable

@Serializable
data class ChatDetails(
    val name: String,
    val messages: List<Message>,
)
