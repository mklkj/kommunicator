package io.github.mklkj.kommunicator.data.db

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.db.SqlDriver
import io.github.mklkj.kommunicator.Chats
import io.github.mklkj.kommunicator.Contacts
import io.github.mklkj.kommunicator.Messages
import io.github.mklkj.kommunicator.Participants
import io.github.mklkj.kommunicator.SelectAllChats
import io.github.mklkj.kommunicator.Users
import io.github.mklkj.kommunicator.data.db.adapters.InstantStringAdapter
import io.github.mklkj.kommunicator.data.db.entity.LocalContact
import io.github.mklkj.kommunicator.data.db.entity.LocalUser
import io.github.mklkj.kommunicator.data.models.Chat
import io.github.mklkj.kommunicator.data.models.Contact
import io.github.mklkj.kommunicator.data.models.Message
import io.github.mklkj.kommunicator.data.models.MessageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.uuid.UUID
import kotlinx.uuid.sqldelight.UUIDStringAdapter
import org.koin.core.annotation.Singleton
import kotlin.random.Random

@Singleton
class Database(sqlDriver: SqlDriver) {

    private val database = AppDatabase(
        driver = sqlDriver,
        ContactsAdapter = Contacts.Adapter(
            idAdapter = UUIDStringAdapter,
            userIdAdapter = UUIDStringAdapter,
            contactUserIdAdapter = UUIDStringAdapter,
        ),
        UsersAdapter = Users.Adapter(UUIDStringAdapter),
        ChatsAdapter = Chats.Adapter(UUIDStringAdapter, UUIDStringAdapter),
        MessagesAdapter = Messages.Adapter(
            idAdapter = UUIDStringAdapter,
            chatIdAdapter = UUIDStringAdapter,
            authorIdAdapter = UUIDStringAdapter,
            createdAtAdapter = InstantStringAdapter,
        ),
        ParticipantsAdapter = Participants.Adapter(
            idAdapter = UUIDStringAdapter,
            userIdAdapter = UUIDStringAdapter,
            chatIdAdapter = UUIDStringAdapter,
        )
    )
    private val dbQuery = database.appDatabaseQueries

    fun getAllUsers(): Flow<List<LocalUser>> {
        return dbQuery.selectAllUsers(::mapUserSelecting).asFlow().mapToList(Dispatchers.IO)
    }

    suspend fun getCurrentUser(): LocalUser? = withContext(Dispatchers.IO) {
        dbQuery.selectAllUsers(::mapUserSelecting).executeAsOneOrNull()
    }

    suspend fun deleteCurrentUser() = withContext(Dispatchers.IO) {
        dbQuery.transaction {
            dbQuery.removeAllUsers() // todo: rename?
        }
    }

    suspend fun updateUserTokens(id: UUID, token: String, refreshToken: String) =
        withContext(Dispatchers.IO) {
            dbQuery.updateUserTokens(
                id = id,
                token = token,
                refreshToken = refreshToken,
            )
        }

    suspend fun insertUser(user: LocalUser) {
        withContext(Dispatchers.IO) {
            dbQuery.insertUser(
                id = user.id,
                email = user.email,
                username = user.username,
                token = user.token,
                refreshToken = user.refreshToken,
                firstName = user.firstName,
                lastName = user.lastName,
                avatarUrl = user.avatarUrl,
            )
        }
    }

    fun observeContacts(userId: UUID): Flow<List<LocalContact>> {
        return dbQuery.selectAllContacts(userId, ::mapContactSelecting)
            .asFlow().mapToList(Dispatchers.IO)
    }

    fun getContacts(userId: UUID): List<LocalContact> {
        return dbQuery.selectAllContacts(userId, ::mapContactSelecting).executeAsList()
    }

    suspend fun insertContacts(userId: UUID, contacts: List<Contact>) =
        withContext(Dispatchers.IO) {
            dbQuery.transaction {
                dbQuery.removeAllContacts(userId)
                contacts.forEach {
                    dbQuery.insertContact(
                        id = it.id,
                        userId = userId,
                        contactUserId = it.contactUserId,
                        avatarUrl = it.avatarUrl,
                        firstName = it.firstName,
                        lastName = it.lastName,
                        username = it.username,
                        isActive = it.isActive,
                    )
                }
            }
        }

    fun observeChats(userId: UUID): Flow<List<Chat>> {
        return dbQuery.selectAllChats(userId).asFlow()
            .mapToList(Dispatchers.IO)
            .map { chats ->
                chats.map {
                    Chat(
                        id = it.chatId,
                        customName = it.chatCustomName,
                        avatarUrl = it.avatarUrl,

                        isUnread = false,
                        isActive = Random.nextBoolean(),
                        participants = listOf(),

                        lastMessage = Message(
                            id = it.lastMessageId,
                            isUserMessage = false,
                            authorId = it.lastMessageAuthorId,
                            authorName = "${it.firstname} ${it.lastName}",
                            authorCustomName = it.lastMessageAuthorCustomName,
                            createdAt = it.createdAt,
                            content = it.content,
                        ),
                    )
                }
            }
    }

    suspend fun getChats(userId: UUID): List<SelectAllChats> {
        return withContext(Dispatchers.IO) {
            dbQuery.selectAllChats(userId).executeAsList()
        }
    }

    suspend fun getChat(chatId: UUID): Chats {
        return withContext(Dispatchers.IO) {
            dbQuery.selectChat(chatId).executeAsOne()
        }
    }

    suspend fun getChatParticipant(chatId: UUID, userId: UUID): Participants? {
        return withContext(Dispatchers.IO) {
            dbQuery.selectParticipantByUserId(userId, chatId)
        }.executeAsOneOrNull()
    }

    suspend fun insertChats(userId: UUID, chats: List<Chat>) {
        withContext(Dispatchers.IO) {
            dbQuery.transaction {
                chats.forEach {
                    dbQuery.insertChat(
                        id = it.id,
                        userId = userId,
                        customName = it.customName,
                        avatarUrl = it.avatarUrl,
                    )
                }

                chats.forEach { chat ->
                    chat.participants.forEach {
                        dbQuery.insertParticipant(
                            id = it.id,
                            userId = it.userId,
                            chatId = chat.id,
                            customName = it.customName,
                            firstname = it.firstName,
                            lastName = it.lastName,
                            avatarUrl = it.avatarUrl,
                        )
                    }
                }

                chats.map { it.id to it.lastMessage }.forEach { (chatId, lastMessage) ->
                    dbQuery.insertMessage(
                        id = lastMessage.id,
                        chatId = chatId,
                        authorId = lastMessage.authorId,
                        createdAt = lastMessage.createdAt,
                        content = lastMessage.content,
                    )
                }
            }
        }
    }

    fun observeMessages(chatId: UUID, userId: UUID): Flow<List<Message>> {
        return dbQuery.selectMessages(chatId).asFlow()
            .mapToList(Dispatchers.IO)
            .map { messages ->
                messages.map {
                    Message(
                        id = it.id,
                        isUserMessage = it.userId == userId,
                        authorId = it.authorId,
                        authorName = "${it.firstname} ${it.lastName}",
                        authorCustomName = it.customName,
                        createdAt = it.createdAt,
                        content = it.content
                    )
                }
            }
    }

    suspend fun insertMessages(chatId: UUID, messages: List<Message>) {
        withContext(Dispatchers.IO) {
            database.transaction {
                messages.forEach {
                    dbQuery.insertMessage(
                        id = it.id,
                        chatId = chatId,
                        authorId = it.authorId,
                        createdAt = it.createdAt,
                        content = it.content,
                    )
                }
            }
        }
    }

    suspend fun insertMessage(chatId: UUID, authorId: UUID, messageRequest: MessageRequest) {
        withContext(Dispatchers.IO) {
            dbQuery.insertMessage(
                id = messageRequest.id,
                chatId = chatId,
                authorId = authorId,
                createdAt = Clock.System.now(),
                content = messageRequest.content,
            )
        }
    }
}

private fun mapUserSelecting(
    id: UUID,
    email: String,
    username: String,
    token: String,
    refreshToken: String,
    firstName: String,
    lastName: String,
    avatarUrl: String,
): LocalUser = LocalUser(
    id = id,
    email = email,
    username = username,
    token = token,
    refreshToken = refreshToken,
    firstName = firstName,
    lastName = lastName,
    avatarUrl = avatarUrl,
)

private fun mapContactSelecting(
    id: UUID,
    userId: UUID,
    contactUserId: UUID,
    avatarUrl: String,
    firstName: String,
    lastName: String,
    username: String,
    isActive: Boolean,
): LocalContact = LocalContact(
    id = id,
    userId = userId,
    contactUserId = contactUserId,
    avatarUrl = avatarUrl,
    firstName = firstName,
    lastName = lastName,
    username = username,
    isActive = isActive
)
