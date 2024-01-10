package io.github.mklkj.kommunicator.data.dao.tables

import kotlinx.uuid.exposed.kotlinxUUID
import org.jetbrains.exposed.sql.Table

object ContactsTable : Table() {
    val id = kotlinxUUID("uuid")
    val contactUserId = kotlinxUUID("contactUserId")
    val userId = kotlinxUUID("userId")

    override val primaryKey = PrimaryKey(userId, contactUserId)
}
