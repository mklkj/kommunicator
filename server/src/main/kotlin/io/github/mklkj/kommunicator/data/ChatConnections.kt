package io.github.mklkj.kommunicator.data

import io.github.mklkj.kommunicator.routes.Connection
import kotlinx.uuid.UUID
import org.koin.core.annotation.Singleton
import java.util.Collections

@Singleton
class ChatConnections {

    private val connections: MutableMap<UUID, MutableList<Connection>> =
        Collections.synchronizedMap(mutableMapOf())

    init {
        println("Connection pool created: $connections")
    }

    fun addConnection(chatId: UUID, connection: Connection) {
        connections.putIfAbsent(chatId, Collections.synchronizedList(mutableListOf()))
        connections.getValue(chatId).add(connection)
        println("Connections: ${connections.size}")
    }

    fun removeConnection(chatId: UUID, connection: Connection) {
        connections.getValue(chatId).remove(connection)
        if (connections.isEmpty()) {
            connections.remove(chatId)
        }
        println("Connections: ${connections.size}")
    }

    fun getConnections(chatId: UUID): List<Connection> {
        println("Connections: ${connections.size}")
        return connections[chatId].orEmpty()
    }
}
