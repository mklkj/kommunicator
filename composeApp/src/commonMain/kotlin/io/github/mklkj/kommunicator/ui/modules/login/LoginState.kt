package io.github.mklkj.kommunicator.ui.modules.login

data class LoginState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null,
)
