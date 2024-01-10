package io.github.mklkj.kommunicator.routes

import io.github.mklkj.kommunicator.data.models.Contact
import io.github.mklkj.kommunicator.data.models.ContactAddRequest
import io.github.mklkj.kommunicator.data.models.ContactsResponse
import io.github.mklkj.kommunicator.data.service.ContactService
import io.github.mklkj.kommunicator.data.service.UserService
import io.github.mklkj.kommunicator.utils.principalId
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import org.koin.ktor.ext.inject

fun Route.contactRoutes() {
    val userService by inject<UserService>()
    val contactService by inject<ContactService>()

    post {
        val request = call.receive<ContactAddRequest>()
        val currentUserId = call.principalId ?: error("Invalid JWT!")
        val contactUser = userService
            .findByUsername(request.username)
            ?: return@post call.response.status(HttpStatusCode.BadRequest)

        contactService.saveContactForUser(
            currentUser = currentUserId,
            contactUser = contactUser.id,
        )

        call.response.status(HttpStatusCode.Created)
    }

    get {
        val userId = call.principalId ?: error("Invalid JWT")
        val items = contactService.getContactsByUser(userId)
        call.respond(
            ContactsResponse(
                contacts = items.map {
                    Contact(
                        id = it.id,
                        contactUserId = it.contactUserId,
                        avatarUrl = "https://search.yahoo.com/search?p=nisl",
                        name = "Raz Dwa",
                        username = "Hans Humphrey",
                        isActive = false,
                    )
                }
            )
        )
    }
}
