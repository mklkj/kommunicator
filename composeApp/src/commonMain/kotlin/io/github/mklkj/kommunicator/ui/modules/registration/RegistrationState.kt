package io.github.mklkj.kommunicator.ui.modules.registration

data class RegistrationState(
    val isLoading: Boolean = false,
    val isRegistered: Boolean = false,
    val errorMessage: String? = null,
)
