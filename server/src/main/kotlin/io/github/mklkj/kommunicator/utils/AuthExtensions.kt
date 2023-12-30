package io.github.mklkj.kommunicator.utils

import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import kotlinx.uuid.UUID

fun extractPrincipalUsername(call: ApplicationCall): String? =
    call.principal<JWTPrincipal>()
        ?.payload
        ?.getClaim("username")
        ?.asString()

val ApplicationCall.principalUsername: String?
    get() = principal<JWTPrincipal>()
        ?.payload
        ?.getClaim("username")
        ?.asString()

val ApplicationCall.principalId: UUID?
    get() = principal<JWTPrincipal>()
        ?.payload
        ?.getClaim("userId")
        ?.asString()
        ?.let(::UUID)
