package io.github.mklkj.kommunicator

import io.github.mklkj.kommunicator.plugins.configureDependencyInjection
import io.github.mklkj.kommunicator.plugins.configureRouting
import io.github.mklkj.kommunicator.plugins.configureSecurity
import io.github.mklkj.kommunicator.plugins.configureSerialization
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    embeddedServer(
        factory = Netty,
        port = SERVER_PORT,
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    configureDependencyInjection()
    configureSerialization()
    configureSecurity()
    configureRouting()
}
