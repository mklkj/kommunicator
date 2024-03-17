package io.github.mklkj.kommunicator.data.exceptions

import java.net.ConnectException
import java.net.UnknownHostException

actual fun Throwable.isNetworkException(): Boolean {
    return when (this) {
        is ConnectException -> true
        is UnknownHostException -> true
        else -> false
    }
}
