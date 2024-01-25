package io.github.mklkj.kommunicator.ui.modules.contacts

import io.github.mklkj.kommunicator.data.db.entity.LocalContact
import kotlinx.uuid.UUID

data class ContactsState(
    val isLoading: Boolean = true,
    val error: Throwable? = null,
    val contacts: List<LocalContact> = emptyList(),
    val createdChat: UUID? = null,
)
