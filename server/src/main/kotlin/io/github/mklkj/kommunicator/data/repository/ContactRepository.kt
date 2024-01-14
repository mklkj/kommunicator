package io.github.mklkj.kommunicator.data.repository

import io.github.mklkj.kommunicator.data.dao.ContactsDao
import io.github.mklkj.kommunicator.data.models.Contact
import io.github.mklkj.kommunicator.data.models.ContactEntity
import io.github.mklkj.kommunicator.utils.md5
import kotlinx.uuid.UUID
import org.koin.core.annotation.Singleton
import kotlin.random.Random

@Singleton
class ContactRepository(
    private val contactsDao: ContactsDao,
) {

    suspend fun getContacts(userId: UUID): List<Contact> {
        return contactsDao.getContacts(userId).map {
            Contact(
                id = it.id,
                contactUserId = it.contactUserId,
                avatarUrl = "https://gravatar.com/avatar/${md5(it.email)}",
                name = "${it.firstName} ${it.lastName}",
                username = it.username,
                isActive = Random.nextBoolean(), // todo
            )
        }
    }

    suspend fun addContact(contactEntity: ContactEntity) {
        contactsDao.addContact(contactEntity)
    }
}
