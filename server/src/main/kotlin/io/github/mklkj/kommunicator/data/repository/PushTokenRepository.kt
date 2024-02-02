package io.github.mklkj.kommunicator.data.repository

import io.github.mklkj.kommunicator.data.dao.UserPushTokensDao
import io.github.mklkj.kommunicator.data.models.PushTokenRequest
import kotlinx.uuid.UUID
import org.koin.core.annotation.Singleton

@Singleton
class PushTokenRepository(
    private val userPushTokensDao: UserPushTokensDao,
) {

    suspend fun savePushToken(pushTokenRequest: PushTokenRequest, userId: UUID) {
        userPushTokensDao.savePushToken(pushTokenRequest, userId)
    }
}
