package io.github.mklkj.kommunicator.data.tables

import kotlinx.uuid.exposed.kotlinxUUID
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object MessagesTable : Table() {

    val id = kotlinxUUID("id")
    val chatId = kotlinxUUID("chatId")
    val userId = kotlinxUUID("userId")
    val timestamp = timestamp("timestamp")
    val content = text("content")

    override val primaryKey = PrimaryKey(id)
}
