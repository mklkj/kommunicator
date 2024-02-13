package io.github.mklkj.kommunicator.data.ws

sealed interface ConnectionStatus {

    data object NotConnected : ConnectionStatus

    data object Connecting : ConnectionStatus

    data object Connected : ConnectionStatus

    data class Error(val error: Throwable) : ConnectionStatus {
        override fun toString(): String = "Connection error"
    }
}
