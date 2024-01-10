package io.github.mklkj.kommunicator.data.models

import kotlinx.serialization.Serializable

@Serializable
data class ContactAddRequest(
    val username: String,
)

@Serializable
data class ContactsResponse(
    val contacts: List<Contact>,
)
