package io.github.mklkj.kommunicator.data.api.service

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import io.github.mklkj.kommunicator.data.models.ContactAddRequest
import io.github.mklkj.kommunicator.data.models.ContactsResponse

interface ContactService {

    @POST("/api/contacts")
    suspend fun addContact(@Body body: ContactAddRequest)

    @GET("/api/contacts")
    suspend fun getContacts(): ContactsResponse
}
