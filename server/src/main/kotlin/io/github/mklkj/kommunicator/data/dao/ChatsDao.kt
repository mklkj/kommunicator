package io.github.mklkj.kommunicator.data.dao

import io.github.mklkj.kommunicator.data.columns.ArrayColumnType
import io.github.mklkj.kommunicator.data.dao.tables.ChatParticipantsTable
import io.github.mklkj.kommunicator.data.dao.tables.ChatsTable
import io.github.mklkj.kommunicator.data.dao.tables.MessagesTable
import io.github.mklkj.kommunicator.data.dao.tables.UsersTable
import io.github.mklkj.kommunicator.data.models.ChatCreateRequest
import io.github.mklkj.kommunicator.data.models.ChatEntity
import io.github.mklkj.kommunicator.data.models.ChatSummaryEntity
import io.github.mklkj.kommunicator.data.models.ChatSummaryLastMessageEntity
import io.github.mklkj.kommunicator.data.models.ChatSummaryParticipant
import io.github.mklkj.kommunicator.utils.dbQuery
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

    suspend fun getChatsContainingParticipants(participantsIds: List<UUID>): List<UUID> = dbQuery {
        @Language("sql")
        val query = """
            SELECT chat_id
            FROM chat_participants
            GROUP BY chat_id
            HAVING array_agg(chat_participants.user_id ORDER BY chat_participants.user_id) = ?
        """

        val stmt = TransactionManager.current().connection.prepareStatement(query, false)
        val column = ArrayColumnType("UUID", participantsIds.size)
        stmt.fillParameters(listOf(Pair(column, participantsIds.sortedBy { it.toString() })))
        stmt.executeQuery().map { rs ->
            ChatParticipantsTable.chatId.columnType.valueFromDB(
                ChatParticipantsTable.chatId.columnType.readObject(rs, 1)!!
            ) as UUID
        }
    }

    suspend fun createChat(chatCreateRequest: ChatCreateRequest): UUID =
        withContext(Dispatchers.IO) {
            val chatId = UUID()
            transaction {
                ChatsTable.insert {
                    it[id] = chatId
                    it[customName] = chatCreateRequest.customName
                }
                ChatParticipantsTable.batchInsert(chatCreateRequest.participants) { participantId ->
                    this[ChatParticipantsTable.id] = UUID()
                    this[ChatParticipantsTable.chatId] = chatId
                    this[ChatParticipantsTable.userId] = participantId
                    this[ChatParticipantsTable.customName] = null
                }
            }
            chatId
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
                    chat_participants.id::text,
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
                messages.message_id as last_message_author_message_id,
                messages.participant_id as last_message_author_participant_id,
                messages.first_name as last_message_author_first_name,
                messages.last_name as last_message_author_last_name,
                messages.custom_name as last_message_author_custom_name,
                participants
            FROM chats
            LEFT JOIN LATERAL(
                SELECT
                    messages.id as message_id, messages.created_at, messages.content,
                    users.id as user_id, users.first_name, users.last_name,
                    chat_participants.id as participant_id, chat_participants.custom_name
                FROM messages
                LEFT JOIN chat_participants ON chat_participants.chat_id = messages.chat_id AND chat_participants.user_id = messages.user_id
                LEFT JOIN users ON users.id = chat_participants.user_id
                WHERE chats.id = messages.chat_id
                ORDER BY created_at DESC
                LIMIT 1
            ) messages on true
            LEFT JOIN aggregated_participants ON chats.id = aggregated_participants.chat_id
            LEFT JOIN chat_participants ON chat_participants.chat_id = chats.id
            WHERE chat_participants.user_id = ? AND messages.created_at IS NOT null
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
                lastMessage = ChatSummaryLastMessageEntity(
                    createdAt = MessagesTable.createdAt.columnType.readObject(rs, 3)
                        ?.let { MessagesTable.createdAt.columnType.valueFromDB(it) } as Instant,
                    content = MessagesTable.content.columnType.readObject(rs, 4) as String,
                    messageId = (MessagesTable.id.columnType.readObject(rs, 5))
                        ?.let { MessagesTable.id.columnType.valueFromDB(it) } as UUID,
                    authorId = (ChatParticipantsTable.id.columnType.readObject(rs, 6))
                        ?.let { ChatParticipantsTable.id.columnType.valueFromDB(it) } as UUID,
                    authorFirstName = UsersTable.id.columnType.readObject(rs, 7) as String,
                    authorLastName = UsersTable.id.columnType.readObject(rs, 8) as String,
                    authorCustomName = UsersTable.id.columnType.readObject(rs, 9) as String?,
                ),
                participants = (rs.getArray(10).array as Array<*>).map {
                    val row = it as Array<*>
                    ChatSummaryParticipant(
                        id = row[0].toString().toUUID(),
                        userId = row[1].toString().toUUID(),
                        customName = row[2]?.toString(),
                        firstName = row[3].toString(),
                        lastName = row[4].toString(),
                        email = row[5].toString(),
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
