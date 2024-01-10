package io.github.mklkj.kommunicator.data.service

import io.github.mklkj.kommunicator.data.models.ContactEntity
import kotlinx.uuid.UUID
import org.koin.core.annotation.Singleton

@Singleton
class ContactService {

    fun getContactsByUser(id: UUID): List<ContactEntity> {
        return listOf(
            ContactEntity(
                id = UUID(),
                userId = UUID(),
                contactUserId = UUID(),
            )
        )
    }

    suspend fun saveContactForUser(currentUser: UUID, contactUser: UUID) {
        ContactEntity(
            id = UUID(),
            userId = currentUser,
            contactUserId = contactUser,
        )
    }
}
