package io.github.mklkj.kommunicator.data.dao

import io.github.mklkj.kommunicator.data.dao.tables.UserPushTokensTable
import io.github.mklkj.kommunicator.data.models.PushTokenRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.uuid.UUID
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.upsert
import org.koin.core.annotation.Singleton

@Singleton
class UserPushTokensDao {

    suspend fun savePushToken(pushTokenRequest: PushTokenRequest, userId: UUID) =
        withContext(Dispatchers.IO) {
            transaction {
                UserPushTokensTable.upsert {
                    it[token] = pushTokenRequest.token
                    it[deviceName] = pushTokenRequest.deviceName
                    it[deviceIdHash] = pushTokenRequest.deviceIdHash
                    it[UserPushTokensTable.userId] = userId
                }
            }
        }
}
