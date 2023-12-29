package io.github.mklkj.kommunicator.data.db

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.db.SqlDriver
import io.github.mklkj.kommunicator.Users
import io.github.mklkj.kommunicator.data.db.entity.LocalUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.uuid.UUID
import kotlinx.uuid.sqldelight.UUIDStringAdapter
import org.koin.core.annotation.Singleton

@Singleton
class Database(sqlDriver: SqlDriver) {

    private val database = AppDatabase(sqlDriver, Users.Adapter(UUIDStringAdapter))
    private val dbQuery = database.appDatabaseQueries

    fun clearDatabase() {
        dbQuery.transaction {
            dbQuery.removeAllUsers()
        }
    }

    fun getAlLUsers(): Flow<List<LocalUser>> {
        return dbQuery.selectAllUsers(::mapUserSelecting).asFlow().mapToList(Dispatchers.IO)
    }

    fun insertUser(user: LocalUser) {
        dbQuery.insertUser(
            id = user.id,
            email = user.email,
            username = user.username,
            token = user.token,
            firstName = user.firstName,
            lastName = user.lastName,
        )
    }
}

private fun mapUserSelecting(
    id: UUID,
    email: String,
    username: String,
    token: String,
    firstName: String,
    lastName: String,
): LocalUser = LocalUser(
    id = id,
    email = email,
    username = username,
    token = token,
    firstName = firstName,
    lastName = lastName
)