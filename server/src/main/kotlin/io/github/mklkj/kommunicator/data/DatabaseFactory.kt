package io.github.mklkj.kommunicator.data

import io.github.mklkj.kommunicator.data.dao.tables.ChatParticipantsTable
import io.github.mklkj.kommunicator.data.dao.tables.ChatsTable
import io.github.mklkj.kommunicator.data.dao.tables.ContactsTable
import io.github.mklkj.kommunicator.data.dao.tables.MessagesTable
import io.github.mklkj.kommunicator.data.dao.tables.UserTokensTable
import io.github.mklkj.kommunicator.data.dao.tables.UsersTable
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.annotation.Singleton

@Singleton
class DatabaseFactory {

    private val tables = arrayOf(
        UsersTable,
        MessagesTable,
        UserTokensTable,
        ContactsTable,
        ChatsTable,
        ChatParticipantsTable,
    )

    fun init() {
        val host = System.getenv("POSTGRES_HOST")
        val user = System.getenv("POSTGRES_USER")
        val password = System.getenv("POSTGRES_PASSWORD")
        val dbName = System.getenv("POSTGRES_DB")

        val database = Database.connect(
            url = "jdbc:postgresql://$host:5432/$dbName",
            driver = "org.postgresql.Driver",
            user = user,
            password = password,
        )

        val flyway = Flyway.configure()
            .dataSource(database.url, user, password)
            .validateMigrationNaming(true)
            .load()
        flyway.migrate()

        transaction(database) {
            SchemaUtils.statementsRequiredToActualizeScheme(*tables).let {
                if (it.isNotEmpty()) {
                    println(it.joinToString(";\n"))
                    error("There is/are ${it.size} migrations to run!")
                }
            }
            SchemaUtils.create(*tables)
        }
    }
}
