package io.github.mklkj.kommunicator.data.exceptions

import io.ktor.client.engine.darwin.DarwinHttpRequestException

actual fun Throwable.isNetworkException(): Boolean {
    return this is DarwinHttpRequestException
}
