package io.github.mklkj.kommunicator.data.repository

import io.github.mklkj.kommunicator.data.api.service.MessagesService
import io.github.mklkj.kommunicator.data.models.Chat
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class MessagesRepositoryTest {

    @Test
    fun `test messages service`() = runTest {
        val service = object : MessagesService {
            override suspend fun getChats(): List<Chat> = emptyList()
        }
        val repo = MessagesRepository(service)

        assertEquals(emptyList(), repo.getChats())
    }
}
