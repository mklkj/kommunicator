package io.github.mklkj.kommunicator.data.dao

import io.github.mklkj.kommunicator.data.dao.tables.MessagesTable
import io.github.mklkj.kommunicator.data.dao.tables.UsersTable
import io.github.mklkj.kommunicator.data.models.MessageEntity
import io.github.mklkj.kommunicator.data.models.MessageWithUserEntity
import io.github.mklkj.kommunicator.utils.md5
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.uuid.UUID
import org.jetbrains.exposed.sql.JoinType
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
        timestamp = row[MessagesTable.createdAt],
        content = row[MessagesTable.content],
    )

    private fun resultRowToMessageWithAuthor(row: ResultRow) = MessageWithUserEntity(
        id = row[MessagesTable.id],
        chatId = row[MessagesTable.chatId],
        userId = row[MessagesTable.userId],
        timestamp = row[MessagesTable.createdAt],
        content = row[MessagesTable.content],
        authorUsername = row[UsersTable.username],
        authorFirstName = row[UsersTable.firstName],
        authorLastName = row[UsersTable.lastName],
        authorAvatarUrl = "https://gravatar.com/avatar/${md5(row[UsersTable.email])}",
    )

    suspend fun addMessage(message: MessageEntity) = withContext(Dispatchers.IO) {
        transaction {
            MessagesTable.insert {
                it[id] = message.id
                it[chatId] = message.chatId
                it[userId] = message.userId
                it[createdAt] = message.timestamp
                it[content] = message.content
            }
        }
    }

    suspend fun getMessages(chatId: UUID): List<MessageEntity> = dbQuery {
        MessagesTable
            .select { MessagesTable.chatId eq chatId }
            .orderBy(MessagesTable.createdAt, order = SortOrder.DESC)
            .limit(15)
            // todo: add pagination
            // based on where <= timestamp???
            .map(::resultRowToMessage)
    }

    suspend fun getLastMessage(chatId: UUID): MessageWithUserEntity? = dbQuery {
        MessagesTable
            .join(
                otherTable = UsersTable,
                joinType = JoinType.LEFT,
                onColumn = MessagesTable.userId,
                otherColumn = UsersTable.id,
            )
            .select { MessagesTable.chatId eq chatId }
            .orderBy(MessagesTable.createdAt, order = SortOrder.DESC)
            .firstOrNull()
            ?.let { resultRowToMessageWithAuthor(it) }
    }
}
