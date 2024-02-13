package io.github.mklkj.kommunicator.data.dao.tables

import kotlinx.uuid.exposed.kotlinxUUID
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object MessagesReadTable : Table("messages_read") {

    val messageId = kotlinxUUID("message_id")
    val participantId = kotlinxUUID("participant_id")
    val readAt = timestamp("read_at")

    init {
        uniqueIndex("message_read_participant", messageId, participantId)
    }
}
