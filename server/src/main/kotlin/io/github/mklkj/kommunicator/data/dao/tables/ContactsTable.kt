package io.github.mklkj.kommunicator.data.dao.tables

import kotlinx.uuid.exposed.kotlinxUUID
import org.jetbrains.exposed.sql.Table

object ContactsTable : Table("contacts") {
    val id = kotlinxUUID("id")
    val contactUserId = kotlinxUUID("contact_user_id")
    val userId = kotlinxUUID("user_id")

    override val primaryKey = PrimaryKey(userId, contactUserId)
}
