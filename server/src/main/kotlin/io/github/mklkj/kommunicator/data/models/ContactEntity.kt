package io.github.mklkj.kommunicator.data.models

import kotlinx.uuid.UUID

data class ContactEntity(
    val id: UUID,
    val contactUserId: UUID,
    val userId: UUID,
)


data class ContactsEntityWithContactUserEntity(
    val id: UUID,
    val contactUserId: UUID,
    val firstName: String,
    val email: String,
    val lastName: String,
    val username: String,
)
