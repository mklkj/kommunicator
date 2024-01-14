package io.github.mklkj.kommunicator.data.repository

import io.github.mklkj.kommunicator.data.api.service.ContactService
import io.github.mklkj.kommunicator.data.db.Database
import io.github.mklkj.kommunicator.data.db.entity.LocalContact
import io.github.mklkj.kommunicator.data.models.ContactAddRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.uuid.UUID
import org.koin.core.annotation.Singleton

@Singleton
class ContactRepository(
    private val contactService: ContactService,
    private val database: Database,
) {

    suspend fun addContact(username: String) {
        contactService.addContact(ContactAddRequest(username))
        refreshContacts()
    }

    fun getContacts(userId: UUID): Flow<List<LocalContact>> {
        return database.getAllContacts(userId)
    }

    suspend fun refreshContacts() {
        val userId = database.getCurrentUser()?.id ?: error("There is no current user!")
        val remoteContacts = contactService.getContacts().contacts
        database.insertContacts(userId, remoteContacts)
    }
}
