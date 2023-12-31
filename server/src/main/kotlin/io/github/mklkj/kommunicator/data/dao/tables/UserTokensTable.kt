package io.github.mklkj.kommunicator.data.dao.tables

import kotlinx.uuid.exposed.kotlinxUUID
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object UserTokensTable : Table() {

    val id = kotlinxUUID("id")
    val userId = kotlinxUUID("userId")
    val refreshToken = varchar("refreshToken", 16)
    val timestamp = timestamp("timestamp")
    val validTo = timestamp("validTo")

    override val primaryKey = PrimaryKey(id)
}
