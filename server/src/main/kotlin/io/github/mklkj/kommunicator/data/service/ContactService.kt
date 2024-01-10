package io.github.mklkj.kommunicator.data.service

import io.github.mklkj.kommunicator.data.models.Contact
import io.github.mklkj.kommunicator.data.models.ContactEntity
import io.github.mklkj.kommunicator.data.repository.ContactRepository
import kotlinx.uuid.UUID
import org.koin.core.annotation.Singleton

@Singleton
class ContactService(
    private val contactRepository: ContactRepository,
) {

    suspend fun getContactsByUser(id: UUID): List<Contact> {
        return contactRepository.getContacts(id)
    }

    suspend fun saveContactForUser(currentUser: UUID, contactUser: UUID) {
        contactRepository.addContact(
            ContactEntity(
                id = UUID(),
                contactUserId = contactUser,
                userId = currentUser,
            )
        )
    }
}
