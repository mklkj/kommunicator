package io.github.mklkj.kommunicator.data.tables

import io.github.mklkj.kommunicator.data.enums.PGEnum
import io.github.mklkj.kommunicator.data.models.UserGender
import kotlinx.uuid.exposed.kotlinxUUID
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date

object UsersTable : Table() {
    val uuid = kotlinxUUID("uuid")
    val email = varchar("email", 64)
    val username = varchar("username", 64)
    val password = varchar("password", 64)
    val firstName = varchar("firstName", 64)
    val lastName = varchar("lastName", 64)
    val dateOfBirth = date("dateOfBirth")
    val gender = customEnumeration(
        name = "gender",
        sql = "UserGender",
        fromDb = { UserGender.valueOf(it.toString()) },
        toDb = { PGEnum("UserGender", it) },
    )

    override val primaryKey = PrimaryKey(uuid)
}
