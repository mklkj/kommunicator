package io.github.mklkj.kommunicator.data

import io.github.mklkj.kommunicator.data.tables.UsersTable
import kotlinx.coroutines.Dispatchers
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.annotation.Singleton

@Singleton
class DatabaseFactory {

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
            .load()
        flyway.migrate()

        transaction(database) {
            SchemaUtils.statementsRequiredToActualizeScheme(UsersTable).let {
                if (it.isNotEmpty()) {
                    println(it)
                    error("There is/are ${it.size} migrations to run!")
                }
            }
            SchemaUtils.create(UsersTable)
        }
    }
}