package io.github.mklkj.kommunicator.di

import co.touchlab.kermit.Logger
import de.jensklingenberg.ktorfit.Ktorfit
import io.github.mklkj.kommunicator.BuildKonfig
import io.github.mklkj.kommunicator.data.api.service.MessagesService
import io.github.mklkj.kommunicator.data.api.service.UserService
import io.github.mklkj.kommunicator.data.db.Database
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import io.ktor.client.plugins.logging.Logger as KtorLogger

val commonModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
            useAlternativeNames = false
        }
    }
    single {
        HttpClient {
            expectSuccess = true
            install(ContentNegotiation) {
                json(get())
            }
            install(Auth) {
                bearer {
                    loadTokens {
                        val database = get<Database>()
                        database.getCurrentUser()?.let {
                            BearerTokens(
                                accessToken = it.token,
                                refreshToken = it.token, //it.refreshToken,
                            )
                        }
                    }
                }
            }
            install(Logging) {
                sanitizeHeader { header ->
                    header == HttpHeaders.Authorization && !BuildKonfig.IS_DEBUG
                }
                level = if (BuildKonfig.IS_DEBUG) LogLevel.BODY else LogLevel.INFO
                logger = object : KtorLogger {
                    override fun log(message: String) {
                        Logger.v(message)
                    }
                }
            }
        }
    }
    single {
        Ktorfit.Builder()
            .baseUrl(BuildKonfig.BASE_URL.trimEnd('/'), checkUrl = false)
            .httpClient(get<HttpClient>())
            .build()
    }
    single { get<Ktorfit>().create<MessagesService>() }
    single { get<Ktorfit>().create<UserService>() }
}
