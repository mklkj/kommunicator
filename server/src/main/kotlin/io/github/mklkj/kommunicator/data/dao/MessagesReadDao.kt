package io.github.mklkj.kommunicator.data.dao

import io.github.mklkj.kommunicator.data.dao.tables.MessagesReadTable
import io.github.mklkj.kommunicator.data.models.MessageReadEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.annotation.Singleton

@Singleton
class MessagesReadDao {

    suspend fun saveMesssageReadStatus(entity: MessageReadEntity) = withContext(Dispatchers.IO) {
        transaction {
            MessagesReadTable.insert {
                it[messageId] = entity.messageId
                it[participantId] = entity.participantId
                it[readAt] = entity.readAt
            }
        }
    }
}
