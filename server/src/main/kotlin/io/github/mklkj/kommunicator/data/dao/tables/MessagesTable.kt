package io.github.mklkj.kommunicator.data.dao.tables

import kotlinx.uuid.exposed.kotlinxUUID
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object MessagesTable : Table("messages") {

    val id = kotlinxUUID("id")
    val chatId = kotlinxUUID("chat_id")
    val participantId = kotlinxUUID("participant_id")
    val createdAt = timestamp("created_at")
    val content = text("content")

    override val primaryKey = PrimaryKey(id)
}
