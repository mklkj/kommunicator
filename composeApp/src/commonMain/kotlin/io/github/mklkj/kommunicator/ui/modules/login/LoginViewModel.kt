package io.github.mklkj.kommunicator.ui.modules.login

import io.github.mklkj.kommunicator.data.repository.UserRepository
import io.github.mklkj.kommunicator.ui.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Factory

@Factory
class LoginViewModel(
    private val userRepository: UserRepository,
) : BaseViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    fun login(username: String, password: String) {
        val isNotValid = username.isBlank() || password.isBlank()
        _state.update {
            it.copy(
                isLoading = false,
                errorMessage = when {
                    isNotValid -> "All field are required"
                    else -> it.errorMessage
                }
            )
        }
        if (isNotValid) return

        launch("login_user") {
            runCatching { userRepository.loginUser(username, password) }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isLoggedIn = false,
                            errorMessage = error.message,
                        )
                    }
                }
                .onSuccess {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            errorMessage = null,
                        )
                    }
                }
        }
    }
}
