package io.github.mklkj.kommunicator.data.dao

import io.github.mklkj.kommunicator.data.dao.tables.ChatParticipantsTable
import io.github.mklkj.kommunicator.data.dao.tables.ChatsTable
import io.github.mklkj.kommunicator.data.dao.tables.MessagesTable
import io.github.mklkj.kommunicator.data.dao.tables.UsersTable
import io.github.mklkj.kommunicator.data.models.ChatCreateRequest
import io.github.mklkj.kommunicator.data.models.ChatEntity
import io.github.mklkj.kommunicator.data.models.ChatSummaryEntity
import io.github.mklkj.kommunicator.data.models.ChatSummaryParticipant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.uuid.UUID
import kotlinx.uuid.exposed.UUIDColumnType
import kotlinx.uuid.toUUID
import org.intellij.lang.annotations.Language
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.annotation.Singleton
import java.sql.ResultSet

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

    suspend fun getChats(userId: UUID): List<ChatSummaryEntity> = dbQuery {
        @Language("sql")
        val query = """
            WITH aggregated_participants as (
              SELECT chat_participants.chat_id, array_agg(array[
                    users.id::text,
                    chat_participants.custom_name,
                    users.first_name,
                    users.last_name,
                    users.email
                ]) as participants
              FROM chat_participants
              LEFT JOIN users ON users.id = chat_participants.user_id
              GROUP BY chat_participants.chat_id
            )
            SELECT
                chats.id as chat_id,
                chats.custom_name as custom_name,
                messages.created_at as last_message_created_at,
                messages.content as last_message_content,
                messages.id as last_message_author_user_id,
                messages.first_name as last_message_author_first_name,
                messages.last_name as last_message_author_last_name,
                participants
            FROM chats
            LEFT JOIN LATERAL(
                SELECT messages.created_at, messages.content, users.id, users.first_name, users.last_name
                FROM messages
                LEFT JOIN chat_participants ON chat_participants.chat_id = messages.chat_id AND chat_participants.user_id = messages.user_id
                LEFT JOIN users ON users.id = chat_participants.user_id
                WHERE chats.id = messages.chat_id
                ORDER BY created_at DESC
                LIMIT 1
            ) messages on true
            LEFT JOIN aggregated_participants ON chats.id = aggregated_participants.chat_id
            LEFT JOIN chat_participants ON chat_participants.chat_id = chats.id
            WHERE chat_participants.user_id = ?
            ORDER BY messages.created_at DESC
        """

        val stmt = TransactionManager.current().connection.prepareStatement(query, false)
        stmt.fillParameters(listOf(Pair(UUIDColumnType(), userId)))
        stmt.executeQuery().map { rs ->
            ChatSummaryEntity(
                id = ChatsTable.id.columnType.valueFromDB(
                    ChatsTable.id.columnType.readObject(rs, 1)!!
                ) as UUID,
                customName = ChatsTable.customName.columnType.readObject(rs, 2) as String?,
                lastMessageCreatedAt = MessagesTable.createdAt.columnType.readObject(rs, 3)
                    ?.let { MessagesTable.createdAt.columnType.valueFromDB(it) } as Instant?,
                lastMessageContent = MessagesTable.content.columnType.readObject(rs, 4) as String?,
                lastMessageAuthorId = (UsersTable.id.columnType.readObject(rs, 5))
                    ?.let { UsersTable.id.columnType.valueFromDB(it) } as UUID?,
                lastMessageAuthorFirstName = UsersTable.id.columnType.readObject(rs, 6) as String?,
                lastMessageAuthorLastName = UsersTable.id.columnType.readObject(rs, 7) as String?,
                participants = (rs.getArray(8).array as Array<*>).map {
                    val row = it as Array<*>
                    ChatSummaryParticipant(
                        userId = row[0].toString().toUUID(),
                        customName = row[1]?.toString(),
                        firstName = row[2].toString(),
                        lastName = row[3].toString(),
                        email = row[4].toString(),
                    )
                },
            )
        }
    }

    private fun <T> ResultSet.map(transform: (ResultSet) -> T): List<T> {
        val result = mutableListOf<T>()
        while (next()) {
            result.add(transform(this))
        }
        return result
    }
}
