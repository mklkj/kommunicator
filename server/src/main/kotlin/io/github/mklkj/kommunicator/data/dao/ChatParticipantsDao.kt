package io.github.mklkj.kommunicator.data.dao

import io.github.mklkj.kommunicator.data.dao.tables.ChatParticipantsTable
import io.github.mklkj.kommunicator.data.dao.tables.UserPushTokensTable
import io.github.mklkj.kommunicator.data.dao.tables.UsersTable
import io.github.mklkj.kommunicator.data.models.ChatParticipantEntity
import io.github.mklkj.kommunicator.data.models.UserPushTokenEntity
import io.github.mklkj.kommunicator.utils.dbQuery
import io.github.mklkj.kommunicator.utils.md5
import kotlinx.uuid.UUID
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.koin.core.annotation.Singleton

@Singleton
class ChatParticipantsDao {

    private fun resultRowToParticipant(row: ResultRow): ChatParticipantEntity {
        return ChatParticipantEntity(
            id = row[ChatParticipantsTable.id],
            customName = row[ChatParticipantsTable.customName],
            userId = row[UsersTable.id],
            email = row[UsersTable.email],
            username = row[UsersTable.username],
            userFirstName = row[UsersTable.firstName],
            userLastName = row[UsersTable.lastName],
            userAvatarUrl = "https://gravatar.com/avatar/${md5(row[UsersTable.email])}"
        )
    }

    suspend fun getChatParticipantId(chatId: UUID, userId: UUID): UUID = dbQuery {
        ChatParticipantsTable
            .select {
                (ChatParticipantsTable.chatId eq chatId) and (ChatParticipantsTable.userId eq userId)
            }
            .map { it[ChatParticipantsTable.id] }
            .first()
    }

    suspend fun getParticipants(chatId: UUID) = dbQuery {
        ChatParticipantsTable
            .join(
                onColumn = ChatParticipantsTable.userId,
                otherTable = UsersTable,
                otherColumn = UsersTable.id,
                joinType = JoinType.LEFT,
            )
            .select { ChatParticipantsTable.chatId eq chatId }
            .limit(15)
            // todo: pagination
            .map(::resultRowToParticipant)
    }

    suspend fun getChatParticipantsPushTokens(
        chatId: UUID,
    ): List<UserPushTokenEntity> = dbQuery {
        ChatParticipantsTable
            .join(
                onColumn = ChatParticipantsTable.userId,
                otherTable = UserPushTokensTable,
                otherColumn = UserPushTokensTable.userId,
                joinType = JoinType.LEFT
            )
            .select { (ChatParticipantsTable.chatId eq chatId) and (UserPushTokensTable.token.isNotNull()) }
            .map {
                UserPushTokenEntity(
                    userId = it[ChatParticipantsTable.userId],
                    token = it[UserPushTokensTable.token],
                )
            }
    }
}
