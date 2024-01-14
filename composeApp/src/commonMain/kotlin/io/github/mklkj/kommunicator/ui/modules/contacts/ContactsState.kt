package io.github.mklkj.kommunicator.ui.modules.contacts

import io.github.mklkj.kommunicator.data.db.entity.LocalContact

data class ContactsState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val contacts: List<LocalContact> = emptyList(),
)
