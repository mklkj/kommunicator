package io.github.mklkj.kommunicator.data.repository

import io.github.mklkj.kommunicator.data.api.service.MessagesService
import io.github.mklkj.kommunicator.data.db.Database
import io.github.mklkj.kommunicator.data.models.Chat
import io.github.mklkj.kommunicator.data.models.ChatCreateRequest
import io.github.mklkj.kommunicator.data.models.ChatDetails
import io.github.mklkj.kommunicator.data.models.MessageRequest
import kotlinx.coroutines.test.runTest
import kotlinx.uuid.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

class MessagesRepositoryTest {

    @Test
    fun `test messages service`() = runTest {
        val service = object : MessagesService {
            override suspend fun getChats(): List<Chat> = emptyList()
            override suspend fun createChat(body: ChatCreateRequest) = Unit
            override suspend fun getChat(id: UUID) = ChatDetails("", emptyList())
            override suspend fun sendMessage(chatId: UUID, message: MessageRequest) = Unit
        }
//        val repo = MessagesRepository(service)
//
//        assertEquals(emptyList(), repo.getChats())
    }
}
