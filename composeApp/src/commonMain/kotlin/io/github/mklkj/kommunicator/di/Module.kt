package io.github.mklkj.kommunicator.di

import co.touchlab.kermit.Logger
import de.jensklingenberg.ktorfit.Ktorfit
import io.github.mklkj.kommunicator.BuildKonfig
import io.github.mklkj.kommunicator.data.api.service.ContactService
import io.github.mklkj.kommunicator.data.api.service.MessagesService
import io.github.mklkj.kommunicator.data.api.service.UserService
import io.github.mklkj.kommunicator.data.db.Database
import io.github.mklkj.kommunicator.data.exceptions.UserTokenExpiredException
import io.github.mklkj.kommunicator.data.models.LoginRefreshRequest
import io.github.mklkj.kommunicator.data.models.LoginResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
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
            defaultRequest {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
            }
            expectSuccess = true
            install(ContentNegotiation) {
                json(get())
            }
            install(Auth) {
                val database = get<Database>()
                // todo: fix issue on user login/registration
                bearer {
                    loadTokens {
                        database.getCurrentUser()?.let {
                            BearerTokens(
                                accessToken = it.token,
                                refreshToken = it.refreshToken,
                            )
                        }
                    }
                    refreshTokens {
                        val currentUser = database.getCurrentUser()
                        val refreshToken = currentUser?.refreshToken
                        if (refreshToken.isNullOrBlank()) error("Refresh token missing!")

                        val tokenInfo = runCatching {
                            client.post(
                                urlString = BuildKonfig.BASE_URL.trimEnd('/') + "/api/auth/refresh",
                            ) {
                                contentType(ContentType.Application.Json)
                                setBody(LoginRefreshRequest(refreshToken = refreshToken))
                                markAsRefreshTokenRequest()
                            }.body<LoginResponse>()
                        }.onFailure {
                            if (it is ClientRequestException && it.response.status == HttpStatusCode.Unauthorized) {
                                database.deleteCurrentUser()
                                throw UserTokenExpiredException(it)
                            } else throw it
                        }.getOrThrow()

                        database.updateUserTokens(
                            id = tokenInfo.id,
                            token = tokenInfo.token,
                            refreshToken = tokenInfo.refreshToken,
                        )
                        BearerTokens(
                            accessToken = tokenInfo.token,
                            refreshToken = tokenInfo.refreshToken,
                        )
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
    single { get<Ktorfit>().create<ContactService>() }
}
