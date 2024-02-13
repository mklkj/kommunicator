package io.github.mklkj.kommunicator.data.dao

import io.github.mklkj.kommunicator.data.dao.tables.ChatParticipantsTable
import io.github.mklkj.kommunicator.data.dao.tables.MessagesTable
import io.github.mklkj.kommunicator.data.dao.tables.UsersTable
import io.github.mklkj.kommunicator.data.models.MessageEntity
import io.github.mklkj.kommunicator.utils.dbQuery
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

    private fun resultRowToMessage(row: ResultRow, isExtended: Boolean = false) = MessageEntity(
        id = row[MessagesTable.id],
        chatId = row[MessagesTable.chatId],
        participantId = row[MessagesTable.participantId],
        timestamp = row[MessagesTable.createdAt],
        content = row[MessagesTable.content],
        author = if (isExtended) row[ChatParticipantsTable.customName] else null,
        firstName = if (isExtended) row[UsersTable.firstName] else null,
    )

    suspend fun addMessage(message: MessageEntity) = withContext(Dispatchers.IO) {
        transaction {
            MessagesTable.insert {
                it[id] = message.id
                it[chatId] = message.chatId
                it[participantId] = message.participantId
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

    suspend fun getMessage(messageId: UUID): MessageEntity = dbQuery {
        MessagesTable
            .join(
                otherTable = ChatParticipantsTable,
                otherColumn = ChatParticipantsTable.id,
                onColumn = MessagesTable.participantId,
                joinType = JoinType.LEFT
            )
            .join(
                otherTable = UsersTable,
                otherColumn = UsersTable.id,
                onColumn = ChatParticipantsTable.userId,
                joinType = JoinType.LEFT,
            )
            .select { MessagesTable.id eq messageId }
            .map { resultRowToMessage(it, true) }
            .let {
                it
            }
            // TODO: WHYYYY????
            .first()
    }
}
