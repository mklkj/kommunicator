package io.github.mklkj.kommunicator.data.repository

import io.github.mklkj.kommunicator.data.dao.UserTokensDao
import io.github.mklkj.kommunicator.data.dao.UsersDao
import io.github.mklkj.kommunicator.data.models.User
import io.github.mklkj.kommunicator.data.models.UserRequest
import io.github.mklkj.kommunicator.data.models.UserTokenEntity
import kotlinx.uuid.UUID
import org.koin.core.annotation.Singleton

@Singleton
class UserRepository(
    private val usersDao: UsersDao,
    private val userTokensDao: UserTokensDao,
) {

    suspend fun findAll(): List<User> = usersDao.getAllUsers()

    suspend fun findById(id: UUID): User? = usersDao.findUser(id)

    suspend fun findByUsername(username: String): User? = usersDao.findUser(username)

    suspend fun save(user: UserRequest) = usersDao.addUser(user)

    suspend fun saveUserToken(userToken: UserTokenEntity) = userTokensDao.saveToken(userToken)

    suspend fun getTokenInfo(refreshToken: String) = userTokensDao.getTokenInfo(refreshToken)

    suspend fun removeTokenInfo(tokenId: UUID) = userTokensDao.deleteTokenInfo(tokenId)
}
