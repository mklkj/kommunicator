package io.github.mklkj.kommunicator.ui.modules.contacts

import io.github.mklkj.kommunicator.data.models.Contact

data class ContactsState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val contacts: List<Contact> = emptyList(),
)
