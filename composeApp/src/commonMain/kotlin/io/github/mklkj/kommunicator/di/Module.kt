package io.github.mklkj.kommunicator.di

import de.jensklingenberg.ktorfit.Ktorfit
import io.github.mklkj.kommunicator.BuildKonfig
import io.github.mklkj.kommunicator.data.api.service.MessagesService
import io.github.mklkj.kommunicator.data.api.service.UserService
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

val commonModule = module {
    single {
        Ktorfit.Builder()
            .baseUrl(BuildKonfig.baseUrl)
            .httpClient {
                install(ContentNegotiation) {
                    json()
                }
            }
            .build()
    }
    single { get<Ktorfit>().create<MessagesService>() }
    single { get<Ktorfit>().create<UserService>() }
}

expect val platformModule: Module

fun initKoin() {
    startKoin {
        modules(commonModule, platformModule)
    }
}
