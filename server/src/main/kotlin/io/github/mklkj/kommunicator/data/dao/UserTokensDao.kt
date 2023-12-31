package io.github.mklkj.kommunicator.data.dao

import io.github.mklkj.kommunicator.data.dao.tables.UserTokensTable
import io.github.mklkj.kommunicator.data.models.UserTokenEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.uuid.UUID
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.annotation.Singleton

@Singleton
class UserTokensDao {

    private fun resultRowToUserToken(row: ResultRow) = UserTokenEntity(
        id = row[UserTokensTable.id],
        userId = row[UserTokensTable.userId],
        refreshToken = row[UserTokensTable.refreshToken],
        timestamp = row[UserTokensTable.timestamp],
        validTo = row[UserTokensTable.validTo],
    )

    suspend fun saveToken(token: UserTokenEntity) = withContext(Dispatchers.IO) {
        transaction {
            UserTokensTable.insert {
                it[id] = token.id
                it[userId] = token.userId
                it[refreshToken] = token.refreshToken
                it[timestamp] = token.timestamp
                it[validTo] = token.validTo
            }
        }
    }

    suspend fun getTokenInfo(refreshToken: String) = dbQuery {
        UserTokensTable.select { UserTokensTable.refreshToken eq refreshToken }
            .firstOrNull()
            ?.let(::resultRowToUserToken)
    }

    suspend fun deleteTokenInfo(tokenId: UUID) = dbQuery {
        UserTokensTable.deleteWhere { id eq tokenId }
    }
}
