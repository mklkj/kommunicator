package io.github.mklkj.kommunicator.ui.modules.contacts.add

data class ContactAddState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
)
