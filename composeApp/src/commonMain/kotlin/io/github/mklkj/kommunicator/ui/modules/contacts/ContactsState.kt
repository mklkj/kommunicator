package io.github.mklkj.kommunicator.ui.modules.contacts

import io.github.mklkj.kommunicator.Contacts
import kotlinx.uuid.UUID

data class ContactsState(
    val isLoading: Boolean = true,
    val error: Throwable? = null,
    val contacts: List<Contacts> = emptyList(),
    val createdChat: UUID? = null,
)
