package io.github.mklkj.kommunicator.data.repository

import io.github.mklkj.kommunicator.Contacts
import io.github.mklkj.kommunicator.data.api.service.ContactService
import io.github.mklkj.kommunicator.data.db.Database
import io.github.mklkj.kommunicator.data.models.ContactAddRequest
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Singleton

@Singleton
class ContactRepository(
    private val contactService: ContactService,
    private val database: Database,
) {

    suspend fun addContact(username: String) {
        runCatching { contactService.addContact(ContactAddRequest(username.trim())) }
            .onFailure {
                if (it is ClientRequestException) {
                    when (it.response.status) {
                        HttpStatusCode.BadRequest -> error("User does not exist")
                        HttpStatusCode.Conflict -> error("Contact already exist")
                    }
                }
            }
        refreshContacts()
    }

    fun observeContacts(): Flow<List<Contacts>> {
        return flow {
            val userId = database.getCurrentUser()?.id ?: error("There is no current user!")
            if (database.getContacts(userId).isEmpty()) {
                refreshContacts()
            }
            emitAll(database.observeContacts(userId))
        }
    }

    suspend fun refreshContacts() {
        val userId = database.getCurrentUser()?.id ?: error("There is no current user!")
        val remoteContacts = contactService.getContacts().contacts
        database.insertContacts(userId, remoteContacts)
    }
}
