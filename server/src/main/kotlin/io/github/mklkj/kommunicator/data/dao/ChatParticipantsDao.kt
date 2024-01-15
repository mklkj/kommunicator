package io.github.mklkj.kommunicator.data.dao

import io.github.mklkj.kommunicator.data.dao.tables.ChatParticipantsTable
import io.github.mklkj.kommunicator.data.dao.tables.UsersTable
import io.github.mklkj.kommunicator.data.models.ChatParticipantEntity
import io.github.mklkj.kommunicator.utils.md5
import kotlinx.uuid.UUID
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import org.koin.core.annotation.Singleton

@Singleton
class ChatParticipantsDao {

    private fun resultRowToParticipant(row: ResultRow): ChatParticipantEntity {
        return ChatParticipantEntity(
            id = row[ChatParticipantsTable.id],
            customName = row[ChatParticipantsTable.customName],
            userId = row[UsersTable.uuid],
            email = row[UsersTable.email],
            username = row[UsersTable.username],
            userFirstName = row[UsersTable.firstName],
            userLastName = row[UsersTable.lastName],
            userAvatarUrl = "https://gravatar.com/avatar/${md5(row[UsersTable.email])}"
        )
    }

    suspend fun getParticipants(chatId: UUID) = dbQuery {
        ChatParticipantsTable
            .join(
                onColumn = ChatParticipantsTable.userId,
                otherTable = UsersTable,
                otherColumn = UsersTable.uuid,
                joinType = JoinType.LEFT,
            )
            .select { ChatParticipantsTable.chatId eq chatId }
            .limit(15)
            // todo: pagination
            .map(::resultRowToParticipant)
    }
}
