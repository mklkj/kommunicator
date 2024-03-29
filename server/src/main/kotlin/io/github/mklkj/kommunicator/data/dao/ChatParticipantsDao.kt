package io.github.mklkj.kommunicator.data.dao

import io.github.mklkj.kommunicator.data.dao.tables.ChatParticipantsTable
import io.github.mklkj.kommunicator.data.dao.tables.UserPushTokensTable
import io.github.mklkj.kommunicator.data.dao.tables.UsersTable
import io.github.mklkj.kommunicator.data.models.ChatParticipantEntity
import io.github.mklkj.kommunicator.data.models.ParticipantReadEntity
import io.github.mklkj.kommunicator.data.models.UserPushTokenEntity
import io.github.mklkj.kommunicator.utils.AvatarHelper
import io.github.mklkj.kommunicator.utils.dbQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.uuid.UUID
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.koin.core.annotation.Singleton

@Singleton
class ChatParticipantsDao(
    private val avatarHelper: AvatarHelper,
) {

    private fun resultRowToParticipant(row: ResultRow): ChatParticipantEntity {
        return ChatParticipantEntity(
            id = row[ChatParticipantsTable.id],
            customName = row[ChatParticipantsTable.customName],
            userId = row[UsersTable.id],
            email = row[UsersTable.email],
            username = row[UsersTable.username],
            userFirstName = row[UsersTable.firstName],
            userLastName = row[UsersTable.lastName],
            userAvatarUrl = avatarHelper.getUserAvatar(
                firstName = row[UsersTable.firstName],
                lastName = row[UsersTable.lastName],
                customName = row[ChatParticipantsTable.customName],
            ),
            readAt = row[ChatParticipantsTable.readAt],
        )
    }

    suspend fun saveParticipantReadStatus(status: ParticipantReadEntity) =
        withContext(Dispatchers.IO) {
            transaction {
                ChatParticipantsTable.update(
                    where = {
                        ChatParticipantsTable.id eq status.participantId
                    }
                ) {
                    it[readAt] = status.readAt
                }
            }
        }

    suspend fun getChatParticipantId(chatId: UUID, userId: UUID): UUID = dbQuery {
        ChatParticipantsTable
            .selectAll()
            .where { (ChatParticipantsTable.chatId eq chatId) and (ChatParticipantsTable.userId eq userId) }
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
            .selectAll()
            .where { ChatParticipantsTable.chatId eq chatId }
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
            .selectAll()
            .where { (ChatParticipantsTable.chatId eq chatId) and (UserPushTokensTable.token.isNotNull()) }
            .map {
                UserPushTokenEntity(
                    userId = it[ChatParticipantsTable.userId],
                    token = it[UserPushTokensTable.token],
                )
            }
    }
}
