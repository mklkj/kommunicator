package io.github.mklkj.kommunicator.plugins

import io.github.mklkj.kommunicator.data.DatabaseFactory
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.ksp.generated.defaultModule
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin

fun Application.configureDependencyInjection() {
    val appModule = module {
        single { this@configureDependencyInjection } bind Application::class
    }
    install(Koin) {
        modules(appModule, defaultModule)
    }

    val databaseFactory by inject<DatabaseFactory>()
    databaseFactory.init()
}
