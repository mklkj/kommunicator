package io.github.mklkj.kommunicator.data.dao

import io.github.mklkj.kommunicator.data.models.User
import io.github.mklkj.kommunicator.data.models.UserRequest
import io.github.mklkj.kommunicator.data.dao.tables.UsersTable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.uuid.UUID
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.annotation.Singleton

@Singleton
class UsersDao {

    private fun resultRowToUser(row: ResultRow) = User(
        id = row[UsersTable.uuid],
        email = row[UsersTable.email],
        username = row[UsersTable.username],
        password = row[UsersTable.password],
        firstName = row[UsersTable.firstName],
        lastName = row[UsersTable.lastName],
        dateOfBirth = row[UsersTable.dateOfBirth],
        gender = row[UsersTable.gender],
    )

    suspend fun addUser(user: UserRequest) = withContext(Dispatchers.IO) {
        transaction {
            UsersTable.insert {
                it[uuid] = user.id
                it[email] = user.email
                it[username] = user.username
                it[password] = user.password
                it[firstName] = user.firstName
                it[lastName] = user.lastName
                it[dateOfBirth] = user.dateOfBirth
                it[gender] = user.gender
            }
        }
    }

    suspend fun getAllUsers(): List<User> = dbQuery {
        UsersTable.selectAll().map(::resultRowToUser)
    }

    suspend fun findUser(uuid: UUID): User? = dbQuery {
        UsersTable
            .select { UsersTable.uuid eq uuid }
            .firstOrNull()?.let(::resultRowToUser)
    }

    suspend fun findUser(username: String): User? = dbQuery {
        UsersTable
            .select { UsersTable.username eq username }
            .firstOrNull()?.let(::resultRowToUser)
    }
}
