package io.github.mklkj.kommunicator.data.dao.tables

import io.github.mklkj.kommunicator.data.enums.PGEnum
import io.github.mklkj.kommunicator.data.models.UserGender
import kotlinx.uuid.exposed.kotlinxUUID
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date

object UsersTable : Table("users") {

    val id = kotlinxUUID("id")
    val email = varchar("email", 64)
    val username = varchar("username", 64)
    val password = varchar("password", 64)
    val firstName = varchar("first_name", 64)
    val lastName = varchar("last_name", 64)
    val dateOfBirth = date("date_of_birth").nullable()
    val gender = customEnumeration(
        name = "gender",
        sql = "UserGender",
        fromDb = { UserGender.valueOf(it.toString()) },
        toDb = { PGEnum("UserGender", it) },
    ).nullable()

    override val primaryKey = PrimaryKey(id)

    init {
        uniqueIndex("Unique username constraint", username)
    }
}
