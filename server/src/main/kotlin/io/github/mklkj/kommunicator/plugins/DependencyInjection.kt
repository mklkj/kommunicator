package io.github.mklkj.kommunicator.plugins

import io.github.mklkj.kommunicator.data.DatabaseFactory
import io.ktor.server.application.Application
import io.ktor.server.application.install
import kotlinx.serialization.json.Json
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.ksp.generated.defaultModule
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

fun Application.configureDependencyInjection() {
    val appModule = module {
        single { this@configureDependencyInjection } bind Application::class
        single<PasswordEncoder> { BCryptPasswordEncoder() }
        single<Json> { Json }
    }
    install(Koin) {
        modules(appModule, defaultModule)
    }

    val databaseFactory by inject<DatabaseFactory>()
    databaseFactory.init()
}
