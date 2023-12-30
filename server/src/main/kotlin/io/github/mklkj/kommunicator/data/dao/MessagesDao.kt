package io.github.mklkj.kommunicator.data.dao

import io.github.mklkj.kommunicator.data.models.MessageEntity
import io.github.mklkj.kommunicator.data.tables.MessagesTable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.uuid.UUID
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.annotation.Singleton

@Singleton
class MessagesDao {

    private fun resultRowToMessage(row: ResultRow) = MessageEntity(
        id = row[MessagesTable.id],
        chatId = row[MessagesTable.chatId],
        userId = row[MessagesTable.userId],
        timestamp = row[MessagesTable.timestamp],
        content = row[MessagesTable.content],
    )

    suspend fun addMessage(message: MessageEntity) = withContext(Dispatchers.IO) {
        transaction {
            MessagesTable.insert {
                it[id] = message.id
                it[chatId] = message.chatId
                it[userId] = message.userId
                it[timestamp] = message.timestamp
                it[content] = message.content
            }
        }
    }

    suspend fun getMessages(chatId: UUID): List<MessageEntity> = dbQuery {
        MessagesTable
            .select { MessagesTable.chatId eq chatId }
            .orderBy(MessagesTable.timestamp, order = SortOrder.DESC)
            .limit(15)
            // todo: add pagination
            // based on where <= timestamp???
            .map(::resultRowToMessage)
    }
}
