package io.github.mklkj.kommunicator.data.dao

import io.github.mklkj.kommunicator.data.dao.tables.ChatParticipantsTable
import io.github.mklkj.kommunicator.data.dao.tables.ChatsTable
import io.github.mklkj.kommunicator.data.models.ChatCreateRequest
import io.github.mklkj.kommunicator.data.models.ChatEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.uuid.UUID
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.annotation.Singleton

@Singleton
class ChatsDao {

    private fun resultRowToChats(row: ResultRow): ChatEntity {
        return ChatEntity(
            id = row[ChatsTable.id],
            customName = row[ChatsTable.customName],
        )
    }

    suspend fun createChat(chatCreateRequest: ChatCreateRequest) = withContext(Dispatchers.IO) {
        transaction {
            ChatsTable.insert {
                it[id] = chatCreateRequest.chatId
                it[customName] = chatCreateRequest.customName
            }
            ChatParticipantsTable.batchInsert(chatCreateRequest.participants) { participantId ->
                this[ChatParticipantsTable.id] = UUID()
                this[ChatParticipantsTable.chatId] = chatCreateRequest.chatId
                this[ChatParticipantsTable.userId] = participantId
                this[ChatParticipantsTable.customName] = null
            }
        }
    }

    suspend fun getChat(chatId: UUID, userId: UUID): ChatEntity? = dbQuery {
        ChatParticipantsTable
            .join(
                otherTable = ChatsTable,
                joinType = JoinType.LEFT,
                onColumn = ChatParticipantsTable.chatId,
                otherColumn = ChatsTable.id,
            )
            .select { (ChatParticipantsTable.userId eq userId) and (ChatsTable.id eq chatId) }
            .firstOrNull()
            ?.let { resultRowToChats(it) }
    }

    suspend fun getChats(userId: UUID) = dbQuery {
        ChatsTable
            .join(
                onColumn = ChatsTable.id,
                otherTable = ChatParticipantsTable,
                otherColumn = ChatParticipantsTable.chatId,
                joinType = JoinType.LEFT,
            )
            .select { ChatParticipantsTable.userId eq userId }
            .limit(15)
            // todo: pagination
            .map(::resultRowToChats)
    }
}
