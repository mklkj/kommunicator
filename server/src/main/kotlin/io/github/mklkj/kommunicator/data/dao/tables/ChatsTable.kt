package io.github.mklkj.kommunicator.data.dao.tables

import kotlinx.uuid.exposed.kotlinxUUID
import org.jetbrains.exposed.sql.Table

object ChatsTable : Table() {
    val id = kotlinxUUID("id")
    val customName = varchar("customName", length = 64).nullable()

    override val primaryKey = PrimaryKey(id)
}
