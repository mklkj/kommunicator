package io.github.mklkj.kommunicator.data.ws

sealed interface ConnectionStatus {

    data object NotConnected : ConnectionStatus

    data object Connecting : ConnectionStatus

    data object Connected : ConnectionStatus

    data class Error(
        val error: Throwable?,
        val retryIn: Int = RECONNECTION_WAIT_SECONDS,
    ) : ConnectionStatus {
        override fun toString(): String = "Connection error. Retry in $retryIn seconds"
    }
}
