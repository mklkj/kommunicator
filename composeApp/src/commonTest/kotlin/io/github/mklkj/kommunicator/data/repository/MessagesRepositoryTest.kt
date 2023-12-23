package io.github.mklkj.kommunicator.data.repository

import io.github.mklkj.kommunicator.data.api.service.MessagesService
import io.github.mklkj.kommunicator.data.models.Chat
import io.github.mklkj.kommunicator.data.models.ChatDetails
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class MessagesRepositoryTest {

    @Test
    fun `test messages service`() = runTest {
        val service = object : MessagesService {
            override suspend fun getChats(): List<Chat> = emptyList()
            override suspend fun getChat(id: String): ChatDetails = ChatDetails("", emptyList())
        }
        val repo = MessagesRepository(service)

        assertEquals(emptyList(), repo.getChats())
    }
}
