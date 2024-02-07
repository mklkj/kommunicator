package io.github.mklkj.kommunicator

import io.ktor.serialization.WebsocketContentConverter
import io.ktor.serialization.WebsocketDeserializeException
import io.ktor.util.reflect.TypeInfo
import io.ktor.util.reflect.typeInfo
import io.ktor.utils.io.charsets.Charsets
import io.ktor.websocket.Frame

suspend inline fun <reified T> Frame.getDeserialized(
    converter: WebsocketContentConverter,
): T = getDeserialized(typeInfo<T>(), converter)

@Suppress("UNCHECKED_CAST")
suspend fun <T> Frame.getDeserialized(
    typeInfo: TypeInfo,
    converter: WebsocketContentConverter,
): T = getDeserializedBase(typeInfo, converter) as T

/**
 * @see [https://youtrack.jetbrains.com/issue/KTOR-4452]
 */
private suspend fun Frame.getDeserializedBase(
    typeInfo: TypeInfo,
    converter: WebsocketContentConverter,
): Any? {
    if (!converter.isApplicable(this)) {
        throw WebsocketDeserializeException(
            "Converter doesn't support frame type ${frameType.name}",
            frame = this
        )
    }

    val result = converter.deserialize(
        charset = Charsets.UTF_8,
        typeInfo = typeInfo,
        content = this
    )

    when {
        typeInfo.type.isInstance(result) -> return result
        result == null -> {
            if (typeInfo.kotlinType?.isMarkedNullable == true) return null
            throw WebsocketDeserializeException("Frame has null content", frame = this)
        }
    }

    throw WebsocketDeserializeException(
        "Can't deserialize value: expected value of type ${typeInfo.type.simpleName}," +
            " got ${result!!::class.simpleName}",
        frame = this
    )
}
