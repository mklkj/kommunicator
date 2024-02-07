package io.github.mklkj.kommunicator.utils

import io.github.mklkj.kommunicator.getDeserialized
import io.ktor.serialization.WebsocketConverterNotFoundException
import io.ktor.server.websocket.WebSocketServerSession
import io.ktor.server.websocket.converter
import io.ktor.util.reflect.TypeInfo
import io.ktor.util.reflect.typeInfo
import io.ktor.websocket.Frame

suspend inline fun <reified T> Frame.getDeserialized(
    session: WebSocketServerSession,
): T = getDeserialized(session, typeInfo<T>())

suspend fun <T> Frame.getDeserialized(session: WebSocketServerSession, typeInfo: TypeInfo): T {
    val converter = session.converter
        ?: throw WebsocketConverterNotFoundException("No converter was found for websocket")
    return getDeserialized(typeInfo, converter) as T
}
