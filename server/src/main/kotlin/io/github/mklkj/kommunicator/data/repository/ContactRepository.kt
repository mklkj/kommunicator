package io.github.mklkj.kommunicator.data.repository

import io.github.mklkj.kommunicator.data.dao.ContactsDao
import io.github.mklkj.kommunicator.data.models.Contact
import io.github.mklkj.kommunicator.data.models.ContactEntity
import io.github.mklkj.kommunicator.utils.AvatarHelper
import kotlinx.uuid.UUID
import org.koin.core.annotation.Singleton

@Singleton
class ContactRepository(
    private val contactsDao: ContactsDao,
    private val avatarHelper: AvatarHelper,
) {

    suspend fun getContacts(userId: UUID): List<Contact> {
        return contactsDao.getContacts(userId).map {
            Contact(
                id = it.id,
                contactUserId = it.contactUserId,
                avatarUrl = avatarHelper.getUserAvatar(it.firstName, it.lastName),
                firstName = it.firstName,
                lastName = it.lastName,
                username = it.username,
            )
        }
    }

    suspend fun addContact(contactEntity: ContactEntity) {
        contactsDao.addContact(contactEntity)
    }
}
