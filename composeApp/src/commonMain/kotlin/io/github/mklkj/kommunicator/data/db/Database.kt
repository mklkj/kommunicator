package io.github.mklkj.kommunicator.data.db

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.db.SqlDriver
import io.github.mklkj.kommunicator.Contacts
import io.github.mklkj.kommunicator.Users
import io.github.mklkj.kommunicator.data.db.entity.LocalContact
import io.github.mklkj.kommunicator.data.db.entity.LocalUser
import io.github.mklkj.kommunicator.data.models.Contact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.uuid.UUID
import kotlinx.uuid.sqldelight.UUIDStringAdapter
import org.koin.core.annotation.Singleton

@Singleton
class Database(sqlDriver: SqlDriver) {

    private val database = AppDatabase(
        driver = sqlDriver,
        ContactsAdapter = Contacts.Adapter(
            idAdapter = UUIDStringAdapter,
            userIdAdapter = UUIDStringAdapter,
            contactUserIdAdapter = UUIDStringAdapter,
        ),
        UsersAdapter = Users.Adapter(UUIDStringAdapter)
    )
    private val dbQuery = database.appDatabaseQueries

    fun getAllUsers(): Flow<List<LocalUser>> {
        return dbQuery.selectAllUsers(::mapUserSelecting).asFlow().mapToList(Dispatchers.IO)
    }

    suspend fun getCurrentUser(): LocalUser? = withContext(Dispatchers.IO) {
        dbQuery.selectAllUsers(::mapUserSelecting).executeAsOneOrNull()
    }

    suspend fun deleteCurrentUser() = withContext(Dispatchers.IO) {
        dbQuery.transaction {
            dbQuery.removeAllUsers() // todo: rename?
        }
    }

    suspend fun updateUserTokens(id: UUID, token: String, refreshToken: String) =
        withContext(Dispatchers.IO) {
            dbQuery.updateUserTokens(
                id = id,
                token = token,
                refreshToken = refreshToken,
            )
        }

    fun insertUser(user: LocalUser) {
        dbQuery.insertUser(
            id = user.id,
            email = user.email,
            username = user.username,
            token = user.token,
            refreshToken = user.refreshToken,
            firstName = user.firstName,
            lastName = user.lastName,
            avatarUrl = user.avatarUrl,
        )
    }

    fun getAllContacts(userId: UUID): Flow<List<LocalContact>> {
        return dbQuery.selectAllContacts(userId, ::mapContactSelecting)
            .asFlow().mapToList(Dispatchers.IO)
    }

    suspend fun insertContacts(userId: UUID, contacts: List<Contact>) =
        withContext(Dispatchers.IO) {
            dbQuery.transaction {
                dbQuery.removeAllContacts(userId)
                contacts.forEach {
                    dbQuery.insertContact(
                        id = it.id,
                        userId = userId,
                        contactUserId = it.contactUserId,
                        avatarUrl = it.avatarUrl,
                        firstName = it.firstName,
                        lastName = it.lastName,
                        username = it.username,
                        isActive = it.isActive,
                    )
                }
            }
        }
}

private fun mapUserSelecting(
    id: UUID,
    email: String,
    username: String,
    token: String,
    refreshToken: String,
    firstName: String,
    lastName: String,
    avatarUrl: String,
): LocalUser = LocalUser(
    id = id,
    email = email,
    username = username,
    token = token,
    refreshToken = refreshToken,
    firstName = firstName,
    lastName = lastName,
    avatarUrl = avatarUrl,
)

private fun mapContactSelecting(
    id: UUID,
    userId: UUID,
    contactUserId: UUID,
    avatarUrl: String,
    firstName: String,
    lastName: String,
    username: String,
    isActive: Boolean,
): LocalContact = LocalContact(
    id = id,
    userId = userId,
    contactUserId = contactUserId,
    avatarUrl = avatarUrl,
    firstName = firstName,
    lastName = lastName,
    username = username,
    isActive = isActive
)
