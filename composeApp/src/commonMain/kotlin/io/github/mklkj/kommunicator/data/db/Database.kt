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
import io.github.mklkj.kommunicator.data.db.entity.LocalChat
import io.github.mklkj.kommunicator.data.db.entity.LocalMessage
import io.github.mklkj.kommunicator.data.models.Chat
import io.github.mklkj.kommunicator.data.models.Contact
import io.github.mklkj.kommunicator.data.models.Message
import io.github.mklkj.kommunicator.data.models.MessageBroadcast
import io.github.mklkj.kommunicator.data.models.MessageRequest
import io.github.mklkj.kommunicator.data.models.ParticipantReadBroadcast
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
            readAtAdapter = InstantStringAdapter,
        )
    )
    private val dbQuery = database.appDatabaseQueries

    fun getAllUsers(): Flow<List<Users>> {
        return dbQuery.selectAllUsers().asFlow().mapToList(Dispatchers.IO)
    }

    suspend fun getCurrentUser(): Users? = withContext(Dispatchers.IO) {
        dbQuery.selectAllUsers().executeAsOneOrNull()
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

    suspend fun insertUser(user: Users) {
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

    fun observeContacts(userId: UUID): Flow<List<Contacts>> {
        return dbQuery.selectAllContacts(userId).asFlow().mapToList(Dispatchers.IO)
    }

    fun getContacts(userId: UUID): List<Contacts> {
        return dbQuery.selectAllContacts(userId).executeAsList()
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

    fun observeChats(userId: UUID): Flow<List<LocalChat>> {
        return dbQuery.selectAllChats(userId).asFlow()
            .mapToList(Dispatchers.IO)
            .map { chats ->
                chats.map {
                    val isUserMessage = userId == it.lastMessageUserId
                    val isUnread = if (it.readAt != null) {
                        it.readAt.toEpochMilliseconds() < it.lastMessageCreatedAt.toEpochMilliseconds()
                    } else true

                    LocalChat(
                        id = it.chatId,
                        customName = it.chatCustomName,
                        avatarUrl = it.avatarUrl,

                        isUnread = isUnread && !isUserMessage,
                        isActive = Random.nextBoolean(),
                        participants = listOf(),

                        lastMessage = LocalMessage(
                            id = it.lastMessageId,
                            isUserMessage = isUserMessage,
                            authorId = it.lastMessageAuthorId,
                            participantName = it.lastMessageAuthorCustomName ?: it.firstname,
                            createdAt = it.lastMessageCreatedAt,
                            content = it.content,
                            chatId = it.chatId,
                            userId = userId,
                            avatarUrl = it.avatarUrl,
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

    suspend fun getChat(chatId: UUID): Chats? {
        return withContext(Dispatchers.IO) {
            dbQuery.selectChat(chatId).executeAsOneOrNull()
        }
    }

    suspend fun getChatParticipant(chatId: UUID, userId: UUID): Participants? {
        return withContext(Dispatchers.IO) {
            dbQuery.selectParticipantByUserId(userId, chatId)
        }.executeAsOneOrNull()
    }

    fun observeParticipants(chatId: UUID): Flow<List<Participants>> {
        return dbQuery.selectChatParticipants(chatId).asFlow()
            .mapToList(Dispatchers.IO)
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
                            readAt = it.readAt,
                        )
                    }
                }

                chats
                    .map { it.id to it.lastMessage }
                    .forEach { (chatId, lastMessage) ->
                        lastMessage?.let {
                            dbQuery.insertMessage(
                                id = lastMessage.id,
                                chatId = chatId,
                                authorId = lastMessage.participantId,
                                createdAt = lastMessage.createdAt,
                                content = lastMessage.content,
                            )
                        }
                    }
            }
        }
    }

    fun observeMessages(chatId: UUID, userId: UUID): Flow<List<LocalMessage>> {
        return dbQuery.selectMessages(chatId).asFlow()
            .mapToList(Dispatchers.IO)
            .map { messages ->
                messages.map {
                    LocalMessage(
                        id = it.id,
                        isUserMessage = it.participantUserId == userId,
                        authorId = userId,
                        participantName = it.customName ?: it.firstname.orEmpty(),
                        createdAt = it.createdAt,
                        content = it.content,
                        chatId = chatId,
                        userId = userId,
                        avatarUrl = it.avatarUrl,
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
                        authorId = it.participantId,
                        createdAt = it.createdAt,
                        content = it.content,
                    )
                }
            }
        }
    }

    suspend fun insertIncomingMessage(chatId: UUID, message: MessageBroadcast) {
        withContext(Dispatchers.IO) {
            dbQuery.insertMessage(
                id = message.id,
                chatId = chatId,
                authorId = message.participantId,
                createdAt = message.createdAt,
                content = message.content,
            )
        }

    }

    suspend fun insertUserMessage(chatId: UUID, authorId: UUID, messageRequest: MessageRequest) {
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

    suspend fun updateParticipantReadAt(readStatus: ParticipantReadBroadcast) = withContext(Dispatchers.IO) {
        dbQuery.updateParticipantReadAt(
            id = readStatus.participantId,
            readAt = readStatus.readAt,
        )
    }
}
