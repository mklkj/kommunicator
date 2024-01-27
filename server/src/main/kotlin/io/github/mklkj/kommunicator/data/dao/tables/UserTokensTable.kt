package io.github.mklkj.kommunicator.data.dao.tables

import kotlinx.uuid.exposed.kotlinxUUID
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object UserTokensTable : Table("users_tokens") {

    val id = kotlinxUUID("id")
    val userId = kotlinxUUID("user_id")
    val refreshToken = varchar("refresh_token", 16)
    val createdAt = timestamp("created_at")
    val validTo = timestamp("valid_to")

    override val primaryKey = PrimaryKey(id)
}
