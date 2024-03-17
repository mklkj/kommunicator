package io.github.mklkj.kommunicator.data.repository

import io.github.mklkj.kommunicator.data.api.service.MessagesService
import io.github.mklkj.kommunicator.data.models.Chat
import io.github.mklkj.kommunicator.data.models.ChatCreateRequest
import io.github.mklkj.kommunicator.data.models.ChatCreateResponse
import io.github.mklkj.kommunicator.data.models.Message
import io.github.mklkj.kommunicator.data.models.MessageEvent
import kotlinx.coroutines.test.runTest
import kotlinx.uuid.UUID
import kotlin.test.Test

class MessagesRepositoryTest {

    @Test
    fun `test messages service`() = runTest {
        val service = object : MessagesService {
            override suspend fun getChats(): List<Chat> = emptyList()
            override suspend fun getChat(id: UUID) = Chat(UUID(), "", emptyList(), null, null)
            override suspend fun getMessages(chatId: UUID): List<Message> = emptyList()
            override suspend fun sendMessage(chatId: UUID, message: MessageEvent) = Unit
            override suspend fun createChat(body: ChatCreateRequest): ChatCreateResponse {
                TODO("Not yet implemented")
            }
        }
//        val repo = MessagesRepository(service)
//
//        assertEquals(emptyList(), repo.getChats())
    }
}
