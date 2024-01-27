package io.github.mklkj.kommunicator.data.dao.tables

import kotlinx.uuid.exposed.kotlinxUUID
import org.jetbrains.exposed.sql.Table

object ChatsTable : Table("chats") {
    val id = kotlinxUUID("id")
    val customName = varchar("custom_name", length = 64).nullable()

    override val primaryKey = PrimaryKey(id)
}
