package io.github.mklkj.kommunicator.data.dao.tables

import kotlinx.uuid.exposed.kotlinxUUID
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object ChatParticipantsTable : Table("chat_participants") {

    val id = kotlinxUUID("id")
    val chatId = kotlinxUUID("chat_id")
    val userId = kotlinxUUID("user_id")
    val customName = varchar("custom_name", length = 64).nullable()
    val readAt = timestamp("read_at").nullable()

    override val primaryKey = PrimaryKey(chatId, userId)

    init {
        uniqueIndex("participants_unique", chatId, userId)
    }
}
