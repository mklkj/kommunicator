package io.github.mklkj.kommunicator.data.repository

import io.github.mklkj.kommunicator.data.api.service.ContactService
import io.github.mklkj.kommunicator.data.models.Contact
import io.github.mklkj.kommunicator.data.models.ContactAddRequest
import org.koin.core.annotation.Singleton

@Singleton
class ContactRepository(
    private val contactService: ContactService,
) {

    suspend fun addContact(username: String) {
        contactService.addContact(ContactAddRequest(username))
    }

    suspend fun getContacts(): List<Contact> {
        return contactService.getContacts().contacts
    }
}
