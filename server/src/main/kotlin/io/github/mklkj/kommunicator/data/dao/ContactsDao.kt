package io.github.mklkj.kommunicator.data.dao

import io.github.mklkj.kommunicator.data.dao.tables.ContactsTable
import io.github.mklkj.kommunicator.data.dao.tables.UsersTable
import io.github.mklkj.kommunicator.data.models.ContactEntity
import io.github.mklkj.kommunicator.data.models.ContactsEntityWithContactUserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.uuid.UUID
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.annotation.Singleton

@Singleton
class ContactsDao {

    private fun resultRowToContactWithContactUser(row: ResultRow) =
        ContactsEntityWithContactUserEntity(
            id = row[ContactsTable.id],
            contactUserId = row[ContactsTable.contactUserId],
            firstName = row[UsersTable.firstName],
            lastName = row[UsersTable.lastName],
            email = row[UsersTable.email],
            username = row[UsersTable.username],
        )

    suspend fun addContact(contactEntity: ContactEntity) = withContext(Dispatchers.IO) {
        transaction {
            ContactsTable.insert {
                it[id] = contactEntity.id
                it[contactUserId] = contactEntity.contactUserId
                it[userId] = contactEntity.userId
            }
        }
    }

    suspend fun getContacts(userId: UUID): List<ContactsEntityWithContactUserEntity> = dbQuery {
        ContactsTable
            .join(
                otherTable = UsersTable,
                joinType = JoinType.LEFT,
                onColumn = ContactsTable.contactUserId,
                otherColumn = UsersTable.id
            )
            .select { ContactsTable.userId eq userId }
            .limit(15)
            // todo: add pagination
            .map(::resultRowToContactWithContactUser)
    }
}
