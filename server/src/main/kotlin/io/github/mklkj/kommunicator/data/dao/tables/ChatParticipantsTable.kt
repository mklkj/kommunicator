package io.github.mklkj.kommunicator.data.dao.tables

import kotlinx.uuid.exposed.kotlinxUUID
import org.jetbrains.exposed.sql.Table

object ChatParticipantsTable : Table() {
    val id = kotlinxUUID("id")
    val chatId = kotlinxUUID("chatId")
    val userId = kotlinxUUID("userId")
    val customName = varchar("customName", length = 64).nullable()

    override val primaryKey = PrimaryKey(chatId, userId)
}
